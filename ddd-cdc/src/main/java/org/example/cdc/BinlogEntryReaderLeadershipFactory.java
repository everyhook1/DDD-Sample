/**
 * @(#)BinlogEntryReaderLeadershipFactory.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;

/**
 * @author liubin01
 */
@Slf4j
public class BinlogEntryReaderLeadershipFactory {

    public static BinlogEntryReaderLeadership createLeader(ChildData data) {
        log.info("createLeader {}", data);
        BinlogEntryReaderLeadership leadership;// = new BinlogEntryReaderLeadership();
        return null;
    }

    public static void updateLeader(ChildData data, BinlogEntryReaderLeadership leadership) {
        log.info("updateLeader {}", data);
    }

    public static void removeLeader(ChildData data, BinlogEntryReaderLeadership leadership) {
        log.info("updateLeader {}", data);
        leadership.stop();
    }
}
