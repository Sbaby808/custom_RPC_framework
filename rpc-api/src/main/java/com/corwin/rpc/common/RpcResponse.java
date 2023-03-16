package com.corwin.rpc.common;

import lombok.Data;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 9:31:59
 */
@Data
public class RpcResponse {

    private String requestId;
    private String error;
    private Object result;
}
