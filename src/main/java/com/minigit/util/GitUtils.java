package com.minigit.util;

import com.minigit.common.R;
import com.minigit.entity.Commit;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minigit.util.BackUtils.*;
import static com.minigit.util.CommitUtils.*;
import static com.minigit.util.Sha1Utils.calculateFileSha1;


public class GitUtils {
    public static String originDir;
    public static String objectDir;
    public static String minigitDir;
    public static String indexPath;
    public static String headPath;
    public static String headsPath;
    public static String refsPath;

    public static R<String> init(String path) {
        File gitDir = new File(path, ".minigit");
        /*if(gitDir.exists()){
            System.out.println("已存在仓库文件，初始化失败！");
            return R.error("已存在仓库文件，初始化失败！");
        }*/
        if(!gitDir.exists()){
            gitDir.mkdirs();
        }
        File objectsDir = new File(gitDir, "objects");
        if(!objectsDir.exists()){
            objectsDir.mkdirs();
        }
        File refsDir = new File(gitDir, "refs");
        if(!refsDir.exists()){
            refsDir.mkdirs();
        }
        File headsDir = new File(refsDir, "heads");
        if(!headsDir.exists()){
            headsDir.mkdirs();
        }
        try {
            String headPath = gitDir + File.separator + "HEAD";
            String headBranch = "main";
            FileUtils.writeFileNoAppend(headPath, headBranch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Initialized empty minigit repository in " + gitDir.getAbsolutePath());
        originDir = path;
        minigitDir = path + File.separator + ".minigit";
        objectDir = minigitDir + File.separator + "objects";
        indexPath = minigitDir + File.separator + "INDEX";
        headPath = minigitDir + File.separator + "HEAD";
        refsPath = minigitDir + File.separator + "refs";
        headsPath = refsPath + File.separator + "heads";
        return R.success("Init Success!");
    }

    public static void add(List<File> files) {
        for (File file : files) {
            String hash = calculateFileSha1(file);
            try {
                FileUtils.writeFile(GitUtils.indexPath,
                        file.getAbsolutePath() + "\t" + hash);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 生成新的commitHash，将commit信息写入object，将refs/head/commitTreeMap中的commitHash替换为最新的hash，删除缓冲区的内容
     * @param
     */
    public static Commit commit(String message, String committer) throws NoSuchAlgorithmException {
        Commit commit = new Commit();
        String oldCommitHash = FileUtils.getCurrentCommitHash();
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash);
        Map<String, String> fileMap = new HashMap<>();
        Map<String, String> indexMap = new HashMap<>();
        Map<String, String> commitTreeMap = new HashMap<>();
        createIndexTree(indexMap);
        createOldCommitTree(oldTreeHeadHash, commitTreeMap);
        createFileTree(fileMap,new File(GitUtils.originDir));
        getNewCommitTree(commitTreeMap, fileMap, indexMap);
        String newTreeHeadHash = writeTree(new File(GitUtils.originDir), commitTreeMap);
        // 将新的提交写入objects文件，并清空index
        StringBuilder sb = new StringBuilder();
        // 这里应该再有一个提交时间
        String data = sb.append(newTreeHeadHash + "\n")
                .append(committer + "\t" + "2020-4-18 00:00:00 \n")
                .append(oldCommitHash + "\n")
                .append(message).toString();
        String commitHash = calculateCommitHash(data);
        commit.setParentHash(oldCommitHash);
        commit.setMessage(message);
        commit.setHash(commitHash);
        commit.setCommitter(committer);
        File file = FileUtils.createObjectFile(commitHash);
        try {
            FileUtils.writeFile(file.getAbsolutePath(), data);
            // 将refs/head/main中的commitHash替换为最新的hash
            FileUtils.writeFileNoAppend(GitUtils.headsPath + File.separator +
                    FileUtils.readLine(GitUtils.headPath), commitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(GitUtils.indexPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commit;
    }

    public static void back(String oldCommitHash){
        Map<String, String> currentCommitTreeMap = getCurrentCommitTree();
        Map<String, String> oldCommitTreeMap = getOldCommitTree(oldCommitHash);
        Map<String, String> fileMap = getFileMap();
        Map<String, String> deleteMap = getDeleteMap(currentCommitTreeMap, oldCommitTreeMap);
        Map<String, String> createMap = getCreateMap(currentCommitTreeMap, oldCommitTreeMap);
        for (String path : deleteMap.keySet()) {
            if(fileMap.containsKey(path)){
                new File(path).delete();
            }
        }
        for (String path: createMap.keySet()){
            if(!fileMap.containsKey(path)){
                try {
                    FileUtils.createFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (String path : oldCommitTreeMap.keySet()) {
            String currentHash = Sha1Utils.calculateFileSha1(new File(path));
            String oldhash = oldCommitTreeMap.get(path);
            if(oldhash != currentHash){
                File objectFile = FileUtils.getObjectFile(oldhash);
                try {
                    String content = FileUtils.readFile(objectFile.getAbsolutePath());
                    FileUtils.writeFileNoAppend(path, content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 将refs/head/main中的commitHash替换为最新的hash
        try {
            FileUtils.writeFileNoAppend(GitUtils.headsPath + File.separator +
                    FileUtils.readLine(GitUtils.headPath), oldCommitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(GitUtils.indexPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void push(List<Commit> commitHashes){
        for(int i = 0; i < commitHashes.size(); i++){
            String commitHash = commitHashes.get(i).getHash();
            String treeHeadHash = FileUtils.getTreeHeadHash(commitHash);
            File file = FileUtils.getObjectFile(treeHeadHash);
            try {
                UploadUtils.uploadFile(new FileInputStream(file), commitHash);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
