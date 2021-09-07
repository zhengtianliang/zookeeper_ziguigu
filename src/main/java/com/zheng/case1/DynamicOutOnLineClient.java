package com.zheng.case1;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: ZhengTianLiang
 * @date: 2021/09/06  20:59
 * @desc: zk集群的动态上下线功能的实现(客户端)
 */
public class DynamicOutOnLineClient {

    private String connectionString = "192.168.230.128:2181";
    private int sessionTimeout = 10000;
    private ZooKeeper client;

    public static void main(String[] args) throws Exception {
        DynamicOutOnLineClient dynamic = new DynamicOutOnLineClient();
        // 客户端连接到zk集群中去
        dynamic.getConnection();

        // 客户端去监听zk中的节点
        dynamic.listener();

        // 做业务操作
        dynamic.business();
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  21:06
     * @desc: 做出相应的业务操作
     */
    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  20:59
     * @desc: 客户端去监听zk中的节点
     */
    private void listener() throws KeeperException, InterruptedException {
        List<String> children = client.getChildren("/servers", true);
        List<String> values = new ArrayList<String>();
        for (String child : children) {
            byte[] data = client.getData("/servers/" + child, false, null);
            values.add(new String(data));
        }

        System.out.println(values);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/06  20:59
     * @desc: 客户端连接到zk集群中去
     */
    private void getConnection() throws IOException {
        client = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                // 不断的去监听，因为zk的监听只能监听一次，想要多次监听，得不断的去创建监听器
                try {
                    listener();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
