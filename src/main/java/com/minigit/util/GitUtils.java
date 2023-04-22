package com.minigit.util;

import com.minigit.common.R;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.minigit.util.CommitUtils.*;


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

    /**
     * 生成新的commitHash，将commit信息写入object，将refs/head/commitTreeMap中的commitHash替换为最新的hash，删除缓冲区的内容
     * @param
     */
    public static void commit(String message, String committer) throws NoSuchAlgorithmException {
        String parentCommitHash = FileUtils.getParentHash();
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(parentCommitHash);
        Map<String, String> fileMap = new HashMap<>();
        Map<String, String> indexMap = new HashMap<>();
        Map<String, String> commitTreeMap = new HashMap<>();
        createIndexTree(indexMap);
        createOldCommitTree(oldTreeHeadHash, commitTreeMap);
        System.out.println("oldCommitTreeMap          " + commitTreeMap);
        createFileTree(fileMap,new File(GitUtils.originDir));
        getNewCommitTree(commitTreeMap, fileMap, indexMap);
        String newTreeHeadHash = writeTree(new File(GitUtils.originDir), commitTreeMap);
        System.out.println("indexMap    " + indexMap);
        System.out.println("newCommitTreeMap         " + commitTreeMap);
        // 将新的提交写入objects文件，并清空index
        StringBuilder sb = new StringBuilder();
        // 这里应该再有一个提交时间
        String data = sb.append(newTreeHeadHash + "\n")
                .append(committer + "\t" + "2020-4-18 00:00:00 \n")
                .append(parentCommitHash + "\n")
                .append(message).toString();
        String commitHash = calculateCommitHash(data);
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
    }
}
