package com.octv.im.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class XiaDanController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

   // @Scheduled(cron = "*/10 * * * * ?")
    public String xiadan(){
        Map<String,Object> map=new HashMap<>();
        map.put("msg","库存-1");
        map.put("aaa","支付宝余额-100");
        //fyx01：交换机名称
        System.out.println("在这个时候 发送成功  ----------->" + LocalDateTime.now());
        rabbitTemplate.convertAndSend("test_exchange","test", JSON.toJSONString(map)); //序列化过程
        //rabbitTemplate.convertAndSend("fyx01","","hello 下单成功");
        return "下单成功";
    }
}
