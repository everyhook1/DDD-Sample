/**
 * @(#)CdcObserver.java, 2月 25, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.Controller;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * @author liubin01
 */
@Service
public class CdcObserver implements CommandLineRunner {

    @Autowired
    private CuratorFramework client;

    @Override
    public void run(String... args) throws Exception {

    }
}
