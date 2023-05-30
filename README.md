## 1 项目背景

GitHub作为全球最大的代码托管平台，不仅提供了代码仓库的管理和版本控制，还为开发者提供了强大的协作和社交功能。
 但是由于网络等各种原因，我们在使用github管理仓库时可能会遇到无法推送和拉取等问题。另外，github是基于git管理代码仓库的，我们需要结合使用git和github才能完成对代码仓库的管理。除此之外，github作为企业级的代码托管平台，对于初级程序员来说，许多配置和命令看上去比较冗余。

基于以上背景，我们想要做一个轻量级的代码管理平台，结合了git和github的功能，可以在页面上直接操作本地仓库，执行add、commit、back等命令。也可以在页面上查看远程仓库的内容等。

## 2 Git原理

Git 是 Linus 在写 Linux 的时候顺便写出来的，用于对 Linux 进行版本管理，Git最核心的功能就是版本控制。

每次初始化一个仓库，会出现一个.git文件夹。

![image-20230530204906447](markdown-img/README.assets/image-20230530204906447.png)

### 1.1 缓冲区

![image-20230530204919347](markdown-img/README.assets/image-20230530204919347.png)

### 2.1 .minigit文件

![image-20230530204944074](markdown-img/README.assets/image-20230530204944074.png)

### 2.2 版本树

![image-20230530204958328](markdown-img/README.assets/image-20230530204958328.png)

### 2.3 版本链

![image-20230530205004018](markdown-img/README.assets/image-20230530205004018.png)

### 2.4 objects数据库

objects数据库中保存着所有历史文件，通过读取文件内容并计算一个40位的hash值，以前两位作为文件夹名，以后38位作为文件名，一旦文件内容发生改变，文件的哈希值就会发生改变。

objects数据库中保存着三种文件：

- commit文件：保存着commit信息
- tree文件：一个tree是一个目录，保存着目录下的文件类型、文件路径、文件hash值
- blob文件：一个blob文件是一个普通文件，保存着文件内容

![image-20230530205442128](markdown-img/README.assets/image-20230530205442128.png)

## 3 实现

