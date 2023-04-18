package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Commit implements Serializable {
    private Long id;
    private String repo_id;
    private String branchName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime commitTime;
    private String committer;
    private String message;
    private String hash;
    private String parentHash;



}
