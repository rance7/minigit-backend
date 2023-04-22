package com.minigit.util;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 对于回退，我们的做法是读取当前commitTree和要回退的版本的commitTree，删除当前commitTree的文件，
 * 创建要回退的版本的commitTree的文件
 */

public class BackUtils {

    public static Map<String, String> getCurrentCommitTree(){
        String currentCommitHash = FileUtils.getCurrentCommitHash();
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(currentCommitHash);
        Map<String, String> currentCommitTreeMap = new HashMap<>();
        CommitUtils.createOldCommitTree(oldTreeHeadHash,currentCommitTreeMap);
        if(currentCommitTreeMap.size() == 0){
            System.out.println("当前分支还没有提交！！");
            return null;
        }
        return currentCommitTreeMap;
    }

    public static Map<String, String> getOldCommitTree(String oldCommitHash){
        String oldTreeHeadHash = FileUtils.getTreeHeadHash(oldCommitHash);
        Map<String, String> oldCommitTreeMap = new HashMap<>();
        CommitUtils.createOldCommitTree(oldTreeHeadHash,oldCommitTreeMap);
        if(oldCommitTreeMap.size() == 0){
            System.out.println("该历史提交不存在！！");
            return null;
        }
        return oldCommitTreeMap;
    }


    public static Map<String,String> getDeleteMap(Map<String, String> currentCommitTreeMap,
                                                  Map<String, String> oldCommitTreeMap){
        Map<String, String> deleteMap = new HashMap<>();
        for (String path : currentCommitTreeMap.keySet()) {
            // 如果老版本的commitTree中没有某个文件，则加入deleteMap，之后从实际目录中删除它
            if(!oldCommitTreeMap.containsKey(path)){
                deleteMap.put(path,currentCommitTreeMap.get(path));
            }
        }
        return deleteMap;
    }

    public static Map<String,String> getCreateMap(Map<String, String> currentCommitTreeMap,
                                                  Map<String, String> oldCommitTreeMap){
        Map<String, String> createMap = new HashMap<>();
        for (String path : oldCommitTreeMap.keySet()) {
            // 如果当前版本的commitTree中没有某个文件，则加入createMap，之后从实际目录中创建它
            if(!currentCommitTreeMap.containsKey(path)){
                createMap.put(path,currentCommitTreeMap.get(path));
            }
        }
        return createMap;
    }

    public static Map<String, String> getFileMap(){
        Map<String, String> fileMap = new HashMap<>();
        CommitUtils.createFileTree(fileMap, new File(GitUtils.originDir));
        return fileMap;
    }
}
