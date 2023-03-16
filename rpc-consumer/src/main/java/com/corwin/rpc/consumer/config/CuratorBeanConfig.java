package com.corwin.rpc.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 14:53:11
 */
@Slf4j
@Configuration
public class CuratorBeanConfig {

    @Bean
    public CuratorFramework curatorClient() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 5);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(50000)
                .connectionTimeoutMs(30000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        log.info("zookeeper session established!");
        return client;
    }
}
