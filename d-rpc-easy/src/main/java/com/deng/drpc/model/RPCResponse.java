package com.deng.drpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RPCResponse implements Serializable {

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 响应数据（预留）
     */
    private Class<?> dataType;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 异常信息
     */
    private Exception exception;

}
