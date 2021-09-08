package com.zheng.case2;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: ZhengTianLiang
 * @date: 2021/09/07  21:17
 * @desc: zk实现分布式锁的代码实现
 * zk实现分布式锁原理：每来一个客户端就往 /locks 这个节点下创建 临时有序节点
 * 然后第一个临时有序节点有锁，其他的每个临时有序节点都监听它的上一个节点(第二监听第一，第三监听第二)
 * 第一个执行完以后，释放锁，第二个节点变成了第一个节点，继续执行业务操作。循环如此
 */


public class DistributedLock {

//    private final String connectionString = "192.168.230.128:2181";
    private final String connectionString = "10.3.7.185:2181";
    private final int sessionTimeout = 10000;
    private ZooKeeper zkClient;

    // 等待zk建立起来链接了，才执行下一步
    // 这个 new CountDownLatch(n) 代表着 每一个线程执行完毕，就将计数器减1，当计数器的值变为0时
    // 在 CountDownLatch上await()的线程就会被唤醒
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    // 等待监听结束以后，才进行下一步
    private CountDownLatch waitLatch = new CountDownLatch(1);

    // 当前节点的前一个节点
    private String waitPath;
    // 当前节点
    private String currentMode;

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/07  21:18
     * @desc: 构造方法，做一些初始化的操作
     */
    public DistributedLock() throws Exception {
        zkClient = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                // 能进行到这一步，则说明，他监听到了前一个节点已经下线了，轮到他获取锁了
                // 名字叫做countDownLatch的CountDownLatch对象需要释放掉，因为此时已经链接上zk服务器了
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected){ // 如果链接上了，就-1
                    countDownLatch.countDown();
                }
                // 名字叫做waitLatch的CountDownLatch对象需要释放掉，因为已经监听完了，不能再等下个线程执行完了
                if (watchedEvent.getType()==Event.EventType.NodeDataChanged &&
                        watchedEvent.getPath().equals(waitPath)){ // 如果你是删除节点，还是删除的前一个节点，
                    waitLatch.countDown();
                }


            }
        });

        // 是说上面的线程执行完了以后，才继续往下走，没执行完，就卡着
        // (因为构造里面写的1，所以是等一个线程执行完了，才会走；若写的2，则是等2个线程执行完了才往后走)
        countDownLatch.await();

        // 判断 /locks 是否存在，若不存在，则创建出来这个节点
        Stat stat = zkClient.exists("/locks", false);
        if (stat == null) {
            zkClient.create("/locks", "locks".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/07  21:18
     * @desc: 对zk加锁
     */
    public void zkLock() {
        try {
            // 创建对应的临时有序节点
            currentMode = zkClient.create("/locks/" + "seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL);
            // 判断创建的节点是否是最小的序号节点。如果是，获取到锁；如果不是，监听它的上一个节点
            List<String> children = zkClient.getChildren("/locks", false);
            if (children == null) {
                System.out.println("=======sorry，俺现在一个子节点也没有哇！");
            }
            // 如果children 只有一个值，则直接获取锁；如果有多个节点，判断谁最小
            if (children.size() == 1) {
                return;
            } else {
                // todo: 我觉得这部没有意义，拿到的是value，但是新增的时候，value就是null
                Collections.sort(children);

                // 获取到节点的名称
                String thisNode = currentMode.substring("/locks/".length());
                // 判断一个当前节点(上面刚刚新增的临时有序节点)在整个child中占什么位置
                int index = children.indexOf(thisNode);
                if (index == -1) {
                    System.out.println("说明不存在？不可能有这种情况啊，上面刚特么新增的");
                }else if(index == 0){ // 说明他就是第一位，直接获取锁
                    return;
                }else { // 他不是第一个，需要监听它上面的那个节点的变化
                    // 拿到他上面的那个节点的路径
                    waitPath = "/locks/" + children.get(index - 1);
                    zkClient.getData(waitPath, true, null);

                    // 等待监听
                    waitLatch.await();

                    // 监听结束以后，就完成了
                    return;
                }
            }
            //

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: ZhengTianLiang
     * @date: 2021/09/07  21:18
     * @desc: 对zk去锁
     */
    public void unZkLock()  {
        try {
            zkClient.delete(currentMode,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

}
