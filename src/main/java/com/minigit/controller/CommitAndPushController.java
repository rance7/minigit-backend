package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.common.R;
import com.minigit.entity.Commit;
import com.minigit.entity.User;
import com.minigit.entityService.CommitService;
import com.minigit.entityService.UserService;
import com.minigit.util.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/commit")
public class CommitController {
    @Autowired
    private CommitService commitService;
    @Autowired
    private UserService userService;

    /**
     * commit
     * @param map   map中保存着message
     * @param session   session中保存着user，branch，repo
     * @return
     */
    @RequestMapping()
    public R<Commit> commit(@RequestBody Map map, HttpSession session) throws NoSuchAlgorithmException {
        String message = (String) map.get("message");
        Long committerId = (Long) session.getAttribute("user");
        Long branchId = (Long) session.getAttribute("branch");
        Long repoId = (Long) session.getAttribute("repo");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getId, committerId);
        User user = userService.getOne(queryWrapper);
        String committer = user.getAccountName();
        Commit commit = GitUtils.commit(message, committer);
        commit.setRepoId(repoId);
        commit.setBranchId(branchId);
        commitService.save(commit);
        return R.success(commit);
    }
}
