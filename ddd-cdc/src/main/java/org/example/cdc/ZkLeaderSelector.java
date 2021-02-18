/**
 * @(#)ZkLeaderSelector.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author liubin01
 */
@Slf4j
public class ZkLeaderSelector implements CdcLeaderSelector {

    private final CuratorFramework curatorFramework;
    private final String lockId;
    private final String leaderId;
    private final LeaderSelectedCallback leaderSelectedCallback;
    private final Runnable leaderRemovedCallback;
    private LeaderSelector leaderSelector;

    public ZkLeaderSelector(CuratorFramework curatorFramework,
                            String lockId,
                            LeaderSelectedCallback leaderSelectedCallback,
                            Runnable leaderRemovedCallback) {

        this(curatorFramework, lockId, UUID.randomUUID().toString(), leaderSelectedCallback, leaderRemovedCallback);
    }

    public ZkLeaderSelector(CuratorFramework curatorFramework,
                            String lockId,
                            String leaderId,
                            LeaderSelectedCallback leaderSelectedCallback,
                            Runnable leaderRemovedCallback) {
        this.curatorFramework = curatorFramework;
        this.lockId = lockId;
        this.leaderId = leaderId;
        this.leaderSelectedCallback = leaderSelectedCallback;
        this.leaderRemovedCallback = leaderRemovedCallback;
    }

    @Override
    public void start() {
        log.info("Starting leader selector");
        leaderSelector = new LeaderSelector(curatorFramework, lockId, new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework client) {
                CountDownLatch stopCountDownLatch = new CountDownLatch(1);

                try {
                    log.info("Calling leaderSelectedCallback, leaderId : {}", leaderId);
                    leaderSelectedCallback.run(new ZkLeadershipController(stopCountDownLatch));
                    log.info("Called leaderSelectedCallback, leaderId : {}", leaderId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    log.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
                    leaderRemovedCallback.run();
                    log.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
                    return;
                }
                try {
                    stopCountDownLatch.await();
                } catch (InterruptedException e) {
                    log.error("Leadership interrupted", e);
                }
                try {
                    log.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
                    leaderRemovedCallback.run();
                    log.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                log.info("StateChanged, state : {}, leaderId : {}", newState, leaderId);
                if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
                    throw new CancelLeadershipException();
                }
            }
        });

        leaderSelector.autoRequeue();
        leaderSelector.start();
        log.info("Started leader selector");
    }

    @Override
    public void stop() {
        log.info("Closing leader selector, leaderId : {}", leaderId);
        leaderSelector.close();
        log.info("Closed leader selector, leaderId : {}", leaderId);
    }

    public interface LeaderSelectedCallback {
        void run(LeadershipController leadershipController);
    }

    public interface LeadershipController {
        void stop();
    }

    public static class ZkLeadershipController implements LeadershipController {

        private final CountDownLatch stopCountDownLatch;

        public ZkLeadershipController(CountDownLatch stopCountDownLatch) {
            this.stopCountDownLatch = stopCountDownLatch;
        }

        @Override
        public void stop() {
            log.info("Stopping leadership controller");
            stopCountDownLatch.countDown();
            log.info("Stopped leadership controller");
        }
    }
}
