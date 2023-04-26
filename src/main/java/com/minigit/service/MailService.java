package com.minigit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

@Service
public class MailService {
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to, String subject, String content) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        // 这段代码什么蠢蛋，为什么setFrom还要重新set一次yml文件中配置过的值，不配或者配成不一样的都会报错
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}

