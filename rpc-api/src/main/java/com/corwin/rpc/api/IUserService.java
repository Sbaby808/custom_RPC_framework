package com.corwin.rpc.api;

import com.corwin.rpc.pojo.User;

import java.util.List;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/1 9:52:16
 */
public interface IUserService {

    User getById(int id);

    List<User> getAll();
}
