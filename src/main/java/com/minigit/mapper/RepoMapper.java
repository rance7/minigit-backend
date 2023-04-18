package com.minigit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.entity.Repo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepoMapper extends BaseMapper<Repo> {
}
