package org.example.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class CoordinatorControllerTest {

    private static final String zkServers = "localhost";
    static CuratorFramework client;

    @BeforeAll
    public static void init() {
        client = CuratorFrameworkFactory.newClient(zkServers,
                new RetryNTimes(3, 100));
        client.start();
    }

    @Test
    public void createTest() throws Exception {
        String path = "/cdc";
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
    }

    @Test
    public void createChild() throws Exception {
        String path = "/cdc/child1";

        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好1".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好2".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好3".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好4".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好5".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好6".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好7".getBytes());
        client.create().orSetData().withMode(CreateMode.EPHEMERAL).forPath(path, "你好8".getBytes());


        byte[] bytes = client.getData().forPath(path);
        System.out.println(new String(bytes));
    }

    @Test
    public void testLeader() throws InterruptedException {
        int cnt = 12;
        Thread[] t = new Thread[cnt];
        for (int i = 0; i < cnt; i++) {
            t[i] = new Thread(new RLeader(i));
            t[i].start();
        }
        for (Thread thread : t) {
            thread.join();
        }
        Thread.sleep(100_000);
    }


    static class RLeader implements Runnable {

        int id;

        RLeader(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            LeaderSelector leaderSelector = new LeaderSelector(client, "/cdc/leader", new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework client) throws Exception {
                    log.info("选举人:{} 成功", id);
                    Thread.sleep(1000);
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    log.info("选举人:{},状态变更{}", id, newState);
                }
            });

            leaderSelector.autoRequeue();
            leaderSelector.start();
        }
    }
}

