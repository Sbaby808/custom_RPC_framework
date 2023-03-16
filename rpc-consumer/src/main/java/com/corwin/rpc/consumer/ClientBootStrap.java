package com.corwin.rpc.consumer;

import com.corwin.rpc.consumer.cache.ServerListCache;
import com.corwin.rpc.consumer.cache.ServerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 11:07:11
 */
@Slf4j
@SpringBootApplication
public class ClientBootStrap implements CommandLineRunner {

    public static ConfigurableApplicationContext ac;

    @Autowired
    CuratorFramework client;

    public static void main(String[] args) {
        ac = SpringApplication.run(ClientBootStrap.class, args);
    }

    /**
     * 客户端启动时从Zookeeper中获取所有服务提供端节点信息，客户端与每一个服务端都建立连接
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String path = "/custom_rpc/server";
                    PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
                    pathChildrenCache.getListenable().addListener((client1, event) -> {
                        PathChildrenCacheEvent.Type type = event.getType();
                        log.info("事件类型：" + type);
                        ChildData data = event.getData();
                        log.info("子节点数据：" + data);
                        if(type.equals(PathChildrenCacheEvent.Type.CHILD_ADDED) || type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                            String[] split = data.getPath().substring(19).split(":");
                            ServerVO serverVO = ServerVO.builder()
                                    .ip(split[0])
                                    .port(Integer.parseInt(split[1])).build();
                            switch (type) {
                                case CHILD_ADDED:
                                    ServerListCache.add(serverVO);
                                    break;
                                case CHILD_REMOVED:
                                    ServerListCache.remove(serverVO);
                                    break;
                            }
                        }
                    });
                    pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
