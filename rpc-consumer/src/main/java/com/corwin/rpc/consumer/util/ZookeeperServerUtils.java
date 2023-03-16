package com.corwin.rpc.consumer.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

/**
 * @author dingsheng
 * @version 1.0
 * @description: curator工具
 * @date 2023/3/16 14:10:59
 */
@Slf4j
@Component
public class ZookeeperServerUtils {

    private volatile static CuratorFramework client;

    private ZookeeperServerUtils() {};

    public static CuratorFramework getClient() {
        if(client == null) {
            synchronized (ZookeeperServerUtils.class) {
                if(client == null) {
                    ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 5);
                    client = CuratorFrameworkFactory.builder()
                            .connectString("127.0.0.1:2181")
                            .sessionTimeoutMs(5000)
                            .connectionTimeoutMs(3000)
                            .retryPolicy(retryPolicy)
                            .build();
                    client.start();
                    log.info("zookeeper session established!");
                    return client;
                }
            }
        }
        return client;
    }

}
