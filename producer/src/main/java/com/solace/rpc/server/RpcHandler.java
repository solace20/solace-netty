package com.solace.rpc.server;


import com.solace.rpc.server.RpcServer;
import com.solace.common.protocol.RpcRequest;
import com.solace.common.protocol.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest>{
    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String ,Object> handleMap;

    public RpcHandler(Map<String, Object> handleMap) {
        this.handleMap = handleMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("client connect:{}",ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("client disconnect:{}",ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        logger.info("received request from{}"+rpcRequest);
        RpcServer.submit(new Runnable() {
            @Override
            public void run() {
                logger.info("receive request:"+rpcRequest.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(rpcRequest.getRequestId());
                try {
                    Object result = handle(rpcRequest);
                    response.setResult(result);
                } catch (InvocationTargetException e) {
                    //e.printStackTrace();
                    response.setError(e.toString());
                    logger.error("RpcServer handle request error");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.debug("Send Response for request:"+rpcRequest.getRequestId());
                    }
                });
            }
        });
    }

    /**
     * 处理服务调用请求
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    private Object handle(RpcRequest request) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String className = request.getClassName();
        Object serviceBean = handleMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParamters();
        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        //通过cglib反射调用方法
//        FastClass serviceFastClass = FastClass.create(serviceClass);
//        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName,parameterTypes);
//        return serviceFastMethod.invoke(serviceClass,parameters);
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
