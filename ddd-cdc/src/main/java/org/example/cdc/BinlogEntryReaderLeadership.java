/**
 * @(#)BinlogEntryReaderLeadership.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author liubin01
 */
@Slf4j
public class BinlogEntryReaderLeadership {

    private String leaderLockId;
    private ZkLeaderSelectorFactory leaderSelectorFactory;
    private BinlogEntryReader binlogEntryReader;

    private ZkLeaderSelector zkLeaderSelector;
    private volatile boolean leader;
    private ZkLeaderSelector.LeadershipController leadershipController;

    public BinlogEntryReaderLeadership(String leaderLockId,
                                       ZkLeaderSelectorFactory leaderSelectorFactory,
                                       BinlogEntryReader binlogEntryReader) {
        this.leaderLockId = leaderLockId;
        this.leaderSelectorFactory = leaderSelectorFactory;
        this.binlogEntryReader = binlogEntryReader;
        binlogEntryReader.setRestartCallback(this::restart);
    }

    public void start() {
        log.info("Starting BinlogEntryReaderLeadership");
        zkLeaderSelector = leaderSelectorFactory.create(leaderLockId,
                UUID.randomUUID().toString(),
                this::leaderSelectedCallback,
                this::leaderRemovedCallback);
        zkLeaderSelector.start();
    }

    private void leaderSelectedCallback(ZkLeaderSelector.LeadershipController leadershipController) {
        log.info("Assigning leadership");
        this.leadershipController = leadershipController;
        leader = true;
        new Thread(binlogEntryReader::start).start();
        log.info("Assigned leadership");
    }

    private void leaderRemovedCallback() {
        log.info("Resigning leadership");
        leader = false;
        binlogEntryReader.stop(false);
        log.info("Resigned leadership");
    }

    public void stop() {
        log.info("Stopping BinlogEntryReaderLeadership");
        binlogEntryReader.stop();
        zkLeaderSelector.stop();
        log.info("Stopped BinlogEntryReaderLeadership");
    }

    public BinlogEntryReader getBinlogEntryReader() {
        return binlogEntryReader;
    }

    public boolean isLeader() {
        return leader;
    }

    private void restart() {
        log.info("Restarting BinlogEntryReaderLeadership");
        leadershipController.stop();
        log.info("Restarted BinlogEntryReaderLeadership");
    }
}
