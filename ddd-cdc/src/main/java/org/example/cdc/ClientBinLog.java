/**
 * @(#)ClientBinLog.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import lombok.Data;

/**
 * @author liubin01
 */
@Data
public class ClientBinLog {

    private String hostname;
    private int port;
    private String schema;
    private String username;
    private String password;
}
