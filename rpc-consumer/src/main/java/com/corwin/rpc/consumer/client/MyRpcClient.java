package com.corwin.rpc.consumer.client;

import com.corwin.rpc.consumer.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 客户端
 * 1. 连接Netty客户端
 * 2. 提供给调用者主动关闭资源的方法
 * 3. 提供消息发送的方法
 */
public class MyRpcClient {

    private NioEventLoopGroup group;
    private Channel channel;

    private String ip;
    private int port;

    private RpcClientHandler rpcClientHandler = new RpcClientHandler();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MyRpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        initClient();
    }

    /**
     * 初始化方法-连接Netty服务器
     */
    public void initClient() {
        try {
            // 1. 创建线程组
            group = new NioEventLoopGroup();
            // 2. 创建启动助手
            Bootstrap bootstrap = new Bootstrap();
            // 3. 设置参数
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(rpcClientHandler);
                        }
                    });

            channel = bootstrap.connect(ip, port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
            if(channel != null) {
                channel.close();
            }
            if(group != null) {
                group.shutdownGracefully();
            }
        }
    }

    /**
     * 提供给调用者主动关闭资源的方法
     */
    public void close() {
        if(channel != null) {
            channel.close();
        }
        if(group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * 消息发送
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        rpcClientHandler.setRequestMsg(msg);
        Future submit = executorService.submit(rpcClientHandler);
        return submit.get();
    }
}
