/**
 * @(#)BinlogFileOffset.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinlogFileOffset {

    private String binlogFilename;
    private long offset;
    private int rowsToSkip;

    public BinlogFileOffset(String binlogFilename, long offset) {
        this(binlogFilename, offset, 0);
    }

    public boolean isSameOrAfter(BinlogFileOffset binlogFileOffset) {
        if (this.equals(binlogFileOffset))
            return true;
        if (this.getBinlogFilename().equals(binlogFileOffset.getBinlogFilename())) {
            return this.getOffset() > binlogFileOffset.getOffset();
        } else {
            return this.getBinlogFilename().compareTo(binlogFileOffset.getBinlogFilename()) > 0;
        }
    }
}
