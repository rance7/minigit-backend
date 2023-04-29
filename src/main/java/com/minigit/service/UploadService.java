package com.minigit.service;

import com.jcraft.jsch.*;
import com.minigit.common.R;
import com.minigit.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

@Slf4j
@Service
public class UploadService {
    private Session session;
    private ChannelSftp channelSftp;
    @Value("${ftp.address}")
    private String ftpAddress;
    @Value("${ftp.username}")
    private String ftpUserName;
    @Value("${ftp.password}")
    private String ftpPassword;
    @Value("${ftp.port}")
    private int ftpPort;
    @Value("${remote-repo-path}")
    public String REMOTE_REPO_PATH;

    public ChannelSftp getSFTPClient(){
        return channelSftp;
    }

    /**
     * 获取sftp连接
     */
    public ChannelSftp createSFTPClient() {

        //开始时间  用于计时
        long startTime = System.currentTimeMillis();
        JSch jsch = new JSch();// 创建JSch对象
        Channel channel = null;
        try {
            System.out.println("获取连接！");
            //根据用户名，主机ip，端口获取一个Session对象
            session = jsch.getSession(ftpUserName, ftpAddress, ftpPort);
            System.out.println(session);
            session.setPassword(ftpPassword); // 设置密码
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config); // 为Session对象设置properties
            //session.setTimeout(timeout); // 设置timeout时间
            session.connect(); // 通过Session建立链接
            channel = session.openChannel("sftp"); // 打开SFTP通道
            channel.connect(); // 建立SFTP通道的连接
            long endTime = System.currentTimeMillis();
            log.info("连接sftp成功耗时" + (endTime - startTime) + "毫秒");
            System.out.println(session);
            System.out.println(channel);
            System.out.println("获取连接成功！");
            return (ChannelSftp) channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description //关闭链接资源
     * @return void
     **/
    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        log.info("关闭连接资源");
    }

    /**
     * @Description //上传文件
     * @return boolean
     **/
    public boolean uploadFile(String repoPath, String userName, String repoName, String branchName){
        try{
            //建立连接
            if (channelSftp == null || !channelSftp.isConnected()) {
                channelSftp=createSFTPClient();
            }
            String branchPath = REMOTE_REPO_PATH + "/"+ userName + "/" + repoName +
                    "/" + branchName;
            deleteDirectory(REMOTE_REPO_PATH, channelSftp);
            createDir(branchPath,channelSftp);
            String commitHash = FileUtils.getCurrentCommitHash(repoPath);
            String treeHeadHash = FileUtils.getTreeHeadHash(commitHash,repoPath);
            uploadCommitFile(channelSftp, treeHeadHash, repoPath, branchPath);
        }catch(Exception e){
            e.printStackTrace();
            close();
        }
        return false;
    }

    /**
     * 在远程服务器上递归创建目录
     * @param dirPath 目录路径，格式为"dir1/dir2/dir3"
     * @param sftp    SFTP客户端
     */
    public void createDir(String dirPath, ChannelSftp sftp) throws SftpException {
        String[] dirs = dirPath.split("/");
        String currentDir = "";
        for (String dir : dirs) {
            if (dir.isEmpty()) {
                continue;
            }
            currentDir += "/" + dir;
            try {
                sftp.cd(currentDir);
            } catch (SftpException e) {
                sftp.mkdir(currentDir);
                sftp.cd(currentDir);
            }
        }
    }


    public void uploadCommitFile(ChannelSftp sftp, String hash, String repoPath, String branchPath) throws SftpException, IOException {
        File file = FileUtils.getObjectFile(hash, repoPath);
        String content = FileUtils.readFile(file.getAbsolutePath());
        String[] lines = content.split("\n?\r");
        for (String line : lines) {
            if(line.equals("")) return;
            String[] s = line.split("\t");
            if(s[0].equals("blob")){
                channelSftp.put(file.getAbsolutePath(), branchPath + s[1].replaceFirst(repoPath
                                .replace("\\", "\\\\"),"")
                                .replaceAll("\\\\","/"));
            }else{
                channelSftp.mkdir(branchPath + s[1].replaceFirst(repoPath.replace("\\", "\\\\")
                                ,"").replaceAll("\\\\","/"));
                uploadCommitFile(sftp, s[2], repoPath, branchPath);
            }
        }
    }

    public void checkRemoteDirectory(String remoteDir, ChannelSftp channelSftp) throws SftpException {
        try {
            channelSftp.cd(remoteDir);
        } catch (SftpException e) {
            channelSftp.mkdir(remoteDir);
        }
    }


    /**
     * 上传一个文件或者整个目录
     * @param sftp
     * @param localPath
     * @param remotePath
     * @throws Exception
     */
    public void uploadFile(ChannelSftp sftp, String localPath, String remotePath) throws Exception {
        File localFile = new File(localPath);
        if (localFile.isFile()) {
            sftp.put(localPath, remotePath);
        } else {
            File[] files = localFile.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile()) {
                    sftp.put(file.getAbsolutePath(), remotePath + "/" + fileName);
                } else {
                    sftp.mkdir(remotePath + "/" + fileName);
                    uploadFile(sftp, file.getAbsolutePath(), remotePath + "/" + fileName);
                }
            }
        }
    }


    public boolean deleteDirectory(String dirPath, ChannelSftp sftp) throws SftpException {
        try{
            sftp.stat(dirPath);
        }catch (Exception e){
            return false;
        }
        Vector<ChannelSftp.LsEntry> fileList = sftp.ls(dirPath);
        for (ChannelSftp.LsEntry entry : fileList) {
            String fileName = entry.getFilename();
            if (".".equals(fileName) || "..".equals(fileName)) {
                continue;
            }
            String path = dirPath + "/" + fileName;
            if (entry.getAttrs().isDir()) {
                deleteDirectory(path, sftp);
            } else {
                sftp.rm(path);
            }
        }
        sftp.rmdir(dirPath);
        return true;
    }

}
