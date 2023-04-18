package com.minigit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.entity.Commit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommitMapper extends BaseMapper<Commit> {
}
