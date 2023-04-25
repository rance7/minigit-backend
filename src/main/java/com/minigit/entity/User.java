package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    private Long id;
    private String accountName;
    private String pwd;
    private String email;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
