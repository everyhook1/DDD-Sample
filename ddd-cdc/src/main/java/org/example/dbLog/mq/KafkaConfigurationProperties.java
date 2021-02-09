/**
 * @(#)KafkaConfigurationProperties.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.dbLog.mq;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author liubin01
 */
@Data
@AllArgsConstructor
public class KafkaConfigurationProperties {

    private String bootstrapServers;
    private long connectionValidationTimeout;
}
