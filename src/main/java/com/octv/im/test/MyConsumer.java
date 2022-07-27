/*
package com.octv.im.test;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class MyConsumer {
    //队列中存在消息则立即回调该方法
    @RabbitListener(queues = {"test_queue"})
    public void listener(Message message, Channel channel) throws IOException {
        System.out.println("接收到消息为 <<---------------时间" + LocalDateTime.now());
        System.out.println(message.getBody().toString());
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
*/
