package com.solace.bean;

import com.solace.protocol.RpcDecoder;
import com.solace.protocol.RpcEncoder;
import com.solace.protocol.RpcRequest;
import com.solace.protocol.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel>{
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new RpcEncoder(RpcRequest.class))
                .addLast(new RpcDecoder(RpcResponse.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0))
                .addLast(new RpcClientHandler());
    }
}
