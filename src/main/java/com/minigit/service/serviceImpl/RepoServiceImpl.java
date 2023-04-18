package com.minigit.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.entity.Repo;
import com.minigit.mapper.RepoMapper;
import com.minigit.service.RepoService;
import org.springframework.stereotype.Service;

@Service
public class RepoServiceImpl extends ServiceImpl<RepoMapper, Repo> implements RepoService {
}
