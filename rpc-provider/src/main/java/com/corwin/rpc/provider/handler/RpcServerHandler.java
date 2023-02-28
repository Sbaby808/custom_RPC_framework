package com.corwin.rpc.provider.handler;

import com.alibaba.fastjson.JSON;
import com.corwin.rpc.common.RpcRequest;
import com.corwin.rpc.common.RpcResponse;
import com.corwin.rpc.provider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 服务端业务处理类
 * 1. 将标有@RpcServer注解的bean缓存
 * 2. 接收客户端请求
 * 3. 根据传递过来的beanName从缓存中查找到对应的bean
 * 4. 解析请求中的方法名称，参数类型，参数信息
 * 5. 反射调用bean的方法
 * 6. 给客户端进行响应
 */
@ChannelHandler.Sharable
@Component
public class RpcServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

    private static final Map SERVICE_INSTANCE_MAP = new ConcurrentHashMap();

    /**
     * 通道读取就绪事件
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        // 接收客户端请求 - 将msg转换RpcRequest对象
        RpcRequest rpcRequest = JSON.parseObject(msg, RpcRequest.class);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setError(e.getMessage());
        }
        // 给客户端进行响应
        channelHandlerContext.writeAndFlush(JSON.toJSONString(rpcResponse));
    }

    /**
     * 业务处理逻辑
     * @return
     */
    public Object handler(RpcRequest rpcRequest) throws InvocationTargetException {
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        if(serviceBean == null) {
            throw new RuntimeException("根据BeanName找不到服务！beanName" + rpcRequest.getClassName());
        }
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // CGLIB反射调用
        FastClass fastClass = FastClass.create(serviceBeanClass);
        FastMethod method = fastClass.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean, parameters);
    }

    /**
     * 将标有@RpcServer注解的bean缓存
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(serviceMap.size() > 0) {
            Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
            for (Map.Entry<String, Object> item : entries) {
                Object serviceBean = item.getValue();
                if(serviceBean.getClass().getInterfaces().length == 0) {
                    throw new RuntimeException("服务必须实现接口！");
                }
                // 默认取第一个接口作为缓存bean的名称
                SERVICE_INSTANCE_MAP.put(serviceBean.getClass().getInterfaces()[0].getName(), serviceBean);
            }
        }
    }
}
