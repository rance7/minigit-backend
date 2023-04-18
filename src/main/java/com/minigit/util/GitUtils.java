package com.minigit.util;

import com.minigit.common.R;

import java.io.*;


public class GitUtils {
    public static String originDir;
    public static String objectDir;
    public static String minigitDir;
    public static String indexPath;
    public static String headPath;

    public static R<String> init(String path) {
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
        File headFile = new File(gitDir, "HEAD");
        if(!headFile.exists()){
            try {
                FileWriter writer = new FileWriter(headFile);
                writer.write("refs\\heads\\main");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Initialized empty minigit repository in " + gitDir.getAbsolutePath());
        originDir = path;
        minigitDir = path + File.separator + ".minigit";
        objectDir = minigitDir + File.separator + "objects";
        indexPath = minigitDir + File.separator + "INDEX";
        headPath = minigitDir + File.separator + "HEAD";
        return R.success("Init Success!");
    }


}
