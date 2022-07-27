package com.octv.im.controller;

import com.octv.im.entity.ChatHistoryBean;
import com.octv.im.entity.DTO.ChatHistoryDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {


    //查询某个聊天室的消息历史记录
    @GetMapping(value = "/A")
    public String getGroupMessageHistory() {
        System.out.println("test");
        return "success";
    }

}
