package com.deng.consumer;

import com.deng.drpc.config.ConfigUtils;
import com.deng.drpc.config.RpcConfig;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}