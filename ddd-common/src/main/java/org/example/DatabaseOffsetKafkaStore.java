/**
 * @(#)DatabaseOffsetKafkaStore.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example;

import java.util.Optional;

/**
 * @author liubin01
 */
public class DatabaseOffsetKafkaStore implements OffsetStore {

    @Override
    public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        return Optional.empty();
    }

    @Override
    public void save(BinlogFileOffset binlogFileOffset) {

    }
}
