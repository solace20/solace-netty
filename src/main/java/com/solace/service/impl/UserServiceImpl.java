package com.solace.service.impl;

import com.solace.annotation.RpcService;
import com.solace.service.UserService;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
@RpcService(UserService.class)
public class UserServiceImpl implements UserService{
    @Override
    public String sayHello() {
        return "hello solace";
    }
}
