/**
 * @(#)BinlogEntry.java, 3月 03, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.binlog;

import org.example.BinlogFileOffset;

/**
 * @author liubin01
 */
public interface BinlogEntry {
    Object getColumn(String name);
    BinlogFileOffset getBinlogFileOffset();

    default String getJsonColumn(String name) {
        return getStringColumn(name);
    }

    default String getStringColumn(String name) {
        Object columnValue = getColumn(name);

        if (columnValue == null) {
            return null;
        }

        if (columnValue instanceof String) return (String) columnValue;

        throw new IllegalArgumentException(String.format("Unexpected type %s of column %s, should be String", columnValue.getClass(), name));
    }
}