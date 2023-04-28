package com.minigit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.common.R;
import com.minigit.entity.Commit;
import com.minigit.entity.Repo;
import com.minigit.entityService.RepoService;
import com.minigit.util.FileUtils;
import com.minigit.util.Sha1Utils;
import com.minigit.util.UploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minigit.util.Sha1Utils.calculateFileSha1;

@Service
public class GitService {
    @Autowired
    private RepoService repoService;
    @Autowired
    private CommitUtilService commitUtilService;
    @Autowired
    private BackService backService;

    public Repo init(String path, Long authorId, Repo repo) {
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getPath,path);
        Repo repo1 = repoService.getOne(queryWrapper);
        if(repo1 != null){
            System.out.println("仓库已经存在！");
            return repo1;
        }
        File gitDir = new File(path, ".minigit");
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
        repo.setAuthorId(authorId);
        System.out.println("初始化仓库成功！");
        return repo;
    }

    public void add(List<File> files, String repoPath) {
        for (File file : files) {
            String hash = calculateFileSha1(file);
            try {
                FileUtils.writeFile(repoPath + File.separator + ".minigit" + File.separator + "INDEX",
                        file.getAbsolutePath() + "\t" + hash);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Commit commit(String message, String committer,String repoPath) throws NoSuchAlgorithmException {
        Commit commit = new Commit();
        String oldCommitHash = FileUtils.getCurrentCommitHash(repoPath);
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash,repoPath);
        Map<String, String> fileMap = new HashMap<>();
        Map<String, String> indexMap = new HashMap<>();
        Map<String, String> commitTreeMap = new HashMap<>();
        commitUtilService.createIndexTree(indexMap,repoPath);
        commitUtilService.createOldCommitTree(oldTreeHeadHash, commitTreeMap,repoPath);
        commitUtilService.createFileTree(fileMap,new File(repoPath));
        commitUtilService.getNewCommitTree(commitTreeMap, fileMap, indexMap);
        String newTreeHeadHash = commitUtilService.writeTree(new File(repoPath), commitTreeMap, repoPath);
        System.out.println(indexMap);
        System.out.println(commitTreeMap);
        System.out.println(newTreeHeadHash);
        // 将新的提交写入objects文件，并清空index
        StringBuilder sb = new StringBuilder();
        // 这里应该再有一个提交时间
        String data = sb.append(newTreeHeadHash + "\n")
                .append(committer + "\t" + "2020-4-18 00:00:00 \n")
                .append(oldCommitHash + "\n")
                .append(message).toString();
        String commitHash = commitUtilService.calculateCommitHash(data);
        commit.setParentHash(oldCommitHash);
        commit.setMessage(message);
        commit.setHash(commitHash);
        commit.setCommitter(committer);
        File file = FileUtils.createObjectFile(commitHash,repoPath);
        System.out.println(file);
        try {
            FileUtils.writeFile(file.getAbsolutePath(), data);
            // 将refs/head/main中的commitHash替换为最新的hash
            String headPath = repoPath + File.separator + ".minigit" + File.separator + "HEAD";
            FileUtils.writeFileNoAppend(repoPath + File.separator + ".minigit" + File.separator + "refs" +
                    File.separator + "heads" + File.separator + FileUtils.readLine(headPath), commitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(repoPath + File.separator + ".minigit" + File.separator + "INDEX");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commit;
    }

    public void back(String oldCommitHash,String repoPath){
        Map<String, String> currentCommitTreeMap = backService.getCurrentCommitTree(repoPath);
        Map<String, String> oldCommitTreeMap = backService.getOldCommitTree(oldCommitHash,repoPath);
        Map<String, String> fileMap = backService.getFileMap(repoPath);
        Map<String, String> deleteMap = backService.getDeleteMap(currentCommitTreeMap, oldCommitTreeMap);
        Map<String, String> createMap = backService.getCreateMap(currentCommitTreeMap, oldCommitTreeMap);
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
                File objectFile = FileUtils.getObjectFile(oldhash,repoPath);
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
            String headPath = repoPath + File.separator + ".minigit" + File.separator + "HEAD";
                    FileUtils.writeFileNoAppend(repoPath + File.separator + ".minigit" + File.separator + "refs"
                    + File.separator + "heads" + FileUtils.readLine(headPath), oldCommitHash);
            // 删除缓冲区的内容
            FileUtils.deleteFileOrDirectory(repoPath + File.separator + ".minigit" + File.separator + "INDEX");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void push(List<Commit> commitHashes, String repoPath){
        for(int i = 0; i < commitHashes.size(); i++){
            String commitHash = commitHashes.get(i).getHash();
            String treeHeadHash = FileUtils.getTreeHeadHash(commitHash,repoPath);
            File file = FileUtils.getObjectFile(treeHeadHash,repoPath);
            try {
                UploadUtils.uploadFile(new FileInputStream(file), commitHash);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
