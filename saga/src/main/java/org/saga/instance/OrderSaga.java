/**
 * @(#)OrderSaga.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.instance;

import lombok.extern.slf4j.Slf4j;
import org.saga.config.statemachine.simpleOrder.OrderEvent;
import org.saga.config.statemachine.simpleOrder.OrderState;
import org.saga.dao.JobRepository;
import org.saga.entity.Job;
import org.saga.entity.SagaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author liubin01
 */
@Slf4j
@Service
public class OrderSaga implements SagaInstance<OrderState, OrderEvent> {

    @Autowired
    private StateMachineService<OrderState, OrderEvent> orderStateMachineService;

    @Autowired
    private JobRepository jobRepository;

    @Transactional
    @Override
    public Job createJob() {
        String machineId = UUID.randomUUID().toString();
        StateMachine<OrderState, OrderEvent> stateMachine = orderStateMachineService.acquireStateMachine(machineId);
        stateMachine.startReactively();
        Job job = new Job();
        job.setMachineId(machineId);
        job.setSagaType(SagaType.ORDER.ordinal());
        return jobRepository.save(job);
    }

    @Override
    public StateMachine<OrderState, OrderEvent> getStateMachine(long jobId) {

        Job job = jobRepository.findById(jobId).orElseThrow(RuntimeException::new);
        return orderStateMachineService.acquireStateMachine(job.getMachineId(), false);
    }
}
