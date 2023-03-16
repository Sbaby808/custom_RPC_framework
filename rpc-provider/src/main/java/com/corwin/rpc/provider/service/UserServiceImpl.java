package com.corwin.rpc.provider.service;

import com.corwin.rpc.api.IUserService;
import com.corwin.rpc.pojo.User;
import com.corwin.rpc.provider.anno.RpcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 10:24:03
 */
@RpcService
@Service
public class UserServiceImpl implements IUserService {
    private Map<Object, User> userMap = new HashMap<>();

    @Override
    public User getById(int id) {
        if(userMap.size() == 0) {
            User user1 = new User();
            user1.setId(1);
            user1.setName("张三");
            User user2 = new User();
            user2.setId(2);
            user2.setName("李四");
            userMap.put(user1.getId(), user1);
            userMap.put(user2.getId(), user2);
        }
        return userMap.get(id);
    }

    @Override
    public List<User> getAll() {
        if(userMap.size() == 0) {
            User user1 = new User();
            user1.setId(1);
            user1.setName("张三");
            User user2 = new User();
            user2.setId(2);
            user2.setName("李四");
            userMap.put(user1.getId(), user1);
            userMap.put(user2.getId(), user2);
        }
        return new ArrayList<User>(userMap.values());
    }
}
