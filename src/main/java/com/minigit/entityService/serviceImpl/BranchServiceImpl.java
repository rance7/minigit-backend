package com.minigit.entityService.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.entity.Branch;
import com.minigit.mapper.BranchMapper;
import com.minigit.entityService.BranchService;
import org.springframework.stereotype.Service;

@Service
public class BranchServiceImpl extends ServiceImpl<BranchMapper, Branch> implements BranchService {
}
