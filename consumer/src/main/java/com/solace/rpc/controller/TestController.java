package com.solace.rpc.controller;

import com.solace.rpc.client.RpcClient;
import com.solace.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/17
 * @CopyRight lengbar.cn
 */
@RestController
public class TestController {
    @Autowired
    private RpcClient rpcClient;

    @GetMapping("/1")
    public String remoteInvoke(){
        UserService userService = rpcClient.create(UserService.class);
        return userService.sayHello();
    }
}
