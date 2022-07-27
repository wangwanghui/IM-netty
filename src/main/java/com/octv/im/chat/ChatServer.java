package com.octv.im.chat;

import com.octv.im.config.NettyConfig;
import com.octv.im.util.ServerBootstrapBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 聊天室的服务端开启或关闭
 */
@Slf4j
public class ChatServer implements Runnable {

    private ServerBootstrapBuilder builder;

    private Channel channel;

    private ServerBootstrap bootstrap;

    private int bindPort;

    public ChatServer(int port) {
        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //请求解码器
                socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
                //将多个消息转换成单一的消息对象
                socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
                //支持异步发送大的码流，一般用于发送文件流
                socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                //处理 websocket 和处理消息的发送
                socketChannel.pipeline().addLast("handler", new WebSocketMessageHandler());
            }
        };
        NettyConfig nettyConfig = new NettyConfig();
        nettyConfig.setChannelInitializer(channelInitializer);
        nettyConfig.setConnectTimeout(20);
        builder = new ServerBootstrapBuilder(nettyConfig);
        bindPort = port;
    }


    @Override
    public void run() {
        if (channel != null && channel.isActive()) {
            return;
        }
        bootstrap = builder.getServerBootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(bindPort));
            channelFuture.addListener(new FutureListener<Void>() {
                @Override
                public void operationComplete(Future<Void> future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("chat server started at port: {}", bindPort);
                    } else {
                        log.error("chat server start failed at port: {}!", bindPort);
                        EventLoop eventLoop = channelFuture.channel().eventLoop();
                        reconnect(eventLoop);
                    }
                }
            });
        } catch (Exception e) {
            log.error("ChatServer start error.", e);
        }
    }


    //服务端意外挂了会每隔60秒再执行
    protected void reconnect(EventLoop eventLoop) {
        eventLoop.schedule(this, 60L, TimeUnit.SECONDS);
    }
}
