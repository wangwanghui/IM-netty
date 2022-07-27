package com.octv.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "octv.im-chat-server")
@Data
public class ChatServerProperties {
    private Integer port;

    private String ipAddress;

    private Long intervalMillis;

    private Boolean openCluster;
}
