/**
 * @(#)SagaManager.java, 3月 17, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineData;

/**
 * @author liubin01
 */
public class SagaManager<T extends StateMachine<T, S, E, C>, S, E, C> {

    private OnReplyConsumer consumer;

    private Publisher publisher;

    StateMachineBuilder<T, S, E, C> builder;

    public void create() {

    }

    private void dumpData() {

    }

    private void loadData(StateMachineData.Reader<T, S, E, C> savedData) {

    }
}
