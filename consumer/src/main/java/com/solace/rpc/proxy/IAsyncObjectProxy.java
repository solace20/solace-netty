package com.solace.rpc.proxy;

import com.solace.rpc.client.RpcFuture;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public interface IAsyncObjectProxy {
    public RpcFuture call(String funcName, Object... args);
}
