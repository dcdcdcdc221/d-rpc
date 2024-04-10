package com.deng.drpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.serializer.JdkSerializer;
import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //序列化
        JdkSerializer serializer = new JdkSerializer();

        //发请求
        RPCRequest getUser = RPCRequest.builder()
                .ServiceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{method.getName()})
                .build();
        try {
            byte[] bodyBytes = serializer.serialize(getUser);
            byte[] bytes;
//            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
//                    .body(bodyBytes)
//                    .execute()){
//                bytes = httpResponse.bodyBytes();
//            }
            //todo 这里地址是硬编码(需要注册中心和服务发现机制解决)
            System.out.println(bodyBytes.toString());
            try(HttpResponse response = HttpRequest.post("http://localhost:8081")
                    .body(bodyBytes)
                    .execute()){
                bytes = response.bodyBytes();
                System.err.println(response);
            }
//            RPCResponse response = serializer.deserialize(bytes, RPCResponse.class);
            return serializer.deserialize(bytes, RPCResponse.class).getData();
//            return (User) serializer.deserialize(bytes, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
