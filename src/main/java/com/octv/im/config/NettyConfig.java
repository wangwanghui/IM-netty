package com.octv.im.config;


import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import com.octv.im.util.ChannelInitializeBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 该类是一个配置信息类。我们可以在这个类中集中设置各种信息，用于生成对应的netty的ServerBootstrap或者ClientBootstrap
 *
 * @version 1.0
 */
@Slf4j
@Data
@ToString
public class NettyConfig {

    public static final AttributeKey<String> CHANNEL_SYNC_KEY = AttributeKey.valueOf("CHANNEL_SYNC_KEY");

    private static int defaultReceiveBuffer = 1024;
    private static int defaultSendBuffer = 1024;
    private static boolean defaultTcpNoDelay = false;

    static {
        Socket unconnectedSocket = new Socket();
        try {
            defaultReceiveBuffer = unconnectedSocket.getReceiveBufferSize();
            defaultSendBuffer = unconnectedSocket.getSendBufferSize();
            defaultTcpNoDelay = unconnectedSocket.getTcpNoDelay();
        } catch (SocketException se) {
            log.debug("Can't init socket system default");
        } finally {
            try {
                unconnectedSocket.close();
            } catch (IOException e) {
                log.debug("Close socket failed");
            }
        }
    }

    /**
     * <p>
     * 是否启动线程池。默认启用。
     * </p>
     */
    private boolean threadPool = true;

    /**
     * <p>
     * 使用的ChannelInitializer
     * </p>
     */
    private ChannelInitializer<?> channelInitializer = new ChannelInitializeBuilder();

    /**
     * <p>
     * 客户端连接到服务器时的超时时间，默认为1秒。仅对ConnectorBuilder有效。
     * </p>
     */
    private int connectTimeout = 1;

    /**
     * <p>
     * socket设置：是否重用地址（ip加端口），默认为true.
     * </p>
     */
    private boolean reuseAddress = true;

    /**
     * <p>
     * socket设置：是否从socket级别保持连接，默认为true
     * </p>
     */
    private boolean socketKeepAlive = true;

    /**
     * <p>
     * socket关闭时的延迟时间设置：soLinger，默认为 0
     * </p>
     */
    private int socketSoLinger = 0;

    /**
     * <p>
     * 接收缓存大小设置，windows XP系统中默认值为8096字节，即4KB.
     * </p>
     */
    private int receiveBufferSize = defaultReceiveBuffer;

    /**
     * <p>
     * 发送缓存大小设置，windows XP系统中默认为8096字节，即8KB
     * </p>
     */
    private int sendBufferSize = defaultSendBuffer;

    /**
     * <P>
     * 启用/禁用Nagle算法,是否不进行任何延迟就发送数据.Socket中默认设置为false。
     * </P>
     * 如果为true，则无论数据大小为多少，都会立刻发出；此时当数据小于TCP包头(40个Byte)大小时，容易产生过载现象，增加带宽占用。 <br>
     * 如果为false，则当数据较小时（比如小于40个字节），会等到有更多待发送数据才封装成一个包一次性发出，最多会等待300ms。带宽占用减少但延迟增加
     */
    private boolean tcpNoDelay = defaultTcpNoDelay;

}
