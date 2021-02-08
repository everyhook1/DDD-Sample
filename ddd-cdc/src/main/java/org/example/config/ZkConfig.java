/**
 * @(#)ZkConfig.java, 2月 07, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liubin01
 */
@Configuration
public class ZkConfig {

    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFrameworkFactory(
            @Value("${zk.servers}") String zkServers,
            @Value("${zk.sleepMsBetweenRetries}") int sleepMsBetweenRetries,
            @Value("${zk.maxRetries}") int maxRetries) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkServers,
                new RetryNTimes(maxRetries, sleepMsBetweenRetries));
        client.start();
        return client;
    }
}
