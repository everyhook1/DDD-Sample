/**
 * @(#)CoordinatorController.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liubin01
 */
@Slf4j
@Service
public class CoordinatorController {

    @Autowired
    private CuratorFramework client;

    private static final String CDC_PATH = "/cdc";

    private static final Map<String, BinlogEntryReaderLeadership> leaderMap
            = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws Exception {
        //设置节点的cache
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, CDC_PATH, true);
        //设置监听器和处理过程
        pathChildrenCache.getListenable().addListener((client, event) -> {
            ChildData data = event.getData();
            if (data != null) {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("NODE_ADDED : " + data.getPath() + " DATA:" + new String(data.getData()));
                        createLeader(data);
                        break;
                    case CHILD_UPDATED:
                        log.info("CHILD_UPDATED : " + data.getPath() + " DATA:" + new String(data.getData()));
                        updateLeader(data);
                        break;
                    case CHILD_REMOVED:
                        log.info("CHILD_REMOVED : " + data.getPath() + " DATA:" + new String(data.getData()));
                        removeLeader(data);
                        break;
                    case INITIALIZED:
                        log.info("INITIALIZED : " + data.getPath() + " DATA:" + new String(data.getData()));
                        break;
                    default:
                        break;
                }
            } else {
                log.info("data is null : " + event.getType());
            }
        });
        //开始监听
        pathChildrenCache.start();
    }

    private void createLeader(ChildData data) {
        if (leaderMap.containsKey(data.getPath())) {
            updateLeader(data);
        } else {
            BinlogEntryReaderLeadership leader =
                    BinlogEntryReaderLeadershipFactory.createLeader(data);
            leaderMap.put(data.getPath(), leader);
        }
    }

    private void updateLeader(ChildData data) {
        if (leaderMap.containsKey(data.getPath())) {
            BinlogEntryReaderLeadership leadership = leaderMap.get(data.getPath());
            if (leadership != null) {
                BinlogEntryReaderLeadershipFactory.updateLeader(data, leadership);
            }

        } else {
            BinlogEntryReaderLeadership leader =
                    BinlogEntryReaderLeadershipFactory.createLeader(data);
            leaderMap.put(data.getPath(), leader);
        }
    }

    private void removeLeader(ChildData data) {
        if (leaderMap.containsKey(data.getPath())) {
            BinlogEntryReaderLeadership leadership = leaderMap.get(data.getPath());
            if (leadership != null) {
                BinlogEntryReaderLeadershipFactory.removeLeader(data,leadership);
            }
            leaderMap.remove(data.getPath());
        }
    }

}
