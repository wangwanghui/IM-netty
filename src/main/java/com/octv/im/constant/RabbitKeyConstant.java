package com.octv.im.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Configuration
public class RabbitKeyConstant {

    public static final String GROUP_CHAT_EXCHANGE = "group.chat.exchange";
    public static final String GROUP_CHAT_QUEUE = "group_chat_queue";
    public static final String GROUP_CHAT_ROUT_KEY = "group";
    public static final String PERSON_CHAT_EXCHANGE = "person.chat.exchange";

    public static final String PERSON_CHAT_QUEUE = "person_chat_queue";

    public static final String PERSON_CHAT_ROUT_KEY = "person";

    public static final String CLUSTER_MODEL_DISPATCH_MESSAGE_EXCHANGE = "cluster.model.exchange";

}
