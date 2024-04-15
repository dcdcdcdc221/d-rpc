package com.deng.drpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.deng.drpc.RpcApplication;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.serializer.JdkSerializer;
import com.deng.drpc.serializer.Serializer;
import com.deng.drpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {


    //动态URL
    //因为是静态方法，所以设置一次值后就处处调用相等。
    private static final String URI = "http://"+ RpcApplication.getRpcConfig().getServerHost()
            + ":"+RpcApplication.getRpcConfig().getServerPort();
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            // 动态处理url
            try (HttpResponse httpResponse = HttpRequest.post(URI)
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                System.err.println(RpcApplication.getRpcConfig().getVersion());
                // 反序列化
                RPCResponse rpcResponse = serializer.deserialize(result, RPCResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //log.error("IOException", e);
        }

        return null;
    }
}
