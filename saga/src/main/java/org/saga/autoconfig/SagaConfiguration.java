/**
 * @(#)SagaConfiguration.java, 3月 23, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author liubin01
 */
@Configuration
@ConditionalOnProperty(prefix = "saga.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SagaConfiguration {

}
