package com.zheng.case3;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @author: ZhengTianLiang
 * @date: 2021/9/8  11:18
 * @desc:
 */

public class ItGuiGuDistributedLockTest {

    public static void main(String[] args) throws
            InterruptedException, IOException, KeeperException {
        // 创建分布式锁 1
        final ItGuiGuDistributedLock lock1 = new ItGuiGuDistributedLock();
        // 创建分布式锁 2
        final ItGuiGuDistributedLock lock2 = new ItGuiGuDistributedLock();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取锁对象
                try {
                    lock1.zkLock();
                    System.out.println("线程 1 获取锁");
                    Thread.sleep(5 * 1000);
                    lock1.zkUnlock();
                    System.out.println("线程 1 释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取锁对象
                try {
                    lock2.zkLock();
                    System.out.println("线程 2 获取锁");
                    Thread.sleep(5 * 1000);
                    lock2.zkUnlock();
                    System.out.println("线程 2 释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
