## 四月初启动项目



## 四月中旬完成add、commit功能的实现



## 4.22

#### 修改commit功能的实现

- 修改了commit会计算实际目录所有文件（即管理了不被minigit管理的文件）hash并写入object数据库的bug。

- 把对空文件夹的处理放到预处理（用户传入目录时即在空目录下生成一个.gitkeep文件）中，而不是在生成版本树时进行，因为这样做的代价太大。
- 目前writeObject方法和calculateDirSha1方法中最前面的处理有点多余，因为唯一调用了他们的方法就是writeTree，这个方法中也做了错误处理，但是为了可读性还是保留了。

#### 版本回退功能的第一版开发

- 对于版本回退，我的做法通过比较当前commitTree和要回溯的版本的commitTree是生成一个deleteMap和createMap，分别保存着实际目录中需要删除的文件和需要创建的文件
- 在实际目录中删除deleteMap中的文件，创建createMap中的文件，再比较当前目录中的文件的hash是否改变，判断是否需要写入或者覆盖文件中的内容（从object文件中读出内容）。
- 只删除文件而不删除目录，因为我们不知道在要回退的那个版本中，目录下是否有其他文件没有被minigit管理，所以我们只删除文件，不删除目录。

## 4.25

- 实现了邮箱服务功能，暂时使用自己的邮箱，不过一分钟只能发一条消息。
- 完成了注册和登录功能并通过测试。

## 4.26

- 对数据库中的表做了一些修改，例如repo表中加了一个path
- 我希望用户访问的url类似于github的**"/{user}/{repo}/blob/{branch}/{filepath:.+}"**，为了实现这个功能，做了一些调查，把原本实现的功能做了一些修改。
- 添加了pathController和repoController等类，目前repoController中已经完成了一部分，但是还没有测试。

## 4.27

- 删除了commit表中的repo_id字段，因为根据branch_id就可以找到repo_id。
- 保留了branch表中的author_id字段，因为branch的创建者不一定是repo的创建者
- repoController中的功能已基本完成并且已经通过测试。
- 修改了controller中的访问路径，未来可能会继续修改。

![image-20230427112817713](markdown-img/DevelpmentHistory.assets/image-20230427112817713.png)

- 计划修改数据库，增加外键（为了能够级联删除和级联更新），目前的数据库存在问题，如删除仓库，但是branch和commit表不会改变
- 删除AddUtils，将add方法转移到GitUtils中
- 增加branchController，CommitAndPushController和FileController
- BranchController中完成了addBranch方法和查询所有分支的方法，并通过测试
- commitAndPushController中完成了add、commit和push方法，但是还没有测试

## 4.28

- 对项目结构做了很大的修改：将Git相关操作的Util类删除，添加了GitService等类。区别在于，util-version是本地测试的版本，每次都要做init获得项目路径。而service-version中，只需要访问数据库获得repo的path。
- 修改后的add、commit、back等方法已经通过测试
- 现在的项目中允许空提交，也就缓冲区没有任何文件的空提交，提交不会产生问题。但是如果我们在一个空仓库中做一个空提交，那么下一次提交在读取上一次提交的时候，会出现问题，暂时还没有做这方面的错误处理。

## 4.29

### 1. 对于push该怎么处理？

应该将所有objects文件夹中的文件全部推送到远程仓库，这样用户就能方便的检出某个分支。

**这样应该是没有必要的，因为其他用户只能查看某个分支最新的提交版本的文件内容，所以远程仓库只保存当前分支最新的版本树的文件目录即可，另外，如果保存所有objects文件，用户访问某个分支的某个目录的URL是固定的，但是去远程服务器找到这个路径是十分费力的**

### 2. 那么检出的分支的信息，如何返回给用户呢？

检出一个分支时，用户点击这个分支，前端向后端发送消息查询这个分支的commitHash，然后后端再根据这个commitHash找到treeHeadHash，treeHeadHash的hash对应的object文件中保存着文件类型、路径和hash，如下图所示：

![image-20230429101736560](markdown-img/DevelpmentHistory.assets/image-20230429101736560.png)

所以后端最后返回给用户的数据有三个：文件类型、文件名和hash值。

**以上作废**

### 3. 用户进入某个文件怎么办

直接到远程服务器拉取文件信息。

### 今日成果

- 完成了push的功能并完成测试！

- 将UploadUtil转为了UploadSerivce，因为静态变量没办法自动注入，如果使用普通变量和方法，就不如直接注册为service。

- 现在项目的整个架构有一些问题，对于本地git来说，使用绝对路径，但是传到远端需要把仓库路径剪掉；如果使用相对路径，那么用户在操作本地仓库文件时，又需要每次都加上仓库的路径。网页端的minigit相比本地git是有一些弊端的，本地的git每次都在某个文件夹内打开，路径是自动获取的，而网页端每次需要到数据库去查路径，用户如果把仓库复制或者剪切到其他位置会出问题。

- 另外绝对路径的另一个问题是，如果不属于仓库的上层目录名发生改变，那么整个文件目录中所有文件的hash都会改变，所以之后最好选择相对路径的方式。

- 对于replace的处理

  ```
  channelSftp.put(file.getAbsolutePath(), branchPath + s[1].replaceFirst(repoPath
                                  .replace("\\", "\\\\"),"")
                                  .replaceAll("\\\\","/"));
  ```

  由于转义字符和Linux服务器的路径分隔符不一致，所以要加一些特殊的处理。

  