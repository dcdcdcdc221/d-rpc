package com.deng.consumer;

import com.deng.drpc.proxy.ServiceProxyFactory;
import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        //todo 需要获取UserService的实现类对象
        ServiceProxyFactory serviceProxyFactory = new ServiceProxyFactory();
        UserService userService = serviceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("捞王");
        //调用，获取user对象
        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println(newUser.getName());
        }else {
            System.out.println("user == null");
        }
    }
}
