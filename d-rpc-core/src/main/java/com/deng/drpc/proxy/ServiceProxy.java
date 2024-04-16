package com.deng.drpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.deng.drpc.RpcApplication;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.serializer.JdkSerializer;
import com.deng.drpc.serializer.Serializer;
import com.deng.drpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 服务代理（JDK 动态代理）
 *
 * @author cong
 * @date 2024/03/07
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @param proxy  代理
     * @param method 方法
     * @param args   参数
     * @return {@link Object}
     * @throws Throwable 可投掷
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        RPCRequest rpcRequest = RPCRequest.builder()
                .ServiceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            assert serializer != null;
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            // todo 注意，这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
            try (HttpResponse httpResponse = HttpRequest.post("http://"+ RpcApplication.getRpcConfig().getServerHost()+":"+ RpcApplication.getRpcConfig().getServerPort())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RPCResponse rpcResponse = serializer.deserialize(result, RPCResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            log.error("IOException", e);
        }

        return null;
    }
}
