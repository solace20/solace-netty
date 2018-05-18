package com.solace.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Solace
 * @Description: 服务提供列表管理
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcServerConnectManager {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerConnectManager.class);

    private volatile static RpcServerConnectManager connectManager;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65535));

    //一个线程安全的数组，用来存放所用处理handle实例
    private CopyOnWriteArrayList<RpcClientHandler> connectedHandles = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress,RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private long connectTimeOutMills = 6000;
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;

    private RpcServerConnectManager(){

    }

    public static RpcServerConnectManager getInstance(){
        if (connectManager==null){
            synchronized (RpcServerConnectManager.class){
                if (connectManager==null){
                    connectManager = new RpcServerConnectManager();
                }
            }
        }
        return connectManager;
    }

    public void reconnect(final RpcClientHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandles.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    public void updateConnectedServer(List<String> allServerAddress){
        if (allServerAddress!=null){
            if (allServerAddress.size()>0){
                HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<>();
                for (int i = 0;i < allServerAddress.size();i++){
                    String[] array = allServerAddress.get(i).split(":");
                    if (array.length==2){
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        final InetSocketAddress remotePeer = new InetSocketAddress(host,port);
                        newAllServerNodeSet.add(remotePeer);
                    }
                }

                //add new server node
                for (final InetSocketAddress serverNodeAddress : newAllServerNodeSet){
                    if (!connectedServerNodes.keySet().contains(serverNodeAddress)){
                        connectServerNode(serverNodeAddress);
                    }
                }

                //close and remove invalid server nodes
                for (int i = 0;i < connectedServerNodes.size();++i){
                    RpcClientHandler connectedClientHandler = connectedHandles.get(i);
                    SocketAddress remotePeer = connectedClientHandler.getRemotePeer();
                    if (!newAllServerNodeSet.contains(remotePeer)){
                        logger.info("remove invalid server node:{}",remotePeer);
                        RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                        if (handler!=null){
                            handler.close();
                        }
                        connectedServerNodes.remove(remotePeer);
                        connectedHandles.remove(connectedClientHandler);
                    }
                }



            }else {
                logger.error("No available server node , all server nodes crash!!!");
                for (final RpcClientHandler connectedServerHandle: connectedHandles){
                    SocketAddress remotePeer = connectedServerHandle.getRemotePeer();
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(connectedServerHandle);
                }
                connectedHandles.clear();
            }
        }
    }

    private void connectServerNode(final InetSocketAddress remotePeer){
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());
                ChannelFuture future = b.connect(remotePeer);
                future.addListener(
                        new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if (channelFuture.isSuccess()){
                                    logger.info("successfully connect to remote server:{}",remotePeer);
                                    RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                                    addHandler(handler);
                                }
                            }
                        }
                );
            }
        });
    }

    public RpcClientHandler chooseHandler(){
        int size = connectedHandles.size();
        while (isRunning&& size<=0){
            try {
                boolean available = waitingForHandler();
                if (available){
                    size = connectedHandles.size();
                }
            } catch (InterruptedException e) {
                logger.error("waitting for available handler node is interrupted!",e);
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        int index = (roundRobin.getAndAdd(1)+size)%size;
        return connectedHandles.get(index);
    }

    public boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeOutMills,TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    private void addHandler(RpcClientHandler handler){
        connectedHandles.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress,handler);
        signalAvailableHandler();
    }
    private void signalAvailableHandler(){
        lock.lock();
        try {
            connected.signalAll();
        }finally {
            lock.unlock();
        }
    }

//    private boolean waitingForHandler() {
//
//    }

    public void stop(){
        isRunning = false;
        for (int i = 0; i<connectedHandles.size();i++){
            RpcClientHandler handler = connectedHandles.get(i);
            handler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
