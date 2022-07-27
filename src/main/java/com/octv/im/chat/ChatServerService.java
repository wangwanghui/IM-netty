package com.octv.im.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@Slf4j
public class ChatServerService implements InitializingBean {
    @Autowired
    private Executor executor;

    @Value("${octv.im-chat-server.port}")
    private Integer port;

    @Value("${octv.im-chat-server.ipAddress}")
    private String ip;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (port == null) {
            return;
        }
        //String url = "ws://" + ip + ":" + port + "/websocket";
        ChatServer chatServer = new ChatServer(port);
        System.out.println("im 聊天室服务启动中");
        log.info("im chat room server start...");
        executor.execute(chatServer);
    }


}
