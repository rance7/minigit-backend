package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Repo implements Serializable {
    private Long id;
    private Long authorId;
    private String name;
    private Boolean isPublic;
    private String path;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
