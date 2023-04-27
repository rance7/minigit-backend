package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.common.R;
import com.minigit.entity.Branch;
import com.minigit.entity.Commit;
import com.minigit.entity.Repo;
import com.minigit.entity.User;
import com.minigit.entityService.BranchService;
import com.minigit.entityService.CommitService;
import com.minigit.entityService.RepoService;
import com.minigit.entityService.UserService;
import com.minigit.util.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/{user}/{repo}/{branch}")
public class CommitAndPushController {
    @Autowired
    private CommitService commitService;
    @Autowired
    private UserService userService;
    @Autowired
    private RepoService repoService;
    @Autowired
    private BranchService branchService;

    @GetMapping("/add")
    public R<String> add(@PathVariable String repo,@PathVariable String branch,
                         @RequestBody List<String> filePaths, HttpSession session){
        List<File> files = new ArrayList<>();
        for (String filePath : filePaths) {
            files.add(new File(filePath));
        }
        GitUtils.add(files);
        return R.success("add成功！");
    }

    /**
     * commit
     * @param map   map中保存着message
     * @return
     */
    @PostMapping("/commit")
    public R<Commit> commit(@PathVariable String repo,@PathVariable String branch,
                            @RequestBody Map map, HttpSession session) throws NoSuchAlgorithmException {
        String message = (String) map.get("message");
        Long committerId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, committerId);
        User user = userService.getOne(queryWrapper);
        String committer = user.getAccountName();
        Commit commit = GitUtils.commit(message,committer);

        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, committerId).eq(Repo::getName, repo);
        Repo repo1 = repoService.getOne(queryWrapper1);

        LambdaQueryWrapper<Branch> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Branch::getRepoId,repo1.getId()).eq(Branch::getName, branch);
        Branch branch1 = branchService.getOne(queryWrapper2);

        commit.setBranchId(branch1.getId());
        return R.success(commit);
    }

    @GetMapping("/push")
    public R<String> push(@PathVariable String repo,@PathVariable String branch, HttpSession session){
        Long authorId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, authorId);
        User user = userService.getOne(queryWrapper);

        LambdaQueryWrapper<Repo> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repo);
        Repo repo1 = repoService.getOne(queryWrapper1);

        LambdaQueryWrapper<Branch> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Branch::getRepoId,repo1.getId()).eq(Branch::getName, branch);
        Branch branch1 = branchService.getOne(queryWrapper2);

        LambdaQueryWrapper<Commit> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Commit::getBranchId,branch1.getId());
        List<Commit> list = commitService.list(queryWrapper3);

        GitUtils.push(list);
        return R.success("推送成功！");
    }


}
