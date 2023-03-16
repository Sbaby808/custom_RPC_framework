package com.corwin.rpc.provider.server;

import com.corwin.rpc.provider.handler.RpcServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 10:23:31
 */
@Service
public class RpcServer implements DisposableBean {

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    RpcServerHandler rpcServerHandler;

    public void startServer(String ip, int port, String zookeeperUrl) {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(rpcServerHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(ip, port).sync();
            registerZookeeper(ip, port, zookeeperUrl);
            System.out.println("Provider服务启动成功！");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if(workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 启动服务端，将ip和端口信息注册到zookeeper上
     * @param ip
     * @param port
     */
    public void registerZookeeper(String ip, int port, String zookeeperUrl) throws Exception {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperUrl)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(retryPolicy)
                .namespace("custom_rpc")
                .build();
        client.start();
        System.out.println("Zookeeper session establish!");

        String path = "/server/" + ip + ":" + port;
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, null);
    }
}
