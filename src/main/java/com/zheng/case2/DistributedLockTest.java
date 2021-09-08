package com.zheng.case2;

/**
 * @author: ZhengTianLiang
 * @date: 2021/09/07  22:38
 * @desc: zk实现分布式锁的测试类
 */
public class DistributedLockTest {

    public static void main(String[] args) throws Exception {
        final DistributedLock lock1 = new DistributedLock();
        final DistributedLock lock2 = new DistributedLock();

        new Thread(new Runnable() {
            public void run() {
                try {
                    lock1.zkLock();
                    System.out.println("线程1 启动，获取到锁");
                    Thread.sleep(4);

                    lock1.unZkLock();
                    System.out.println("线程1 结束，释放锁");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    lock2.zkLock();
                    System.out.println("线程2 启动，获取到锁");
                    Thread.sleep(4);

                    lock2.unZkLock();
                    System.out.println("线程2 结束，释放锁");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }
}
