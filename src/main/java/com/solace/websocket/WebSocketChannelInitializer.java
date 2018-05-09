package com.solace.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/10
 * @CopyRight lengbar.cn
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel>{
    /**
     * 初始化设置channel
     * @param socketChannel
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("http-codec",new HttpServerCodec())
                .addLast("aggregator", new HttpObjectAggregator(65536))//将HTTP消息的多个部分合成一条完整的HTTP消息
                .addLast("http-chunked",new ChunkedWriteHandler())//向客户端发送HTML5文件
                .addLast("handle",new WebSocketServerHandle());

    }
}
