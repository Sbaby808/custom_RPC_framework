package com.corwin.rpc.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.corwin.rpc.common.RpcRequest;
import com.corwin.rpc.common.RpcResponse;
import com.corwin.rpc.consumer.ClientBootStrap;
import com.corwin.rpc.consumer.cache.ServerVO;
import com.corwin.rpc.consumer.client.RpcClient;
import org.apache.curator.framework.CuratorFramework;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 10:32:47
 */
public class RpcClientProxy {

    public static Object createProxy(Class serviceClass) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setParameters(args);
                        long beginTime = System.currentTimeMillis();
                        RpcClient rpcClient = new RpcClient();
                        try {
                            Object responseMsg = rpcClient.send(JSON.toJSONString(rpcRequest));
                            RpcResponse rpcResponse = JSON.parseObject(responseMsg.toString(), RpcResponse.class);
                            if(rpcResponse.getError() != null) {
                                throw new RuntimeException(rpcResponse.getError());
                            }
                            Object result = rpcResponse.getResult();
                            // 更新每个服务端最后一次响应时间
                            updateLastResponseTime(beginTime, System.currentTimeMillis(), rpcClient.getServerVO());
                            return JSON.parseObject(result.toString(), method.getReturnType());
                        } catch (Exception e) {
                            throw e;
                        } finally {
                            rpcClient.close();
                        }
                    }
                });
    }

    /**
     * 更新服务端最后一次响应时间
     * @param serverVO
     */
    public static void updateLastResponseTime(Long beginTime, Long endTime, ServerVO serverVO) throws Exception {
        CuratorFramework client = ClientBootStrap.ac.getBean(CuratorFramework.class);
        client.setData()
                .forPath("/custom_rpc/server/" + serverVO.getIp() + ":" + serverVO.getPort(),
                        (endTime - beginTime + "," + endTime).getBytes());

    }
}
