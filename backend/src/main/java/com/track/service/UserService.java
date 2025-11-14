package com.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.track.entity.User;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User register(User user);
}