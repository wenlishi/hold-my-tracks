package com.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.track.entity.User;
import com.track.mapper.UserMapper;
import com.track.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // @Autowired
    // private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return this.count(queryWrapper) > 0;
        
    }

    @Override
    public User register(User user) {
        // 1. 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 2. 设置默认状态 (保持不变)
        user.setStatus(1);

        // 3.使用 Service 层的 save 方法
        this.save(user);

        // 注意：MyBatis-Plus 会自动把生成的 ID 回填到 user 对象中
        // 所以这里直接返回 user 就能拿到 ID 了
        return user;
    }
}