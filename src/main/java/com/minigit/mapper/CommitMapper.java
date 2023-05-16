package com.minigit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.common.R;
import com.minigit.entity.Commit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommitMapper extends BaseMapper<Commit> {
    @Select("select * from user  \n" +
            "     inner join repo on user.id = repo.author_id  \n" +
            "     inner join branch on branch.repo_id = repo.id  \n" +
            "     inner join commit on commit.branch_id = branch.id \n" +
            "     order by commit.create_time ASC;")
    List<Commit> getAllCommits(String userName, String repoName, String branchName);
}
