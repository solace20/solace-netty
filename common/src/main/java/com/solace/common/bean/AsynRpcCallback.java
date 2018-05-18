package com.solace.common.bean;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public interface AsynRpcCallback {
    void success(Object result);

    void fail(Exception e);

}
