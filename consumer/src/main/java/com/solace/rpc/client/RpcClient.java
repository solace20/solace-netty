package com.solace.rpc.client;


import com.solace.rpc.proxy.IAsyncObjectProxy;
import com.solace.rpc.proxy.ObjectProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class RpcClient {

    private String serverAddress;
    @Autowired(required = false)
    private ServiceDiscovery serviceDiscovery;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));

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
        RpcServerConnectManager.getInstance().stop();
    }

    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }
}
