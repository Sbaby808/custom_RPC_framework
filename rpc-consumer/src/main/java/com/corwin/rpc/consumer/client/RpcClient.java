package com.corwin.rpc.consumer.client;

import com.corwin.rpc.consumer.cache.ServerListCache;
import com.corwin.rpc.consumer.cache.ServerVO;
import com.corwin.rpc.consumer.handler.RpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 9:48:50
 */
@Slf4j
public class RpcClient {

    private EventLoopGroup group;

    private Channel channel;

    private String ip;

    private int port;

    public ServerVO getServerVO() {
        return serverVO;
    }

    private ServerVO serverVO;

    private RpcClientHandler rpcClientHandler = new RpcClientHandler();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        initClient();
    }

    /**
     * 基于权重的随机负载均衡（默认权重相同）
     * @return
     */
    public RpcClient() {
        this.serverVO = ServerListCache.randomOne();
        this.ip = serverVO.getIp();
        this.port = serverVO.getPort();
        initClient();
    }

    public void initClient() {
        try {
            group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
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
        } catch (Exception e) {
            e.printStackTrace();
            if(channel != null) {
                channel.close();
            }
            if(group != null) {
                group.shutdownGracefully();
            }
        } finally {
            log.info("结束一次连接");
        }
    }

    public void close() {
        if(channel != null) {
            channel.close();
        }
        if(group != null) {
            group.shutdownGracefully();
        }
    }

    public Object send(String msg) throws ExecutionException, InterruptedException {
        rpcClientHandler.setRequestMsg(msg);
        Future submit = executorService.submit(rpcClientHandler);
        return submit.get();
    }
}
