package com.solace.proxy;

import com.solace.bean.RpcClientHandler;
import com.solace.bean.RpcFuture;
import com.solace.bean.RpcServerConnectManager;
import com.solace.protocol.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class ObjectProxy<T> implements InvocationHandler,IAsyncObjectProxy{
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz){
        this.clazz = clazz;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Object类默认处理方式
        if (Object.class==method.getDeclaringClass()){
            String methodName = method.getName();
            if ("equals".equals(methodName)){
                return proxy==args[0];
            }else if ("hashCode".equals(methodName)){
                return System.identityHashCode(proxy);
            }else if ("toString".equals(methodName)){
                return proxy.getClass().getName()+"@"+Integer.toHexString(System.identityHashCode(proxy))+",with" +
                        " InvocationHanlder"+this;
            }else {
                throw new IllegalStateException(String.valueOf(method));
            }

        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParamters(args);

        // Debug
        logger.debug(method.getDeclaringClass().getName());
        logger.debug(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            logger.debug(method.getParameterTypes()[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            logger.debug(args[i].toString());
        }

        RpcClientHandler handler = RpcServerConnectManager
    }
}
