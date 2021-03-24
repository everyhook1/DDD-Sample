/**
 * @(#)OrderEvent.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.config.statemachine.simpleOrder;

/**
 * @author liubin01
 */
public enum OrderEvent {

    START_EVENT,
    ACCOUNT_SUBMIT_SUCCESS_EVENT,
    ACCOUNT_SUBMIT_FAIL_EVENT,
    ORDER_CANCEL_FAIL_EVENT,
    ORDER_CANCEL_SUCCESS_EVENT,
    ;
}
