package com.minigit.util;

import java.io.File;
import java.io.IOException;

import static com.minigit.util.Sha1Utils.*;

public class AddUtils {

    public static void addFile(File[] files){
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


    public static void addDir(File file){

    }

}
