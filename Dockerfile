FROM openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制 jar 包到容器中
COPY target/spring-1.0-SNAPSHOT.jar app.jar

# 启动容器时执行的命令
ENTRYPOINT ["java","-jar","app.jar"]

