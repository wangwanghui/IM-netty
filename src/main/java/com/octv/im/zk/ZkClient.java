/*
package com.octv.im.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class ZkClient implements InitializingBean {
    private String registryAddress = "127.0.0.1:2181";
    private final int timeout = 4000;

    //这个可以放到配置文件里，对应Zookeeper已经启动的ip+port
    private volatile List<String> serviceAddressList = new ArrayList<>();

    private String nodePath = "/zkServer";

    private String childNodePath = "/zk1";

    private ZooKeeper zk = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        zk = connectServer();
        if (zk == null) {
            log.error("zookeeper start fail..");
            return;
        }
        createNode("192.168.1.20");
    }

    */
/**
     * 服务发现
     *
     * @return
     *//*

    public String discover() {
        String data = null;
        int size = serviceAddressList.size();
        if (size > 0) {
            if (size == 1) {
                //只有一个服务提供方
                data = serviceAddressList.get(0);
                log.info("unique service address :{}", data);
            } else {
                //使用随机分配法,简单的负载均衡法
                data = serviceAddressList.get(ThreadLocalRandom.current().nextInt(size));
                log.info("choose an address : {}", data);
            }
        }
        return data;
    }

    */
/**
     * 监听节点
     *
     * @return
     *//*

    public void watchNode() {
        try {
            //获取子节点列表
            List<String> nodeList = zk.getChildren(nodePath,true);*/
/*nodePath, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        // 发生子节点变化时再次调用此方法更新服务地址
                        System.out.println("节点变化" + watchedEvent.getPath());
                        watchNode();
                    }
                }
            });*//*

            System.out.println("节点变化");
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(nodePath + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            log.info("node data: {}", dataList);
            this.serviceAddressList = dataList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    */
/**
     * 连接 zookeeper 服务器
     *
     * @return
     *//*

    private ZooKeeper connectServer() {
        try {
            zk = new ZooKeeper(registryAddress, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        // latch.countDown();
                        System.out.println("Watcher.........");
                        watchNode();
                    }

                }
            });
            //latch.await();
        } catch (IOException e) {
            log.error("", e);
            e.printStackTrace();
        }
        return zk;
    }

    */
/**
     * 创建节点
     *
     * @param zk
     * @param data
     *//*

    private void createNode(String data) {
        try {
            //父节点不存在时进行创建
            Stat stat = zk.exists(nodePath, true);
            if (stat == null) {
                zk.create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            //CreateMode.EPHEMERAL_SEQUENTIAL,创建临时顺序节点,客户端会话结束后，节点将会被删除
            String createPath = zk.create(nodePath + childNodePath, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info("create zookeeper node ({} =&gt; {} =&gt; {})", data, createPath);
        } catch (KeeperException | InterruptedException e) {
            log.info("", e);
            e.printStackTrace();
        }
    }


}
*/
