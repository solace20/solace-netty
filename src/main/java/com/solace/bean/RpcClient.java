package com.solace.bean;

import com.solace.proxy.IAsyncObjectProxy;
import com.solace.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcClient {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));

    public RpcClient(){

    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},new ObjectProxy<>(interfaceClass)
        );
    }
    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass){
        return new ObjectProxy<T>(interfaceClass);
    }

    public void stop(){
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
    }
}
