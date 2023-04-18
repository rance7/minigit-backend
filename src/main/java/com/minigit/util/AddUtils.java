package com.minigit.util;

import java.io.File;
import java.io.IOException;

import static com.minigit.util.Sha1Utils.*;

public class AddUtils {

    public static void addFile(File[] files){
        for (File file : files) {
            String hash = calculateFileSha1(file);
            String folder = hash.substring(0,2);
            String filename = hash.substring(2);
            String filePath = GitUtils.objectDir + File.separator + folder + File.separator + filename;
            File objectFile = new File(filePath);
            if (!objectFile.exists()) {
                File folderFile = new File(GitUtils.objectDir + File.separator + folder);
                folderFile.mkdirs();
                try{
                    objectFile.createNewFile();
                    // addFile时将文件的路径和哈希写入INDEX
                    FileUtils.writeFile(GitUtils.indexPath,
                            file.getAbsolutePath() + "\t" + hash);
                }catch (IOException e){
                    System.out.println(e);
                }
            }
        }
    }

    public static void addDir(File file){

    }

}
