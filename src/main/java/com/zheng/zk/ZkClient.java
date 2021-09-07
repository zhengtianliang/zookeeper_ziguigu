package com.zheng.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author: ZhengTianLiang
 * @date: 2021/09/05  16:23
 * @desc: zk的客户端
 */
public class ZkClient {

    /**
     * 要连接的zk服务器，集群的话，用逗号分割，但是不能有空格。例如：
     *   String s = "hadoop102:2181,hadoop103:2181,hadoop104:2181"        不要有空格
     */
    private String connectString = "192.168.230.128:2181";

    /**
     * 连接超时时间(超过这个时间还连不上，就报错)
     */
    private Integer sessionTimeout = 10000;

    private ZooKeeper zkClient;

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/05  16:24
     * @desc: 初始化操作(连接到zk服务器)
     */
//    @Test
    @Before
    public void init() throws IOException {
        // 三个参数分别代表着  连接的zk服务器、连接超时时间，监听器
        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                List<String> children = null;
                try {
                    children = zkClient.getChildren("/", true);
                    System.out.println("===============================");
                    // 输出一下节点信息
                    for (String s : children){
                        System.out.println(s);
                    }

                    System.out.println("===============================");

                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/05  16:32
     * @desc: 创建一个zk节点
     */
    @Test
    public void create() throws KeeperException, InterruptedException {
        /**
         *   四个参数分别是   要创建的节点的路径、要创建节点的数据、节点的权限、节点的类型
         *         ZooDefs.Ids.OPEN_ACL_UNSAFE      代表任何人都能操作这个节点
         *         CreateMode.PERSISTENT            代表的是永久的无序节点
         */
        zkClient.create("/atguigu","atguigu的value的值".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/05  16:44
     * @desc: 获取子节点并监听节点变化
     *      注意： 这个 zkClient.getChildren("路径",watch)  或者是  zkClient.getChildren("路径",boolean)  是方法重载
     *                  这个方法只能监听  节点下的 节点的变化；不能节点节点的数据的变化
     */
    @Test
    public void getChilederAndListener() throws KeeperException, InterruptedException {
        // 设置成true的话，默认是走的创建zk客户端的监听器(就是38行那个)。也可以自己创建一个watch(再次new Watch)
        List<String> children = zkClient.getChildren("/", true);

        System.out.println("进入了getChilederAndListener方法");
        // 输出一下节点信息
        for (String s : children){
            System.out.println(s);
        }

        System.out.println("getChilederAndListener方法结束了");

        // 延时阻塞，看看效果
        Thread.sleep(Long.MAX_VALUE);
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/05  17:00
     * @desc: 判断某个节点是否存在
     */
    @Test
    public void exist() throws KeeperException, InterruptedException {
        Stat exists = zkClient.exists("/atguigu", false);
        System.out.println(exists);

        System.out.println("：：：："+exists==null?"不存在":"存在");
    }

}
