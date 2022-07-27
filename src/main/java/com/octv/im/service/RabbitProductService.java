package com.octv.im.service;

import com.octv.im.entity.ChatMessageBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public interface RabbitProductService extends RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback{
    void sendMessage(String exchange, String routingKey, Object message);
}