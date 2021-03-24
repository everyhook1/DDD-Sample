package org.saga.instance;

import org.saga.entity.Job;
import org.springframework.statemachine.StateMachine;

public interface SagaInstance<S, E> {

    Job createJob();

    StateMachine<S, E> getStateMachine(long jobId);
}
