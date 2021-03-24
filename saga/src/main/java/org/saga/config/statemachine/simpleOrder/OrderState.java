package org.saga.config.statemachine.simpleOrder;

public enum OrderState {
    START,
    ORDER_CANCEL,
    ACCOUNT_SUBMIT,
    TIME_OUT,
    COMPLETE,
    FAIL,
    ;
}
