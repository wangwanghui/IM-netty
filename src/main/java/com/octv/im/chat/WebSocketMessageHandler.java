package com.octv.im.chat;

import com.octv.im.service.ChannelSessionService;
import com.octv.im.service.WebSocketService;
import com.octv.im.util.ApplicationContextHolder;
import com.octv.im.util.NettyAttrUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


/**
 * MessageHandler，用来处理用户连接以及聊天的信息
 *
 * @Author
 * @Description 消息处理，接收用户消息的逻辑类
 */
@Slf4j
public class WebSocketMessageHandler extends SimpleChannelInboundHandler<Object> {

    private static WebSocketService webSocketService = ApplicationContextHolder
            .getBean(WebSocketService.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println("channelRead0   -----");
        if (o instanceof FullHttpRequest) {
            //处理客户端向服务端发起 http 请求的业务
            webSocketService.handleWebSocketConnect(channelHandlerContext, (FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            //处理客户端与服务端之间的 websocket 业务
            webSocketService.handleWebSocketWork(channelHandlerContext, (WebSocketFrame) o);
        }
    }


    /**
     * 客户端与服务端创建连接的时候调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //创建新的 WebSocket 连接，保存当前 channel
        log.info("————客户端与服务端连接开启————");

//        // 设置高水位
//        ctx.channel().config().setWriteBufferHighWaterMark();
//        // 设置低水位
//        ctx.channel().config().setWriteBufferLowWaterMark();
    }

    /**
     * 客户端与服务端断开连接的时候调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = NettyAttrUtil.getUserId(ctx.channel());
        log.info("用户 {} 在 {} 与服务端断开连接", userId, LocalDateTime.now());
        ChannelSessionService channelSessionService = ApplicationContextHolder
                .getBean(ChannelSessionService.class);
        channelSessionService.clearSession(ctx.channel());
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 工程出现异常的时候调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:", cause);
        ctx.close();
    }
}
