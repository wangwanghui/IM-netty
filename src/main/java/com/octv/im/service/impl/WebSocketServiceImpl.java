package com.octv.im.service.impl;

import static com.octv.im.constant.RabbitKeyConstant.*;
import static com.octv.im.constant.ChatMessageConstant.JOIN_CHAT_GROUP_ROOM;
import static com.octv.im.constant.RabbitKeyConstant.GROUP_CHAT_ROUT_KEY;
import static com.octv.im.constant.RedisKeyConst.OFFLINE_CHAT_PERSON_KEY_PREFIX;
import static com.octv.im.constant.RedisKeyConst.ONLINE_MEMBER_LIST_KEY;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octv.im.config.ChatServerProperties;
import com.octv.im.service.RabbitProductService;
import com.octv.im.util.ChatChannelManage;
import com.octv.im.constant.ChatMessageConstant;
import com.octv.im.entity.ChatMessageBean;
import com.octv.im.service.ChannelSessionService;
import com.octv.im.service.WebSocketService;
import com.octv.im.util.DateUtils;
import com.octv.im.util.NettyAttrUtil;
import com.octv.im.util.RequestParamUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.*;


@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    // WebSocket 握手工厂类
    @Autowired
    private WebSocketServerHandshakerFactory factory;

    @Autowired
    private ChannelSessionService channelSessionService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitProductService rabbitProductService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatServerProperties chatServerProperties;

    private WebSocketServerHandshaker handshaker;


    @Override
    public void handleWebSocketConnect(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 如果请求失败或者该请求不是客户端向服务端发起的 http 请求，则响应错误信息
        if (!request.decoderResult().isSuccess()
                || !("websocket".equals(request.headers().get("Upgrade")))) {
            // code ：400
            sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        //新建一个握手
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            //如果为空，返回响应：不受支持的 websocket 版本
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            //否则，执行握手
            try {
                String url = URLDecoder.decode(request.uri(), "utf-8");
                Map<String, String> params = RequestParamUtil.urlSplit(url);
                Channel channel = ctx.channel();
                String userId = params.get("userId");
                redisTemplate.opsForSet().add(ONLINE_MEMBER_LIST_KEY, userId);
                NettyAttrUtil.setUserId(channel, userId);
                NettyAttrUtil.refreshLastHeartBeatTime(channel);
                handshaker.handshake(ctx.channel(), request);
                log.info("该用户 {} 连接成功，客户端请求uri：{} 和 channel {} ", userId, url, channel);
                channelSessionService.addChannel(channel, userId);
                //处理离线消息
                String key = StringUtils.joinWith(".", OFFLINE_CHAT_PERSON_KEY_PREFIX, userId);
                //按照score 从大到小 拿出前1000条数据
                Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 1, 1000, 0, 1);
                Double score = 0D;
                if (set != null && !set.isEmpty()) {
                    String msg = null; //测试 默认发一条后期需要发送一个List 给前端
                    for (ZSetOperations.TypedTuple<Object> s : set) {
                        msg = (String) s.getValue();
                        score = s.getScore();
                        System.out.println("MSG --- +score ----->" + msg + "ppppp  ---" + score);
                    }
                    ChannelFuture channelFuture = channel.writeAndFlush(new TextWebSocketFrame(msg));
                    Double finalScore = score;
                    channelFuture.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                log.info("离线消息成功发送");
                                redisTemplate.opsForZSet().removeRangeByScore(key, finalScore, finalScore);
                            } else {
                                System.out.println("离线消息发送失败");
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleWebSocketWork(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否是关闭 websocket 的指令
        if (frame instanceof CloseWebSocketFrame) {
            //关闭握手
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            channelSessionService.clearSession(ctx.channel());
            return;
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //判断是否是二进制消息，如果是二进制消息，抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("目前我们不支持二进制消息");
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            throw new RuntimeException("【" + this.getClass().getName() + "】不支持消息");
        }
        // 获取并解析客户端向服务端发送的 json 消息
        String message = ((TextWebSocketFrame) frame).text();
        messageDispatch(ctx.channel(), message);
    }


    /**
     * 消息转发
     */
    @Override
    public void messageDispatch(Channel channel, String message) {
        log.info("channel {} 发来 消息：{}", channel, message);
        JSONObject json = JSONObject.parseObject(message);
        try {
            String uuid = UUID.randomUUID().toString();
            String time = DateUtils.date2String(new Date(), "yyyy-MM-dd HH:mm:ss");
            json.put("id", uuid);
            json.put("sendTime", time);

            int code = json.getIntValue("code");
            switch (code) {
                //群聊
                case ChatMessageConstant.GROUP_CHAT_CODE:
                    //发送群消息
                    try {
                        // ChatMessageBean chatMessageBean = objectMapper.readValue(message, ChatMessageBean.class);
                        //测试 正常情况下 会下加入聊天室再发消息  ----演示加入聊天室
                        if (chatServerProperties.getOpenCluster()) {
                            rabbitProductService.sendMessage(CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE, null, json);
                        } else {
                            //单机版
                            ChannelGroup channelGroup = ChatChannelManage.chatChannelGroups.get("10");
                            if (channelGroup == null) {
                                channelGroup = new DefaultChannelGroup("chatGroup", GlobalEventExecutor.INSTANCE);
                                channelGroup.add(channel);
                                ChatChannelManage.chatChannelGroups.put("10", channelGroup);
                            } else {
                                channelGroup.add(channel);
                            }
                            channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(json)));
                        }
                        //通过MQ 存入 mysql  ---防止高并发请求到mysql
                        rabbitProductService.sendMessage(GROUP_CHAT_EXCHANGE, GROUP_CHAT_ROUT_KEY, json);
                        //rabbitProductService.sendMessage(GROUP_CHAT_EXCHANGE, GROUP_CHAT_ROUT_KEY, json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                //私聊
                case ChatMessageConstant.PRIVATE_CHAT_CODE:
                    //接收人id
                    String receiveUserId = json.getString("receiverUserId");
                    String sendUserId = json.getString("sendUserId");
                    String msg = JSONObject.toJSONString(json);
                    Channel receiveChannel = ChatChannelManage.channelMap.get(receiveUserId);
                    if (receiveChannel == null) {
                        log.info("存入离线消息 {} ", message);
                        String key = StringUtils.joinWith(".", OFFLINE_CHAT_PERSON_KEY_PREFIX, receiveUserId);
                        redisTemplate.opsForZSet().incrementScore(key, msg, 1);
                        //channel.writeAndFlush(new TextWebSocketFrame(msg));
                        if (chatServerProperties.getOpenCluster()) {
                            rabbitProductService.sendMessage(CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE, null, json);
                        }
                    } else {
                        //在线的直接发给用户
                        this.writeAndFlush(receiveChannel, new TextWebSocketFrame(msg));
                        //channel.writeAndFlush(new TextWebSocketFrame(msg));
                    }
                    // 如果发给别人，给自己也发一条
                    if (!receiveUserId.equals(sendUserId)) {
                        ChatChannelManage.channelMap.get(sendUserId).writeAndFlush(new TextWebSocketFrame(msg));
                    }
                    //存入mysql
                    //rabbitProductService.sendMessage(PERSON_CHAT_EXCHANGE, PERSON_CHAT_ROUT_KEY, json);
                    break;
                case ChatMessageConstant.SYSTEM_MESSAGE_CODE:
                    //向连接上来的客户端广播消息
                    System.out.println("广播信息 ---》");
                    //SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(json)));
                    break;
                case ChatMessageConstant.USER_GROUP_ROOM_ACTION:
                    //用户加入或退出聊天室 action = 1 is join  0 is leave
                    // {"userID":"wang","action":"1",groupID:"10","sendTime":"2022-05-05 10:20:58"}
                    ChatMessageBean chatMessageBean = objectMapper.readValue(message, ChatMessageBean.class);
                    ChannelGroup channelGroups = ChatChannelManage.chatChannelGroups.get(chatMessageBean.getGroupID());
                    if (StringUtils.endsWithIgnoreCase(JOIN_CHAT_GROUP_ROOM, chatMessageBean.getAction())) {
                        if (channelGroups == null || channelGroups.isEmpty()) {
                            ChannelGroup channelGroup = new DefaultChannelGroup("Group_Chat_" + chatMessageBean.getGroupID(), GlobalEventExecutor.INSTANCE);
                            channelGroup.add(channel);
                            ChatChannelManage.chatChannelGroups.put(chatMessageBean.getGroupID(), channelGroup);
                        } else {
                            channelGroups.add(channel);
                        }
                    } else {
                        Channel removeChannel = ChatChannelManage.channelMap.get(chatMessageBean.getUserID());
                        channelGroups.remove(removeChannel);
                    }
                    break;
                //pong
                case ChatMessageConstant.PONG_CHAT_CODE:
                    // 更新心跳时间
                    NettyAttrUtil.refreshLastHeartBeatTime(channel);
                default:
            }
        } catch (Exception e) {
            log.error("转发消息异常:", e);
            e.printStackTrace();
        }
    }

    /**
     * 服务端向客户端响应消息
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, DefaultFullHttpResponse response) {
        if (response.status().code() != 200) {
            //创建源缓冲区
            ByteBuf byteBuf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
            //将源缓冲区的数据传送到此缓冲区
            response.content().writeBytes(byteBuf);
            //释放源缓冲区
            byteBuf.release();
        }
        //写入请求，服务端向客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (response.status().code() != 200) {
            /**
             * 如果请求失败，关闭 ChannelFuture
             * ChannelFutureListener.CLOSE 源码：future.channel().close();
             */
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    //防止netty因为消息超过了高水位 发生内存泄漏的问题
    private void writeAndFlush(Channel channel, Object message) {
        if (channel.isActive() && channel.isWritable()) {
            int a = channel.config().getWriteBufferHighWaterMark();
            System.out.println(a + "ggggggggggg");
            channel.writeAndFlush(message);
        } else {
            log.error("message exceed netty buffer");
        }

    }
}
