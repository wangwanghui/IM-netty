package com.octv.im.service;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

public interface RabbitConsumeService {

    void personalMessageConsume(Message message, Channel channel);

    void groupMessageConsume(Message message, Channel channel);

    void clusterMessageDispatch(Message message, Channel channel);

}
