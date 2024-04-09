package com.deng.drpc.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RPCRequest implements Serializable {
    /**
     * 服务名称
     */
    private String ServiceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型列表
     */
    private Class<?> parameterTypes;

    /**
     * 参数列表
     */
    private Object args;
}
