package com.minigit.util;

import lombok.Data;

import java.util.List;

@Data
public class TreeEntry {
    private String path;
    private String hash;
    private EntryType entryType;

    private Status status;

    public TreeEntry() {
    }

    public TreeEntry(String path, String hash, EntryType entryType) {
        this.path = path;
        this.hash = hash;
        this.entryType = entryType;
        this.status = Status.untracked;
    }

    public enum EntryType{
        blob, tree
    }

    public enum Status {
        untracked,   //未追踪
        unmodified,  // 文件没有被修改过，当前工作目录中的文件和Git仓库中的一样
        modified,    // 文件被修改过，但是还没有被提交到Git仓库
        deleted,     // 文件已经被删除，但是还没有被提交到Git仓库

        //staged,      // 已暂存，文件已经被添加到了Git的暂存区，等待被提交到仓库
        //renamed,     // 文件已经被重命名，但是还没有被提交到Git仓库
        //stagedAndModified   // 文件被修改后已添加到暂存区，但是又被修改了
    }
}
