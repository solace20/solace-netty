package com.solace.rpc.proxy;


import com.solace.rpc.client.RpcClientHandler;
import com.solace.rpc.client.RpcFuture;
import com.solace.rpc.client.RpcServerConnectManager;
import com.solace.common.protocol.RpcRequest;
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
public class ObjectProxy<T> implements InvocationHandler,IAsyncObjectProxy {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz){
        this.clazz = clazz;
    }

    @Override
    public RpcFuture call(String funcName, Object... args){
        RpcClientHandler handler = RpcServerConnectManager.getInstance().chooseHandler();
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
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
//        for (int i = 0; i < args.length; ++i) {
//            logger.debug(args[i].toString());
//        }

        RpcClientHandler handler = RpcServerConnectManager.getInstance().chooseHandler();
        RpcFuture future = handler.sendRequest(request);
        return future.get();
    }
    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParamters(args);

        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
//        Method[] methods = clazz.getDeclaredMethods();
//        for (int i = 0; i < methods.length; ++i) {
//            // Bug: if there are 2 methods have the same name
//            if (methods[i].getName().equals(methodName)) {
//                parameterTypes = methods[i].getParameterTypes();
//                request.setParameterTypes(parameterTypes); // get parameter types
//                break;
//            }
//        }

        logger.debug(className);
        logger.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            logger.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            logger.debug(args[i].toString());
        }

        return request;
    }
    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }

        return classType;
    }
}
