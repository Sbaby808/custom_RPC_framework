package com.corwin.rpc.provider.server;

import com.corwin.rpc.provider.handler.RpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 启动类
 * 这里原来取名RpcServer失败了，不知道为啥
 */
@Service
public class MyRpcServer implements DisposableBean {

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    RpcServerHandler rpcServerHandler;

    public void startServer(String ip, int port) {
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
                            // 添加String的编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            // 添加业务处理类
                            pipeline.addLast(rpcServerHandler);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(ip, port).sync();
            System.out.println("----------服务端启动成功----------");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
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
}
