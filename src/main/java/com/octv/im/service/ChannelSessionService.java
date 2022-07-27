package com.octv.im.service;

import io.netty.channel.Channel;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

public interface ChannelSessionService {
    void addChannel(Channel channel,String userID);

    void clearSession(Channel channel);

    void scheduledSendClientPing();

    void scheduledScanNotActiveChannel();


}
