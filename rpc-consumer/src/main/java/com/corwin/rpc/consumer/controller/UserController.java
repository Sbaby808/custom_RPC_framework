package com.corwin.rpc.consumer.controller;

import com.alibaba.fastjson.JSON;
import com.corwin.rpc.api.IUserService;
import com.corwin.rpc.consumer.proxy.RpcClientProxy;
import com.corwin.rpc.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/6 14:50:21
 */
@RestController
public class UserController {

    private IUserService userService = (IUserService) RpcClientProxy.createProxy(IUserService.class);

    @GetMapping("/get/user/{id}")
    public String getUserById(@PathVariable Integer id) {
        User user = userService.getById(id);
        return JSON.toJSONString(user);
    }

    @GetMapping("/get/user/all")
    public String getAll() {
        List<User> all = userService.getAll();
        return JSON.toJSONString(all);
    }

}
