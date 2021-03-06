/**
 * @(#)SchemaAndTable.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaAndTable {

    private String schema;
    private String tableName;
}
