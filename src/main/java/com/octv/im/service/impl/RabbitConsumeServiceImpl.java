package com.octv.im.service.impl;

import static com.octv.im.constant.RabbitKeyConstant.*;
import static com.octv.im.constant.RedisKeyConst.OFFLINE_CHAT_PERSON_KEY_PREFIX;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octv.im.config.ChatServerProperties;
import com.octv.im.entity.ChatMessageBean;
import com.octv.im.service.RabbitConsumeService;
import com.octv.im.util.ChatChannelManage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@Slf4j
public class RabbitConsumeServiceImpl implements RabbitConsumeService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ChatServerProperties chatServerProperties;
    @Autowired
    private WebSocketServiceImpl webSocketService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    //处理个人消息最后慢慢存入到mysql
    @RabbitListener(containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(value = @Queue(value = PERSON_CHAT_QUEUE, autoDelete = "false", durable = "true"),
                    exchange = @Exchange(value = PERSON_CHAT_EXCHANGE, type = ExchangeTypes.DIRECT), key = PERSON_CHAT_ROUT_KEY))
    @Override
    public void personalMessageConsume(Message message, Channel channel) {
        try {
            String msg = new String(message.getBody(), "UTF-8");
            System.out.println("MQ在 +" + LocalDateTime.now() + "+收到的消息为 ： " + msg);
            ChatMessageBean chatMessageBean = objectMapper.readValue(msg, ChatMessageBean.class);
            //@TODO 存入mysql
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("personal message consume fail exchange {}, queue {}  message {}", PERSON_CHAT_EXCHANGE, PERSON_CHAT_QUEUE, String.valueOf(message.getBody()), e);
        }
    }


    //监听群聊消息，如果有大量消息可以先存到MQ 最后慢慢存入到mysql
    @RabbitListener(containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(value = @Queue(value = GROUP_CHAT_QUEUE, autoDelete = "false", durable = "true"),
                    exchange = @Exchange(value = GROUP_CHAT_EXCHANGE, type = ExchangeTypes.DIRECT), key = GROUP_CHAT_ROUT_KEY))
    @Override
    public void groupMessageConsume(Message message, Channel channel) {
        try {
            String msg = new String(message.getBody(), "UTF-8");
            ChatMessageBean chatMessageBean = objectMapper.readValue(msg, ChatMessageBean.class);
            if (chatMessageBean != null) {
                //@TODO 存入mysql
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            log.error("group message consume fail exchange {}, queue {}  message {}", GROUP_CHAT_EXCHANGE, GROUP_CHAT_QUEUE, String.valueOf(message.getBody()), e);
        }
    }

    //
    @RabbitListener(containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(value = @Queue(value = "#{queue.name}", autoDelete = "false", durable = "true"),
                    exchange = @Exchange(value = CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE, type = ExchangeTypes.FANOUT)))
    @Override
    public void clusterMessageDispatch(Message message, Channel channel) {
        try {
            String msg = new String(message.getBody(), "UTF-8");
            System.out.println("clusterMessageDispatchMQ在 +" + LocalDateTime.now() + "+收到的消息为 ： " + msg);
            ChatMessageBean chatMessageBean = objectMapper.readValue(msg, ChatMessageBean.class);
            if (chatMessageBean != null) {
                //@TODO 存入mysql
                log.info("该用户在这个服务器上 {} ", channel);
                if (chatMessageBean.getCode() != 1) {
                    ChatChannelManage.channelGroup.writeAndFlush(new TextWebSocketFrame(msg));
                    String key = StringUtils.joinWith(".", OFFLINE_CHAT_PERSON_KEY_PREFIX, chatMessageBean.getReceiverUserId());
                    redisTemplate.opsForZSet().remove(key, chatMessageBean);
                } else {
                    String receiveUserID = chatMessageBean.getReceiverUserId();
                    io.netty.channel.Channel receiveChannel = ChatChannelManage.channelMap.get(receiveUserID);
                    if (receiveChannel != null) {
                        receiveChannel.writeAndFlush(new TextWebSocketFrame(msg));
                    }
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            log.error("group message consume fail exchange {}, queue {}  message {}", GROUP_CHAT_EXCHANGE, GROUP_CHAT_QUEUE, String.valueOf(message.getBody()), e);
        }
    }

}