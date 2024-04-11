package com.deng.provider;

import com.deng.drpc.RpcApplication;
import com.deng.drpc.registry.LocalRegistry;
import com.deng.drpc.server.VertxHttpServer;
import com.deng.example.common.service.UserService;
import com.deng.provider.service.UserServiceImpl;

public class EasyProviderExample {
    public static void main(String[] args) {
        RpcApplication.init();
        //注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        //启动Web服务
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
//        vertxHttpServer.doStart(8081);
    }
}
