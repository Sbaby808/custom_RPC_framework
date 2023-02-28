package com.corwin.rpc.consumer;

import com.corwin.rpc.api.IUserService;
import com.corwin.rpc.consumer.proxy.RpcClientProxy;
import com.corwin.rpc.pojo.User;

/**
 * 测试类
 */
public class ClientBootstrap {
    public static void main(String[] args) {
        IUserService proxy = (IUserService) RpcClientProxy.createProxy(IUserService.class);
        User user = proxy.getById(2);
        System.out.println(user);
    }
}
