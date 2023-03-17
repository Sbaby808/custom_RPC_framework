package com.corwin.rpc.consumer.cache;

import com.corwin.rpc.consumer.ClientBootStrap;
import com.corwin.rpc.consumer.util.TimeTaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author dingsheng
 * @version 1.0
 * @description: 服务端列表缓存
 * @date 2023/3/13 10:02:13
 */
@Slf4j
public class ServerListCache {

    public static final Map<String, ServerVO> serverList = new ConcurrentHashMap<>();
    public static final Map<String, ScheduledFuture> taskMap = new ConcurrentHashMap<>();

    public static void add(ServerVO server) {
        if(!serverList.containsKey(server.getKey())) {
            serverList.put(server.getKey(), server);
            log.info("ServerList add success:" + server);
            log.info("ServerList total:" + serverList);
            TimeTaskExecutor executor = ClientBootStrap.ac.getBean(TimeTaskExecutor.class);
            executor.addTask(server.getIp() + ":" + server.getPort());
        }
    }

    public static void remove(ServerVO server) {
        serverList.remove(server.getKey());
        log.info("ServerList remove success:" + server);
        log.info("ServerList total:" + serverList);
        TimeTaskExecutor executor = ClientBootStrap.ac.getBean(TimeTaskExecutor.class);
        executor.removeTask(server.getIp() + ":" + server.getPort());
    }

    public static ServerVO findByNameStr(String name) {
        String[] split = name.split(",");
        return serverList.get(split[0]);
    }

    public static ServerVO randomOne() {
        if(ObjectUtils.isEmpty(serverList)) {
            throw new RuntimeException("无可用服务端！");
        }
        Random random = new Random();
        int index = random.nextInt(serverList.size());
        return (ServerVO) serverList.values().toArray()[index];
    }

    public static ServerVO minLastTimeOne() {
        CuratorFramework client = ClientBootStrap.ac.getBean(CuratorFramework.class);
        try {
            boolean flag = false;
            String pickOne = "";
            long minTime = Integer.MAX_VALUE;
            List<String> strings = client.getChildren().forPath("/custom_rpc/server");
            if (ObjectUtils.isEmpty(strings)) {
                for (String string : strings) {
                    byte[] bytes = client.getData().forPath("/custom_rpc/server/" + string);
                    if(bytes != null) {
                        long costTime = Long.parseLong(new String(bytes).split(",")[0]);
                        if(minTime != Integer.MAX_VALUE && costTime != minTime) {
                            flag = true;
                        }
                        if(costTime < minTime) {
                            minTime = costTime;
                            pickOne = string;
                        }
                    }
                }
            }
            if(flag) {
                return findByNameStr(pickOne);
            } else {
                return randomOne();
            }
        } catch (Exception e) {
            log.error("获取zk服务列表失败！");
            throw new RuntimeException(e);
        }
    }
}
