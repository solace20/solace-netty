package com.solace.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author solace
 * @ClassName: ${type_name}
 * @Description: 处理客户端请求
 * @date 2018/5/10
 * @CopyRight lengbar.cn
 */
public class WebSocketServerHandle extends SimpleChannelInboundHandler<Object>{
    private static final Logger logger = Logger
            .getLogger(WebSocketServerHandle.class.getName());

    private WebSocketServerHandshaker handshaker;

    /**
     * 第一次链接进来，是以http形式，然后对协议进行转换
     * @param channelHandlerContext
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        // 传统的HTTP接入
        if (o instanceof FullHttpRequest) {
            handleHttpRequest(channelHandlerContext, (FullHttpRequest) o);
        }
        // WebSocket接入
        else if (o instanceof WebSocketFrame) {
            handleWebSocketFrame(channelHandlerContext, (WebSocketFrame) o);
        }
    }

    /**
     * 代码深度解读：读完消息之后，ByteBuf并未刷盘，需要调用flush完成真正的刷盘
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    /**
     * 出来websocket协议请求
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx,
                                      WebSocketFrame frame) {

        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(),
                    (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }

        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("%s received %s", ctx.channel(), request));
        }
        ctx.channel().write(
                new TextWebSocketFrame(request
                        + " , 欢迎使用Netty WebSocket服务，现在时刻："
                        + new java.util.Date().toString()));
    }

    /**
     * 处理http请求，协议转换
     * @param ctx
     * @param request
     */
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request){
        if (!request.decoderResult().isSuccess()||(!"websocket".equals(request.headers().get("Upgrade")))){
            sendHttpResponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
        }
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket",null,false);
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * 返回response
     * @param ctx
     * @param request
     * @param response
     */
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

    /**
     * 处理异常
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
