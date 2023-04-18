package com.minigit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.entity.Branch;
import org.apache.ibatis.annotations.Mapper;

@Mapper  // 加入注解并继承后常见的增删改查方法就有了
public interface BranchMapper extends BaseMapper<Branch> {
}
