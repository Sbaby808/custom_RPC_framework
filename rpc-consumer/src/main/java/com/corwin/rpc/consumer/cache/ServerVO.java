package com.corwin.rpc.consumer.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 14:52:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerVO {

    private String ip;
    private int port;

    public String getKey() {
        return ip + ":" + port;
    }
}
