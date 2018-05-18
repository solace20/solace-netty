package com.solace.rpc.service.impl;


import com.solace.common.annotation.RpcService;
import com.solace.common.service.UserService;

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
