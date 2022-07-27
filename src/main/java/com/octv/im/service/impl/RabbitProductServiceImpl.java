package com.octv.im.service.impl;


import static com.octv.im.constant.RabbitKeyConstant.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octv.im.service.RabbitProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;


@Service
@Slf4j
public class RabbitProductServiceImpl implements RabbitProductService {

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println("交换机时间" + LocalDateTime.now());
//        System.out.println("ConfirmCallback     " + "相关数据：" + correlationData);
//        System.out.println("ConfirmCallback     " + "确认情况：" + b);
//        System.out.println("ConfirmCallback     " + "原因：" + s);
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        System.out.println("队列时间" + LocalDateTime.now());
//        System.out.println("ReturnCallback：     " + "消息：" + returnedMessage.getMessage());
//        System.out.println("ReturnCallback：     " + "回应码：" + returnedMessage.getReplyCode());
//        System.out.println("ReturnCallback：     " + "回应消息：" + returnedMessage.getReplyText());
//        System.out.println("ReturnCallback：     " + "交换机：" + returnedMessage.getExchange());
//        System.out.println("ReturnCallback：     " + "路由键：" + returnedMessage.getRoutingKey());
    }

    @Override
    public void sendMessage(String exchange, String routingKey, Object message) {
        Assert.notNull(message, "message can't be NULL");
        Assert.notNull(exchange, "exchange can't be NULL");
       // Assert.notNull(routingKey, "routingKey can't be NULL");
        this.convertAndSend(exchange, routingKey, message);
    }

    private void convertAndSend(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            log.error("RabbitMQ send message error, body:{}, exchange:{}, routingKey:{}", message, exchange, routingKey, e);
            // TODO
        }
    }
}
