package com.octv.im.config;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketConfig {
    @Value("${octv.im-chat-server.port}")
    private Integer port;

    @Value("${octv.im-chat-server.ipAddress}")
    private String ip;

    @ConditionalOnMissingBean
    @Bean
    public WebSocketServerHandshakerFactory getWebSocketServerHandshakerFactory(){
        String url = "ws://" + ip + ":" + port + "/websocket";
        return new WebSocketServerHandshakerFactory(url, null, false);
    }


}
