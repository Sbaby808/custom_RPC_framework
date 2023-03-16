package com.corwin.rpc.provider;

import com.corwin.rpc.provider.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 18:38:14
 */
@SpringBootApplication
public class ServerBootStrapApplication implements CommandLineRunner {

    @Autowired
    RpcServer rpcServer;

    public static void main(String[] args) {
        SpringApplication.run(ServerBootStrapApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rpcServer.startServer("127.0.0.1", 8899, "127.0.0.1:2181");
            }
        }).start();
    }
}
