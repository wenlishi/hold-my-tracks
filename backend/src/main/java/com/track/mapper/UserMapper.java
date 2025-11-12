package com.track.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.track.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}