package com.minigit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class pathController {

    @GetMapping("/{user}/{repo}")
    public String getRepoPath(@PathVariable String user, @PathVariable String repo){
        return user + "/" + repo;
    }

    @GetMapping("/{user}/{repo}/tree/{branch}")
    public String getBranchPath(@PathVariable String user, @PathVariable String repo, @PathVariable String branch){
        return user + "/" + repo + "/tree/" + branch;
    }

    @GetMapping("/{user}/{repo}/blob/{branch}/{filepath:.+}")
    public String getFilePath(@PathVariable String user, @PathVariable String repo, @PathVariable String branch,
                              @PathVariable String filepath){
        return user + "/" + repo + "/blob/" + branch + filepath;
    }

    @GetMapping("/{user}/{repo}/tree/{branch}/{filepath:.+}")
    public String getDirPath(@PathVariable String user, @PathVariable String repo, @PathVariable String branch,
                              @PathVariable String filepath){
        return user + "/" + repo + "/tree/" + branch + filepath;
    }
}
