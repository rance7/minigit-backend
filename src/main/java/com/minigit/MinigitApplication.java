package com.minigit;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j // 自动添加get、set方法，提供日志功能
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
public class MinigitApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinigitApplication.class, args);
        log.info("项目启动成功！！！！！！！！！！！！");
    }
}
