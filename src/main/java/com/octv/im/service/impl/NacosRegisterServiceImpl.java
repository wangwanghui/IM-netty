package com.octv.im.service.impl;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingService;
import com.octv.im.config.ChatServerProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class NacosRegisterServiceImpl implements InitializingBean {
    @Autowired(required = false)
    private NacosServiceManager nacosServiceManager;

    @Autowired(required = false)
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    private ChatServerProperties chatServerProperties;

    @Value("${spring.cloud.client.ip-address}")
    private String discoveryIp;



    @Override
    public void afterPropertiesSet() throws Exception {
        registerWebSocketToNacos();
    }

    private void registerWebSocketToNacos(){
        System.out.println("AAAAAAAAAAAAAA");
        try {
            if (nacosServiceManager != null) {
                System.out.println("BBBBBBBBBBBBB" +nacosDiscoveryProperties.getIp());
                NamingService namingService = nacosServiceManager
                        .getNamingService(nacosDiscoveryProperties.getNacosProperties());
                namingService.registerInstance(nacosDiscoveryProperties.getService() + "-socket",
                        nacosDiscoveryProperties.getGroup(), nacosDiscoveryProperties.getIp(),
                        chatServerProperties.getPort(), nacosDiscoveryProperties.getClusterName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
