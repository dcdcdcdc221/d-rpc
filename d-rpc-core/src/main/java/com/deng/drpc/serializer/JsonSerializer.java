package com.deng.drpc.serializer;

import com.deng.drpc.model.RPCRequest;
import com.deng.drpc.model.RPCResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON序列化器
 */
public class JsonSerializer implements Serializer{
    /**
     * Jackson绑定对象
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, type);
        if(obj instanceof RPCRequest){
            return handleRequest((RPCRequest) obj,type);
        }
        if(obj instanceof RPCResponse){
            return handleResponse((RPCResponse) obj,type);
        }
        return obj;
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被作为LinkedHashMap无法转换成原始对象，因此这里做了特殊处理
     * @param rpcRequest
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    private <T> T handleRequest(RPCRequest rpcRequest,Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();
        //循环处理每个参数类型
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            //如果类型不同，就处理一下类型
            if(!clazz.isAssignableFrom(args[i].getClass())){
                byte[] asBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(asBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     *由于Object的原始对象会被擦除，导致反序列化时会被作为LinkedHashMap无法转换成原始对象，因此这里做了特殊处理
     * @param rpcResponse
     * @param type
     * @return
     * @param <T>
     * @throws Exception
     */
    private <T> T handleResponse(RPCResponse rpcResponse,Class<T> type) throws IOException{
        //处理响应数据
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse);
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, type));
        return type.cast(rpcResponse);
    }
}
