package com.minigit.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minigit.util.Sha1Utils.calculateDirSha1;
import static com.minigit.util.Sha1Utils.calculateFileSha1;

public class CommitUtils {
    /**
     * 计算commitHash值
     * @param
     */
    public static String calculateCommitHash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        // byte是0-255的整数
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            hashBuilder.append(String.format("%02x", b));
        }
        return hashBuilder.toString();
    }
    /**
     * 创建旧的commitTree，这是一个map，保存了所有blob文件的路径和哈希值，传入的hash是treeHeadHash
     * @param hash
     * @param commitTreeMap
     * @throws IOException
     */
    public static void createOldCommitTree(String hash, Map<String,String> commitTreeMap){
        try {
            if(hash == null){
                return;
            }
            File file = FileUtils.getObjectFile(hash);
            String content = FileUtils.readFile(file.getAbsolutePath());
            if(!content.startsWith("blob") && !content.startsWith("tree")){
                System.out.println("目标文件类型或内容错误！" + content);
                return;
            }
            String[] lines = content.split("\r?\n");
            for (String line : lines) {
                String[] s = line.split("\t");
                if(s[0].equals("blob")) {
                    commitTreeMap.put(s[1],s[2]);
                }
                else if(s[0].equals("tree")){
                    createOldCommitTree(s[2],commitTreeMap);
                }else {
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("目标文件不存在！");
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建fileTree，这是一个map，保存了所有文件（不包括目录）的路径和哈希值
     * @param fileMap
     */
    public static void createFileTree(Map<String, String> fileMap, File file){
        // 忽略掉.minigit目录
        if(GitUtils.minigitDir.toString().equals(file.getAbsolutePath())) {
            return;
        }
        if(file.isDirectory()){
            if(file.listFiles().length == 0) return;
            for (File child: file.listFiles()) {
                if(child.isFile()){
                    String hash = calculateFileSha1(child);
                    fileMap.put(child.getAbsolutePath(), hash);
                }else {
                    createFileTree(fileMap,child);
                }
            }
        }
    }

    /**
     * 创建indexTree，这是一个map，保存了所有缓存文件的路径和哈希值
     * @param indexMap
     */
    public static void createIndexTree(Map<String, String> indexMap){
        File file = new File(GitUtils.indexPath);
        try {
            if(!file.exists()){
                System.out.println("没有add任何文件！");
                return;
            }
            String content = FileUtils.readFile(file.getAbsolutePath());
            if(content == "" || content == null){
                System.out.println("INDEX文件为空或者INDEX文件不存在！");
                return;
            }
            String[] lines = content.split("\r?\n");
            for (String line : lines) {
                String[] s = line.split("\t");
                indexMap.put(s[0],s[1]);
            }
        } catch (IOException e) {
            System.out.println("读取INDEX文件出错！");
            throw new RuntimeException(e);
        }
    }

    public static void getFileStatus(){

    }

    /**
     * 根据旧的commitTree、fileMap和indexMap得到新的提交树
     * @param
     */
    // 对于文件名字被改变的文件，我们的处理策略是，commit中删除旧文件，添加新的文件
    public static Map<String, String> getNewCommitTree(Map<String, String> commitTreeMap, Map<String, String> fileMap,
                                   Map<String, String> indexMap){
        if(commitTreeMap == null){
            // 如果commitTreeMap为null，说明是第一次提交，直接使用indexMap
            System.out.println(indexMap);
            return indexMap;
        }
        // 对每个缓冲区的文件和commitTree中的文件做比较
        for (String path : indexMap.keySet()) {
            // 1 如果commitTreeMap包含这个key
            // 1.1 已提交但已修改
            // 1.2 已提交且未修改
            if(commitTreeMap.containsKey(path)){
                // 1.1 已提交且未修改
                if(commitTreeMap.containsValue(indexMap.get(path))){
                    // 什么都不做
                } else {
                    // 1.2 已提交但已修改
                    // 覆盖为新的文件哈希值
                    commitTreeMap.put(path, indexMap.get(path));
                }
            } else {
                // 2 如果commit不包含这个key，则这是一个新添加的文件
                commitTreeMap.put(path,indexMap.get(path));
            }
        }
        // 上面的操作已经将新添加的文件和修改的文件都添加到了commitTree，现在要检测已删除的文件
        // 遍历commitTreeMap中的key，若在实际文件目录中不存在，那么是已删除的
        List<String> list = new ArrayList<>();
        for (String path: commitTreeMap.keySet()){
            if(!fileMap.containsKey(path)){
                list.add(path);
            }
        }
        for (String path : list) {
            commitTreeMap.remove(path);
        }
        // 更新成功！！！！！！！！,获得更新后的commitTreeMap
        return commitTreeMap;
    }

    /**
     * 根据新的commitTree将文件写入object，获得treeHeadHash
     * @param
     */
    public static String writeTree(File file, Map<String,String> commitTreeMap) {
        List<TreeEntry> treeEntries = new ArrayList<>();
        String hash;
        if (file.getAbsolutePath().equals(GitUtils.minigitDir)) return null;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                TreeEntry.EntryType entryType = child.isDirectory() ? TreeEntry.EntryType.tree : TreeEntry.EntryType.blob;
                hash = writeTree(child, commitTreeMap);
                if(hash != null){
                    treeEntries.add(new TreeEntry(child.getAbsolutePath(), hash, entryType));
                }
            }
            // 对于空文件夹，应该在前端传入路径的时候做判断，如果是空目录，则加入一个.gitkeep文件，
            // 在write-tree的时候，map中的应该都是blob文件，不存在空目录的情况
            if (treeEntries.size() == 0){
                return null;
            }
            hash = calculateDirSha1(treeEntries, file.getAbsolutePath());
            writeObject(treeEntries, file.getAbsolutePath());
            System.out.println(treeEntries);
        } else{
            hash = commitTreeMap.get(file.getAbsolutePath());
        }
        // 获得treeHeadHash
        return hash;
    }

    public static void writeObject(List<TreeEntry> treeEntries, String dirPath){
        if(treeEntries.size() == 0){
            // 这个判断有一点多余，因为暂时只有writeTree中调用了这个方法，而writeTree中已经有了此情况的处理
            return ;
        }
        StringBuilder sb = new StringBuilder();
        // 计算blob文件的哈希值，写入objects
        for (TreeEntry treeEntry : treeEntries) {
            // 因为未被追踪的文件的hash为null
            if(treeEntry.getHash() != null){
                sb.append(treeEntry.getEntryType() + "\t").append(treeEntry.getPath() + "\t").
                        append(treeEntry.getHash() + "\n");
                // 对于每个blob文件，写入object并将type、path和hash写入sb
                // 对每个tree文件，仅将type、path和hash写入sb，而将tree文件写入object的操作是根据每个目录下的所有文件完成的/
                // 也就是有目录下的文件生成上一个目录的hash
                if(treeEntry.getEntryType() == TreeEntry.EntryType.blob){
                    try {
                        File file = FileUtils.createObjectFile(treeEntry.getHash());
                        // file == null，代表objectFile已经存在，即文件未发生改变，也就不用写
                        if(file != null){
                            String content = FileUtils.readFile(treeEntry.getPath());
                            FileUtils.writeFile(file.getAbsolutePath(), content);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        // 计算tree文件的哈希值，写入objects
        String dirHash = calculateDirSha1(treeEntries, dirPath);
        File dirFile = FileUtils.createObjectFile(dirHash);
        try {
            if(dirFile != null) {
                dirFile.createNewFile();
                FileUtils.writeFile(dirFile.getAbsolutePath(), sb.toString());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
