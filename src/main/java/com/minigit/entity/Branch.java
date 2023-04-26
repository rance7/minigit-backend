package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Branch implements Serializable {
    private Long id;
    private String name;
    private Long repoId;
    private Long authorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private String headHash;

}

