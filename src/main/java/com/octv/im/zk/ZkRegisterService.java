/*
package com.octv.im.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class ZkRegisterService implements InitializingBean {
    private static final String BASE_SERVICES = "/server";
    private static final String SERVICE_NAME = "/netty-websocket";

    @Override
    public void afterPropertiesSet() throws Exception {
        register("127.0.0.1", 7979);
    }


    public static void register(String address, int port) {
        try {
// 创建CuratorFramework实例，封装与ZooKeeper的操作。
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient
                    ("127.0.0.1:2181",
                    new RetryNTimes(4, 2000));
            curatorFramework.start();
// 创建ServiceInstanceBuilder实例，指定服务名称和地址，
// 由ServiceInstanceBuilder实例构建ServiceInstance实例，ServiceInstance实例即服务。
            ServiceInstanceBuilder serviceInstanceBuilder = ServiceInstance.builder();
           // serviceInstanceBuilder.uriSpec(new UriSpec("ws://{address}:{port}"));
            serviceInstanceBuilder.address("127.0.0.1");
            serviceInstanceBuilder.port(7979);
            serviceInstanceBuilder.name("netty-websocket");
            ServiceInstance serviceInstance = serviceInstanceBuilder.build();
// 创建serviceDiscoveryBuilder实例，指定服务和服务在Zookeeper中所在路径，
// 由ServiceDiscoveryBuilder实例构建ServiceDiscovery实例，由ServiceDiscovery实例注册服务。
            ServiceDiscoveryBuilder serviceDiscoveryBuilder = ServiceDiscoveryBuilder.builder(Void.class);
            serviceDiscoveryBuilder.basePath("/server");
            serviceDiscoveryBuilder.client(curatorFramework);
            serviceDiscoveryBuilder.build().registerService(serviceInstance);
           // serviceDiscovery.start();
//注册服务
            //serviceDiscovery.registerService(serviceInstance);
            */
/* 服务注册 end *//*

// 等待30秒，这时在ZooKeeper中可以查看到注册的服务
            Thread.sleep(30000);
// 30秒后，进程关闭，ZooKeeper中注册的服务因是临时节点而被删除，说明服务已不可用
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
*/
