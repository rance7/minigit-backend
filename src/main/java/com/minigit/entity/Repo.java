package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Repo implements Serializable {
    private Long id;
    private String author;
    private String name;
    private boolean isPublic;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    @TableField(exist = false)
    private List<Branch> branches;

}
