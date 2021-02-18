/**
 * @(#)CdcDataPublisher.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import java.util.concurrent.CompletableFuture;

/**
 * @author liubin01
 */
public class CdcDataPublisher<EVENT> {
    public CompletableFuture<?> sendMessage(EVENT convert) {
        return null;
    }
}
