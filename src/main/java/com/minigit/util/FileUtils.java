package com.minigit.util;

import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;

import java.io.*;
import java.util.List;

public  class FileUtils {

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
            content.append(System.lineSeparator());
        }
        reader.close();
        return content.toString();
    }

    /**
     * 写入文件内容（追加）
     * @param filePath 文件路径
     * @param content 内容字符串
     * @throws IOException
     */
    public static void writeFile(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true));
        writer.write(content + "\n");
        writer.close();
    }

    /**
     * 写入文件内容(覆盖）
     * @param filePath 文件路径
     * @param content 内容字符串
     * @throws IOException
     */
    public static void writeFileNoAppend(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,false));
        writer.write(content + "\n");
        writer.close();
    }
    /**
     * 读取文件的一行并去除换行符
     */
    public static String readLine(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();
        reader.close();
        if (line != null) {
            line = line.replaceAll("\n|\r", "");
        }
        return line;
    }


    /**
     * 复制文件
     * @param srcFile 源文件路径
     * @param destFile 目标文件路径
     * @throws IOException
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(srcFile);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    /**
     * 删除文件或目录
     * @param path 文件或目录路径
     * @throws IOException
     */
    public static void deleteFileOrDirectory(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) return;
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteFileOrDirectory(subFile.getPath());
            }
        }
        file.delete();
    }

    /**
     * 写入object文件
     */
    public static void writeObject(String path, String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String filePath = GitUtils.objectDir + File.separator + folder + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            File folderFile = new File(GitUtils.objectDir + File.separator + folder);
            folderFile.mkdirs();
            try {
                objectFile.createNewFile();
                File file = new File(path);
                if (file.isDirectory()) {

                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static File createObjectFile(String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String filePath = GitUtils.objectDir + File.separator + folder + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            File folderFile = new File(GitUtils.objectDir + File.separator + folder);
            folderFile.mkdirs();
            try {
                objectFile.createNewFile();
            } catch (IOException e) {
                System.out.println("创建文件失败！");
                throw new RuntimeException(e);
            }
        }
        return objectFile;
    }

    public static File getObjectFile(String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        String filePath = GitUtils.objectDir + File.separator + folder + File.separator + filename;
        File objectFile = new File(filePath);
        if (!objectFile.exists()) {
            System.out.println("目标文件不存在！");
            return null;
        }
        return objectFile;
    }

    public static String getParentHash(){
        try {
            // 到head文件中找到当前分支的路径
            String branch = readFile(GitUtils.headPath);
            // 向分支中找到parentHash
            File file = new File(GitUtils.minigitDir + File.separator + branch);
            if(!file.exists()){
                System.out.println("当前分支还没有commit！");
                return null;
            }
            String commitHash =  readFile(file.getAbsolutePath());
            return commitHash;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTreeHeadHash(){
        try {
            // 到head文件中找到当前分支的路径
            String branch = readFile(GitUtils.headPath);
            // 向分支中找到parentHash
            File file = new File(GitUtils.minigitDir + File.separator + branch);
            if(!file.exists()){
                System.out.println("当前分支还没有commit！");
                return null;
            }
            String commitHash =  readFile(file.getAbsolutePath());
            // 通过commitHash找到commit的objects文件
            File treeHeadFile = getObjectFile(commitHash);
            // commit的objects文件的第一行保存着treeHeadHash
            if(!treeHeadFile.exists()){
                System.out.println("treeHeadFile不存在！");
                return null;
            }
            String treeHeadHash = readLine(treeHeadFile.getAbsolutePath());
            return treeHeadHash;
        } catch (IOException e) {
            System.out.println("HEAD文件中保存着当前分支name，不应该抛出异常！");
        }
        System.out.println("commitHash或者treeHeadHash出错！");
        return null;
    }


}

