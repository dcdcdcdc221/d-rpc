package com.deng.consumer;

import com.deng.drpc.config.ConfigUtils;
import com.deng.drpc.config.RpcConfig;
import com.deng.drpc.proxy.ServiceProxyFactory;
import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        ServiceProxyFactory serviceProxyFactory = new ServiceProxyFactory();
        // 动态代理
        UserService userService = serviceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("捞王");
        //调用
        User newUser = userService.getUser(user);
        // 调用
            if (newUser != null) {
                System.out.println("我是name啊"+newUser.getName());
            } else {
                System.out.println("user == null");
            }
        long number = userService.getNumber();
        System.out.println(number);

    }
}
