package com.corwin.rpc.common;

import lombok.Data;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 9:31:52
 */
@Data
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
