package com.zheng.case1;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * @author: ZhengTianLiang
 * @date: 2021/09/06  20:37
 * @desc: zk集群的动态上下线功能的实现(服务器端)
 *      原理是：服务器端往zk集群中注册/销毁。客户端去监听zk集群的节点的变化，
 *      从而达到监控服务器端节点动态上下线的功能
 */
public class DynamicOutOnLineServer {

    // zk集群的地址及端口，多个之间用，分割
    private String connectionString = "192.168.230.128:2181";

    // 程序链接到zk的超时时间，10秒钟
    private int sessionTimeout = 10000;

    private ZooKeeper zkClient;

    public static void main(String[] args) throws Exception {
        DynamicOutOnLineServer dynamic = new DynamicOutOnLineServer();
        // 链接到zk集群
        dynamic.getConnection();

        // 服务器注册到zk集群中
        dynamic.register(args[0]);

        // 启动业务功能
        dynamic.business(args[0]);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  20:48
     * @desc: 做出相应的业务功能
     */
    private void business(String arg) throws InterruptedException {
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  20:46
     * @desc: 服务器端注册到zk集群中
     */
    private void register(String hostName) throws KeeperException, InterruptedException {
        String s = zkClient.create("/servers/"+hostName, hostName.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(hostName + "成功的注册进了zk集群中");
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  20:42
     * @desc: 让程序建立和zk集群的链接
     */
    private void getConnection() throws IOException {
        zkClient = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {

            }
        });
    }

}
