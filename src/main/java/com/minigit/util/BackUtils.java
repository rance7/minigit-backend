package com.minigit.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.minigit.util.CommitUtils.createFileTree;

public class BackUtils {
    public static void back(String commitHash){
        // 读取要回溯的版本的commitHash
        File OldcommitFile = FileUtils.getObjectFile(commitHash);
        try {
            String content = FileUtils.readFile(OldcommitFile.getAbsolutePath());
            String[] lines = content.split("\r?\n");
            String oldTreeHeadHash = lines[0];
            String parentCommitHash = lines[2];
            if (parentCommitHash == null){
                System.out.println("没有历史提交！！");
                return ;
            }
            Map<String, String> fileMap = new HashMap<>();
            Map<String, String> currentCommitTreeMap = new HashMap<>();
            Map<String, String> oldCommitTreeMap = new HashMap<>();
            createFileTree(fileMap,new File(GitUtils.originDir));
            //createOldCommitTree(currentCommitTreeMap, oldCommitTreeMap);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void getCurrentCommitTree(String currentCommitHash){

    }
}
