package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Branch implements Serializable {
    private String name;
    private Long repoId;
    private String author;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime modifyTime;
    private String headHash;
    @TableField(exist = false)
    private List<Commit> commitHistory;

}

