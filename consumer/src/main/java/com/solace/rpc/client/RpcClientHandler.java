package com.solace.rpc.client;


import com.solace.common.protocol.RpcRequest;
import com.solace.common.protocol.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse>{
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentHashMap<String, RpcFuture> pendingRpc = new ConcurrentHashMap<>();

    private volatile Channel channel;
    private SocketAddress remotePeer;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        RpcFuture rpcFuture = pendingRpc.get(requestId);
        if (rpcFuture!=null){
            pendingRpc.remove(requestId);
            rpcFuture.done(rpcResponse);
        }
    }

    public RpcFuture sendRequest(RpcRequest request){
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture future = new RpcFuture(request);
        pendingRpc.put(request.getRequestId(),future);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return future;
    }

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
