/**
 * @(#)ZkLeaderSelectorFactory.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

/**
 * @author liubin01
 */
public interface ZkLeaderSelectorFactory {

    ZkLeaderSelector create(String lockId,
                            String leaderId,
                            ZkLeaderSelector.LeaderSelectedCallback leaderSelectedCallback,
                            Runnable leaderRemovedCallback);
}
