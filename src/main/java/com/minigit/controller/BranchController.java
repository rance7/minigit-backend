package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.common.R;
import com.minigit.entity.Branch;
import com.minigit.entity.Repo;
import com.minigit.entityService.BranchService;
import com.minigit.entityService.RepoService;
import com.minigit.entityService.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/{userName}/{repoName}")
public class BranchController {
    @Autowired
    private BranchService branchService;
    @Autowired
    private UserService userService;
    @Autowired
    private RepoService repoService;


    @PostMapping("/add")
    public R<Branch> addBranch(@PathVariable String userName, @PathVariable String repoName,@RequestParam String branchName,
                               @RequestBody Branch sourceBranch, HttpSession session){
        Long authorId = (Long) session.getAttribute("user");
        Long repoId = sourceBranch.getRepoId();
        System.out.println(sourceBranch);
        System.out.println(repoId);
        Branch branch = new Branch();
        branch.setName(branchName);
        branch.setRepoId(sourceBranch.getRepoId());
        branch.setAuthorId(authorId);
        branch.setCommitHash(sourceBranch.getCommitHash());
        branchService.save(branch);
        return R.success(branch);
    }

    @GetMapping("/branches")
    public R<List<Branch>> getAllBranches(@PathVariable String userName, @PathVariable String repoName,HttpSession session){
        Long authorId = (Long) session.getAttribute("user");
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Repo::getAuthorId, authorId).eq(Repo::getName, repoName);
        Repo repo1 = repoService.getOne(queryWrapper);

        LambdaQueryWrapper<Branch> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Branch::getRepoId,repo1.getId());
        List<Branch> list = branchService.list(queryWrapper1);
        return R.success(list);
    }

}
