package org.example;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;

public class Main4 {
    public static void main(String[] args) throws Exception {
        // Zookeeper集群地址，多个节点地址可以用逗号分隔
        String zkAddress = "127.0.0.1:2181";
        // 重试策略，如果连接不上ZooKeeper集群，会重试三次，重试间隔会递增
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 创建Curator Client并启动，启动成功之后，就可以与Zookeeper进行交互了
        CuratorFramework client = CuratorFrameworkFactory
                .newClient(zkAddress, retryPolicy);
        client.start();
        try {
            client.create().withMode(CreateMode.PERSISTENT)
                    .forPath("/user", "test".getBytes());
        } catch (Exception e) {
        }
        // 这里通过usingWatcher()方法添加一个Watcher
        List<String> children = client.getChildren().usingWatcher(
                new CuratorWatcher() {
                    public void process(WatchedEvent event) throws Exception {
                        System.out.println(event.getType() + "," +
                                event.getPath());
                    }
                }).forPath("/user");
        System.out.println(children);
        System.in.read();
    }
}