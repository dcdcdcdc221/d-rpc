package com.deng.drpc.server;


import com.deng.drpc.RpcApplication;
import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.deng.drpc.registry.LocalRegistry;
import com.deng.drpc.serializer.Serializer;
import com.deng.drpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 *Http请求处理
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        //记录日志
        System.out.println("Received request: " + request.method() + request.uri());

        //处理Http请求(在接受请求的时候处理它)，传入的参数是当接受所有正文后会调用此程序，所以我们可以在里面处理
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RPCRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RPCRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //构造响应结果对象
            RPCResponse rpcResponse = new RPCResponse();
            //如果请求为null，立即返回
            if(rpcResponse ==null){
                rpcResponse.setMessage("rpcResponse is null");
                doResponse(request,rpcResponse,serializer);
                return;
            }
            System.err.println("rpcRequest:" + rpcRequest.toString());
            try{
                //获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                //反射调用方法
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("OK");

            }catch (Exception e){
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            //响应
            doResponse(request, rpcResponse,serializer);
        });
    }

    /**
     * 响应
     * @param request
     * @param response
     * @param serializer
     * @throws IOException
     */
    void doResponse(HttpServerRequest request, RPCResponse response,Serializer serializer){
        //设置响应头，获取响应
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");

        try {
            //序列化
            byte[] serialize = serializer.serialize(response);
            httpServerResponse.end(Buffer.buffer(serialize));
        }catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
