package com.deng.drpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.deng.drpc.RpcApplication;
import com.deng.drpc.config.RpcConfig;
import com.deng.drpc.constant.RpcConstant;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.model.ServiceMetaInfo;
import com.deng.drpc.registry.Registry;
import com.deng.drpc.registry.RegistryFactory;
import com.deng.drpc.serializer.JdkSerializer;
import com.deng.drpc.serializer.Serializer;
import com.deng.drpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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
        String serviceName = method.getDeclaringClass().getName();
        RPCRequest rpcRequest = RPCRequest.builder()
                .ServiceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            assert serializer != null;
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            //从注册中心获取服务提供者地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException("暂无服务地址");
            }
            //暂时取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
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
