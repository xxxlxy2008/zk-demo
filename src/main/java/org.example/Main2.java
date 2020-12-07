package org.example;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class Main2 {
    public static void main(String[] args) throws Exception {
        // Zookeeper集群地址，多个节点地址可以用逗号分隔
        String zkAddress = "127.0.0.1:2181";
        // 重试策略，如果连接不上ZooKeeper集群，会重试三次，重试间隔会递增
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 创建Curator Client并启动，启动成功之后，就可以与Zookeeper进行交互了
        CuratorFramework client = CuratorFrameworkFactory
                .newClient(zkAddress, retryPolicy);
        client.start();
        // 添加CuratorListener监听器，针对不同的事件进行处理
        client.getCuratorListenable().addListener(
                new CuratorListener() {
                    public void eventReceived(CuratorFramework client,
                                              CuratorEvent event) throws Exception {
                        switch (event.getType()) {
                            case CREATE:
                                System.out.println("CREATE:" +
                                        event.getPath());
                                break;
                            case DELETE:
                                System.out.println("DELETE:" +
                                        event.getPath());
                                break;
                            case EXISTS:
                                System.out.println("EXISTS:" +
                                        event.getPath());
                                break;
                            case GET_DATA:
                                System.out.println("GET_DATA:" +
                                        event.getPath() + ","
                                        + new String(event.getData()));
                                break;
                            case SET_DATA:
                                System.out.println("SET_DATA:" +
                                        new String(event.getData()));
                                break;
                            case CHILDREN:
                                System.out.println("CHILDREN:" +
                                        event.getPath());
                                break;
                            default:
                        }
                    }
                });
        // 注意:下面所有的操作都添加了inBackground()方法，转换为后台操作
        client.create().withMode(CreateMode.PERSISTENT)
                .inBackground().forPath("/user", "test".getBytes());
        client.checkExists().inBackground().forPath("/user");
        client.setData().inBackground().forPath("/user",
                "setData-Test".getBytes());
        client.getData().inBackground().forPath("/user");
        for (int i = 0; i < 3; i++) {
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .inBackground().forPath("/user/child-");
        }
        client.getChildren().inBackground().forPath("/user");
        // 添加BackgroundCallback
        client.getChildren().inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework client,
                                      CuratorEvent event) throws Exception {
                System.out.println("in background:"
                        + event.getType() + "," + event.getPath());
            }
        }).forPath("/user");

        client.delete().deletingChildrenIfNeeded().inBackground()
                .forPath("/user");
        System.in.read();
    }
}
// 输出：
// CREATE:/user
// EXISTS:/user
// GET_DATA:/user,setData-Test
// CREATE:/user/child-
// CREATE:/user/child-
// CREATE:/user/child-
// CHILDREN:/user
// DELETE:/user