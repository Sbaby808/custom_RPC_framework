package com.corwin.rpc.consumer.util;

import com.corwin.rpc.consumer.cache.ServerListCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 16:14:35
 */
@Slf4j
@Component
public class TimeTaskExecutor {

    @Autowired
    CuratorFramework client;

    @Autowired
    ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public void addTask(String name) {
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(new MyTask(name), new PeriodicTrigger(5, TimeUnit.SECONDS));
        ServerListCache.taskMap.put(name, future);
        log.info("添加任务：" + name);
    }

    public void removeTask(String name) {
        ScheduledFuture future = ServerListCache.taskMap.get(name);
        if(future != null) {
            log.info("取消任务：" + name);
            future.cancel(true);
        }
    }

    private class MyTask implements Runnable {
        private String name;

        public MyTask(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                byte[] bytes = client.getData().forPath("/custom_rpc/server/" + name);
                if(bytes != null) {
                    String content = new String(bytes);
                    String[] split = content.split(",");
                    if(split.length == 2) {
                        long lastTime = Long.parseLong(split[1]);
                        log.info(new Date(lastTime).toLocaleString());
                        long res = System.currentTimeMillis() - lastTime - 5000;
                        if(res > 0) {
                            client.setData().forPath("/custom_rpc/server/" + name, null);
                            log.info("清除" + name + "节点时间！");
                        }
                    }
                }
            } catch (Exception e) {
                log.info("获取" + name + "节点信息失败！");
                e.printStackTrace();
            }
        }
    }
}
