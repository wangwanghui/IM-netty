package com.octv.im.util;

import com.octv.im.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.util.Assert;

public class ServerBootstrapBuilder {
    /**
     * 用于持有将会生成的ServerBootstrap对象
     */
    private ServerBootstrap serverBootstrap;


    /**
     * 用于持有各种参数，初始值为null，将在构造函数中被初始化。
     */
    private final NettyConfig config;

    private EventLoopGroup bossGroup; // 连接线程
    private EventLoopGroup workerGroup; // 处理线程组

    private ChannelInitializer<?> channelInitializer;

    public ServerBootstrapBuilder(NettyConfig config) {
        Assert.notNull(config, "config cannot be null");
        this.config = config;
    }

    public synchronized ServerBootstrap getServerBootstrap() {
        if (serverBootstrap == null) {
            serverBootstrap = new ServerBootstrap();
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, config.isReuseAddress());
            serverBootstrap.option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize());
            serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout()); // 多少秒没有连上服务器则返回
            //serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);//设置netty的高低水位防止OOM
            serverBootstrap.childHandler(config.getChannelInitializer());
        }
        return serverBootstrap;
    }

    /**
     * <p>
     * 关闭线程池。如果没有启用或者已经关闭，不会有任何影响(优雅地退出)
     * </p>
     */
    public synchronized void shutdownGracefully() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

}
