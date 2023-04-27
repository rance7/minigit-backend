package com.minigit.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Description TODO
 * @Version 1.0
 */

@Slf4j
public class UploadUtils {
    private static Session session;
    private static ChannelSftp channelSftp;
    @Value("${ftp.address}")
    private static String ftpAddress;
    @Value("${ftp.username}")
    private static String ftpUserName;
    @Value("${ftp.password}")
    private static String ftpPassword;
    @Value("${ftp.port}")
    private static int ftpPort;
    @Value("${remote-repo-path}")
    public static String REMOTE_REPO_PATH;

    public static ChannelSftp getSFTPClient(){
        return channelSftp;
    }

    /**
     * 获取sftp连接
     */
    public static ChannelSftp createSFTPClient() {
        //开始时间  用于计时
        long startTime = System.currentTimeMillis();
        JSch jsch = new JSch();// 创建JSch对象
        Channel channel = null;
        try {
            //根据用户名，主机ip，端口获取一个Session对象
            session = jsch.getSession(ftpUserName, ftpAddress, ftpPort);
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
    public static void close() {
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
    public static boolean uploadFile(InputStream in, String hash){
        try{
            //建立连接
            if (channelSftp == null || !channelSftp.isConnected()) {
                channelSftp=createSFTPClient();
            }
            String dirPath = hash.substring(0,2);
            String filePath = hash.substring(2);
            SftpATTRS sftpATTRS = channelSftp.lstat(REMOTE_REPO_PATH + File.separator + dirPath);
            if(!sftpATTRS.isDir()){
                channelSftp.mkdir(REMOTE_REPO_PATH + File.separator + dirPath);
            }
            if(!sftpATTRS.isReg()){
                channelSftp.put(in, REMOTE_REPO_PATH + File.separator + dirPath + File.separator + filePath);
            }
        }catch(Exception e){
            e.printStackTrace();
            close();
        }
        return false;
    }
}

