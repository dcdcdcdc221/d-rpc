package com.deng.consumer;

import com.deng.drpc.config.ConfigUtils;
import com.deng.drpc.config.RpcConfig;
import com.deng.drpc.proxy.ServiceProxyFactory;
import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("捞王");
        System.out.println("User类型：" + user.getClass());
        System.out.println("UserService类型_1" + userService.getClass());
        //调用
        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println("hhaa" + newUser.getName());
        }else {
            System.out.println("user == null");
        }
       // long number = userService.getNumber();
        //System.out.println(number);;
    }
}