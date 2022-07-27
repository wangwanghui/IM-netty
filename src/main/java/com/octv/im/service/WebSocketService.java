package com.octv.im.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public interface WebSocketService {
    void handleWebSocketConnect(ChannelHandlerContext ctx, FullHttpRequest request);

    void handleWebSocketWork(ChannelHandlerContext ctx, WebSocketFrame frame);

    void messageDispatch(Channel channel, String message);
}
