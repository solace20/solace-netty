package com.solace.bean;

import com.solace.annotation.RpcService;
import com.solace.protocol.RpcDecoder;
import com.solace.protocol.RpcEncoder;
import com.solace.protocol.RpcRequest;
import com.solace.protocol.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: 服务注册初始化(这里用一句话描述这个类的作用)
 * @date 2018/5/16
 * @CopyRight lengbar.cn
 */
@Component
public class RpcServer implements ApplicationContextAware,InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    @Autowired(required = false)
    private ServiceRegistry registry;

    @Value("${netty.rpc.serverAddress}")
    private String serverAddress;

    private Map<String,Object> hanldeMap = new ConcurrentHashMap<>();

    private static ThreadPoolExecutor poolExecutor;

    private EventLoopGroup boosGroup = null;
    private EventLoopGroup workerGroup = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!MapUtils.isEmpty(serviceBeanMap)){
            for (Object serviceBean :serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                logger.info("loading service:{}",interfaceName);
                hanldeMap.put(interfaceName,serviceBean);

            }
        }
    }

    public void start() throws InterruptedException {
        if (boosGroup==null){
            boosGroup = new NioEventLoopGroup();
        }
        if (workerGroup==null){
            workerGroup = new NioEventLoopGroup();
        }
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boosGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0))
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcHandler(hanldeMap));
                    }
                })
                .option(ChannelOption.SO_BACKLOG,120)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        String[] array = serverAddress.split(":");
        if (array.length!=2){
            throw new IllegalArgumentException("serverAddress setting error!");
        }
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        ChannelFuture future = bootstrap.bind(host,port).sync();
        logger.info("RpcServer start at {} port {}",host,port);
        if (registry!=null){
            registry.register(serverAddress);
        }
        future.channel().closeFuture().sync();
    }

    public void stop(){
        if (boosGroup!=null)
            boosGroup.shutdownGracefully();
        if (workerGroup!=null)
            workerGroup.shutdownGracefully();
    }

    public static void submit(Runnable task){
        if (poolExecutor == null){
            synchronized (RpcServer.class){
                if (poolExecutor == null){
                    poolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        poolExecutor.submit(task);
    }

    public RpcServer addService(String interfaceName,Object serviceBean){
        if (!hanldeMap.containsKey(interfaceName)){
            logger.info("loadding service:{}"+interfaceName);
            hanldeMap.put(interfaceName,serviceBean);
        }
        return this;
    }
}
