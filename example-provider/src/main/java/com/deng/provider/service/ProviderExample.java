package com.deng.provider.service;

import com.deng.drpc.RpcApplication;
import com.deng.drpc.config.RegistryConfig;
import com.deng.drpc.config.RpcConfig;
import com.deng.drpc.model.ServiceMetaInfo;
import com.deng.drpc.registry.LocalRegistry;
import com.deng.drpc.registry.Registry;
import com.deng.drpc.registry.RegistryFactory;
import com.deng.drpc.server.HttpServer;
import com.deng.drpc.server.VertxHttpServer;
import com.deng.example.common.service.UserService;

/**
 * 使用注册中心版本
 */
public class ProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
