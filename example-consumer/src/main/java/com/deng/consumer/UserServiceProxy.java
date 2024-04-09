package com.deng.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.serializer.JdkSerializer;
import com.deng.example.common.model.User;
import com.deng.example.common.service.UserService;



import java.io.IOException;

public class UserServiceProxy  implements UserService {
    @Override
    public User getUser(User user) {
        //序列化
        JdkSerializer serializer = new JdkSerializer();

        //发请求
        RPCRequest getUser = RPCRequest.builder()
                .ServiceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            byte[] bodyBytes = serializer.serialize(getUser);
            byte[] bytes;
//            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
//                    .body(bodyBytes)
//                    .execute()){
//                bytes = httpResponse.bodyBytes();
//            }
            try(HttpResponse response = HttpRequest.post("http://localhost:8081")
                    .body(bodyBytes)
                    .execute()){
                bytes = response.bodyBytes();
            }
//            RPCResponse response = serializer.deserialize(bytes, RPCResponse.class);
            return (User) serializer.deserialize(bytes, RPCResponse.class).getData();
//            return (User) serializer.deserialize(bytes, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
