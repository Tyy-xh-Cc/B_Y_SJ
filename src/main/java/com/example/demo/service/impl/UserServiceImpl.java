package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        Long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
        return user;
    }
}