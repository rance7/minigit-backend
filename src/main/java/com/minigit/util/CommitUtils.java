package com.minigit.util;

import com.minigit.entity.Commit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minigit.util.Sha1Utils.calculateDirSha1;
import static com.minigit.util.Sha1Utils.calculateFileSha1;

public class CommitUtils {
    public static String calculateCommitHash(Commit commit) throws NoSuchAlgorithmException {
        String data = commit.getMessage() + commit.getCommitter() + commit.getCommitTime() + commit.getParentHash();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        // byte是0-255的整数
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            hashBuilder.append(String.format("%02x", b));
        }
        return hashBuilder.toString();
    }

    public static Commit createCommit(String message, String committer, String parentHash) throws NoSuchAlgorithmException {
        // 创建一个新的提交对象
        Commit commit = new Commit();
        commit.setMessage(message);
        commit.setCommitter(committer);
        commit.setCommitTime(LocalDateTime.now());
        commit.setParentHash(parentHash);
        String hash = calculateCommitHash(commit);
        commit.setHash(hash);

        // 将新的提交写入objects文件，并清空index
        String commitHash = getHeadHash();
        if(commitHash == null) return null;
        String folder = hash.substring(0,2);
        String filename = hash.substring(2);
        String filePath = GitUtils.objectDir + File.separator + folder + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            File folderFile = new File(GitUtils.objectDir + File.separator + folder);
            folderFile.mkdirs();
            try {
                objectFile.createNewFile();
                FileUtils.writeFile(filePath, hash);
                FileUtils.writeFile(filePath, committer + "\t" + commit.getCommitTime());
                FileUtils.writeFile(filePath, message);
                FileUtils.deleteFileOrDirectory(GitUtils.indexPath);

                // 保存commit的信息
                System.out.println(FileUtils.readFile(GitUtils.headPath));
                FileUtils.writeFile(GitUtils.minigitDir + File.separator + FileUtils.readLine(GitUtils.headPath), hash);
            } catch (FileNotFoundException e) {
                System.out.println("找不到文件!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        // 添加新的提交到仓库中


        // 将当前分支的head指向新的提交

        return commit;
    }

    public static String getHeadHash(){
        try {
            String content = FileUtils.readFile(GitUtils.indexPath);
            String[] lines = content.split("\r?\n");
            for (String line : lines) {
                if(line.startsWith(GitUtils.originDir)){
                    String hash = line.substring(line.indexOf("\t") + 1);
                    return hash;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("INDEX文件中没有找到树根哈希");
        return null;
    }

    /**
     * 创建commitTree，这是一个map，保存了所有blob文件的路径和哈希值
     * @param treeHeadHash 一定是根目录（originDir)
     * @throws IOException
     */
    public static void createCommitTree(String treeHeadHash, Map<String,String> commitMap){
        try {
            File file = FileUtils.getObjectFile(treeHeadHash);
            String content = FileUtils.readFile(file.getAbsolutePath());
            if(!content.startsWith("bolb") && !content.startsWith("tree")){
                System.out.println("目标文件类型或内容错误！");
                return;
            }
            String[] lines = content.split("\r?\r");
            for (String line : lines) {
                String[] s = line.split("\t");
                if(s[0] == "blob") {
                    commitMap.put(s[1],s[2]);
                }
                else {
                    createCommitTree(s[2],commitMap);
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
    public static void createFileTree(Map<String, String> fileMap){
        File file = new File(GitUtils.originDir);
        // 忽略掉.minigit目录
        while(file.isDirectory() && file.getAbsolutePath() != GitUtils.minigitDir){
            for (File child: file.listFiles()) {
                if(child.isFile()){
                    String hash = calculateFileSha1(child);
                    fileMap.put(child.getAbsolutePath(), hash);
                }else {
                    createFileTree(fileMap);
                }
            }
        }
    }

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
            String[] lines = content.split("\r?\t");
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


    // 对于文件名字被改变的文件，我们的处理策略是，commit中删除旧文件，添加新的文件
    public static Map<String, String> getCommitTree(Map<String, String> commitTreeMap, Map<String, String> fileMap,
                                   Map<String, String> indexMap){
        if(commitTreeMap == null){
            // 如果commitTreeMap为null，说明是第一次提交，直接使用indexMap
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
        for (String path: commitTreeMap.keySet()){
            if(!fileMap.containsKey(path)){
                commitTreeMap.remove(path);
            }
        }
        // 更新成功！！！！！！！！
        return commitTreeMap;
    }


    public static String writeTree(File file, Map<String,String> fileMap, Map<String,String> commitTreeMap) {
        List<TreeEntry> treeEntries = new ArrayList<>();
        String hash;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                TreeEntry.EntryType entryType = child.isDirectory() ? TreeEntry.EntryType.tree : TreeEntry.EntryType.blob;
                hash = writeTree(child, fileMap, commitTreeMap);
                treeEntries.add(new TreeEntry(child.getAbsolutePath(), hash, entryType));
            }
            hash = calculateDirSha1(treeEntries);
            writeObject(treeEntries);
        } else if(file.isFile() && commitTreeMap.containsKey(file.getAbsolutePath())) {
            // 如果commitTreeMap中包含这个文件
            hash = commitTreeMap.get(file.getAbsolutePath());
            // 那么就将这个文件加入到treeEntries
            treeEntries.add(new TreeEntry(file.getAbsolutePath(), hash, TreeEntry.EntryType.blob));
        } else {
            hash = null;
        }
        return hash;
    }


    public static void writeObject(List<TreeEntry> treeEntries){
        StringBuilder sb = new StringBuilder();
        // 计算blob文件的哈希值，写入objects
        for (TreeEntry treeEntry : treeEntries) {
            // 因为未被追踪的文件的hash为null
            if(treeEntry.getHash() != null){
                sb.append(treeEntry.getEntryType() + "\t").append(treeEntry.getPath() + "\t").
                        append(treeEntry.getHash() + "\n");
                File file = FileUtils.createObjectFile(treeEntry.getHash());
                // 对于每个blob文件，写入object并将type、path和hash写入sb
                // 对每个tree文件，仅将type、path和hash写入sb，而将tree文件写入object的操作是根据每个目录下的所有文件完成的/
                // 也就是有目录下的文件生成上一个目录的hash
                if(treeEntry.getEntryType() == TreeEntry.EntryType.blob){
                    try {
                        String content = FileUtils.readFile(treeEntry.getPath());
                        FileUtils.writeFile(file.getAbsolutePath(), content);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
        // 计算tree文件的哈希值，写入objects
        String dirHash = calculateDirSha1(treeEntries);
        File dirFile = FileUtils.createObjectFile(dirHash);
            try {
                dirFile.createNewFile();
                FileUtils.writeFile(dirFile.getAbsolutePath(), sb.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

}
