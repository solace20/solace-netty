package com.solace.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.logging.Logger;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: ${todo}(这里用一句话描述这个类的作用)
 * @date 2018/5/10
 * @CopyRight lengbar.cn
 */
public class WebSocketServerHandle extends SimpleChannelInboundHandler<Object>{
    private static final Logger logger = Logger
            .getLogger(WebSocketServerHandle.class.getName());

    private WebSocketServerHandshaker handshaker;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

    }

    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request){
        if (request.decoderResult().isSuccess()||(!"websocket".equals(request.headers().get("Upgrade")))){
            sendHttpResponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
        }
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket",null,false);
        handshaker = factory.newHandshaker(request);
        if (handshaker==null){

        }
    }

    public void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response){
        if (response.getStatus().code()!=200){
            ByteBuf buf  = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(response,response.content().readableBytes());
        }
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(request)||response.status().code() != 200){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
