package com.octv.im.controller;

import com.octv.im.entity.ChatHistoryBean;
import com.octv.im.entity.DTO.ChatHistoryDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * 消息控制器，获取消息历史记录
 *
 * @Author
 * @Description
 */
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    //查询某个聊天室的消息历史记录
    @GetMapping(value = "/group_history/{group_id}")
    public ChatHistoryBean getGroupMessageHistory(@PathVariable("group_id") String groupID,
                                                  ChatHistoryDTO chatHistoryDTO) {
        System.out.println(groupID +"ZZZZZZZZZ");
        return new ChatHistoryBean();
    }

    //查询某个用户的消息历史记录
    @GetMapping(value = "/user_history")
    public String getUserMessageHistory(@PathVariable("user_id") String userID,
                                                 ChatHistoryDTO chatHistoryDTO) {
        System.out.println(userID +"ppppppppp");
        return "SUCCESS";
    }


}
