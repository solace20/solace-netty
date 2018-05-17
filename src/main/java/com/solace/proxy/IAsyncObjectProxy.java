package com.solace.proxy;

import com.solace.bean.RpcFuture;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public interface IAsyncObjectProxy {
    public RpcFuture call(String funcName,Object... args);
}
