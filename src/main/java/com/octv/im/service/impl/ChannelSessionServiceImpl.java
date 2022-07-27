package com.octv.im.service.impl;

import static com.octv.im.constant.RabbitKeyConstant.CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE;
import static com.octv.im.constant.RedisKeyConst.ONLINE_MEMBER_LIST_KEY;

import com.alibaba.fastjson.JSONObject;
import com.octv.im.config.ChatServerProperties;
import com.octv.im.constant.ChatMessageConstant;
import com.octv.im.entity.ChatMessageBean;
import com.octv.im.service.ChannelSessionService;
import com.octv.im.service.RabbitProductService;
import com.octv.im.util.ChatChannelManage;
import com.octv.im.util.NettyAttrUtil;
import com.octv.im.util.RequestParamUtil;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Netty channel会话管理。管理连接进来的channel
 *
 * @Author
 * @Description
 */
@Service
@Slf4j
public class ChannelSessionServiceImpl implements ChannelSessionService {
    @Autowired
    private RabbitProductService rabbitProductService;
    @Autowired
    private ChatServerProperties chatServerProperties;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public void addChannel(Channel channel, String userId) {
        ChatChannelManage.channelGroup.add(channel);
        ChatChannelManage.channelMap.put(userId, channel);
        // 推送用户上线消息，更新客户端在线用户列表 ...看前端处理用户量大的时候不建议发送给前端userList
        //Set<String> userList = ChatChannelManage.channelMap.keySet();
        Set<Object> userList = redisTemplate.opsForSet().members(ONLINE_MEMBER_LIST_KEY);
        ChatMessageBean chatMessageBean = new ChatMessageBean();
        Map<String, Object> ext = new HashMap<String, Object>();
        ext.put("userList", userList);
        chatMessageBean.setExt(ext);
        chatMessageBean.setCode(ChatMessageConstant.SYSTEM_MESSAGE_CODE);
        chatMessageBean.setType(ChatMessageConstant.UPDATE_USERLIST_SYSTEM_MESSGAE);
        if (chatServerProperties.getOpenCluster()) {
            rabbitProductService.sendMessage(CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE, null, chatMessageBean);
        } else {
            ChatChannelManage.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(chatMessageBean)));
        }
    }

    @Override
    public void clearSession(Channel channel) {
        String userId = NettyAttrUtil.getUserId(channel);
        // 清除会话信息
        redisTemplate.opsForSet().remove(ONLINE_MEMBER_LIST_KEY,userId);
        ChatChannelManage.channelGroup.remove(channel);
        ChatChannelManage.channelMap.remove(userId);
    }

    @Scheduled(cron = "${octv.im-chat-server.scheduled.heart-beat}")
    @Override
    public void scheduledSendClientPing() {
        ChatMessageBean chatMessageBean = new ChatMessageBean();
        chatMessageBean.setCode(ChatMessageConstant.PING_MESSAGE_CODE);
        String message = JSONObject.toJSONString(chatMessageBean);
        ChatChannelManage.channelGroup.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Scheduled(cron = "${octv.im-chat-server.scheduled.check-active}")
    @Override
    public void scheduledScanNotActiveChannel() {
        Map<String, Channel> channelMap = ChatChannelManage.channelMap;
        // 如果这个直播下已经没有连接中的用户会话了，删除频道
        if (channelMap.size() == 0) {
            return;
        }
        for (Channel channel : channelMap.values()) {
            Long lastHeartBeatTime = NettyAttrUtil.getLastHeartBeatTime(channel);
            if (lastHeartBeatTime == null) {
                return;
            }
            long intervalMillis = (System.currentTimeMillis() - lastHeartBeatTime);
            if (!channel.isOpen()
                    || !channel.isActive()
                    || intervalMillis > 90000L) {
                channelMap.remove(channel);
                ChatChannelManage.channelGroup.remove(channel);
                if (channel.isOpen() || channel.isActive()) {
                    channel.close();
                }
            }
        }

    }
}
