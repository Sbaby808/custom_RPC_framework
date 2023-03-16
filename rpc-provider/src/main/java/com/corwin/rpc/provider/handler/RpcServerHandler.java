package com.corwin.rpc.provider.handler;

import com.alibaba.fastjson.JSON;
import com.corwin.rpc.common.RpcRequest;
import com.corwin.rpc.common.RpcResponse;
import com.corwin.rpc.provider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 18:40:59
 */
@Component
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {
    
    private static final Map SERVICE_INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 读就绪事件
     * @param channelHandlerContext
     * @param s
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        RpcRequest rpcRequest = JSON.parseObject(s, RpcRequest.class);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setError(e.getMessage());
        }
        channelHandlerContext.writeAndFlush(JSON.toJSONString(rpcResponse));
    }

    /**
     * 业务处理
     * @param rpcRequest
     */
    public Object handler(RpcRequest rpcRequest) throws InvocationTargetException {
        System.out.println("8898开始业务处理！");
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        if(serviceBean == null) {
            throw new RuntimeException("找不到bean服务，beanName：" + rpcRequest.getClassName());
        }
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Object[] parameters = rpcRequest.getParameters();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        FastClass fastClass = FastClass.create(serviceBeanClass);
        FastMethod method = fastClass.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 将所有注解了@RpcServer的service放入map
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(serviceMap.size() > 0) {
            Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                Object serviceBean = entry.getValue();
                if(serviceBean .getClass().getInterfaces().length == 0) {
                    throw new RuntimeException("服务必须实现接口");
                }
                String name = serviceBean.getClass().getInterfaces()[0].getName();
                SERVICE_INSTANCE_MAP.put(name, serviceBean);
            }
        }
    }
}
