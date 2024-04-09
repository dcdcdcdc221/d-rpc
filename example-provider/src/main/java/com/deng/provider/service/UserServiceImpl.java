package com.deng.provider.service;

import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;

public class UserServiceImpl implements UserService {
    public User getUser(User user) {
        System.out.println("用户名:"+user.getName());
        return user;
    }
}
