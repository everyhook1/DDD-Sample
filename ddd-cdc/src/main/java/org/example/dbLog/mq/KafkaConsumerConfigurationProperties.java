/**
 * @(#)KafkaConsumerConfigurationProperties.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.dbLog.mq;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liubin01
 */
@Data
public class KafkaConsumerConfigurationProperties {

    private Map<String, String> properties = new HashMap<>();
    private int low = 0;
    private int high = Integer.MAX_VALUE;
    private long pollTimeout;

    public static KafkaConsumerConfigurationProperties empty() {
        return new KafkaConsumerConfigurationProperties();
    }
}
