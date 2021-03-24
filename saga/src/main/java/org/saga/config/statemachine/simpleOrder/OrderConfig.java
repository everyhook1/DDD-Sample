/**
 * @(#)OrderConfig.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.config.statemachine.simpleOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import java.util.EnumSet;

/**
 * @author liubin01
 */
@Configuration
public class OrderConfig {

    @Configuration
    public static class JpaPersisterConfig {
        @Bean(name = "orderPersist")
        public StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister(
                JpaStateMachineRepository jpaStateMachineRepository) {
            return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
        }
    }

    @Configuration
    @EnableStateMachineFactory(name = "orderStateMachineFactory")
    public static class MachineConfig extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {

        @Autowired
        private StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister;

        @Override
        public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config)
                throws Exception {
            config
                    .withPersistence()
                    .runtimePersister(stateMachineRuntimePersister);
        }

        @Override
        public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
                throws Exception {
            states
                    .withStates()
                    .initial(OrderState.START)
                    .end(OrderState.COMPLETE)
                    .end(OrderState.FAIL)
                    .end(OrderState.TIME_OUT)
                    .states(EnumSet.allOf(OrderState.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
                throws Exception {
            transitions
                    .withExternal()
                    .source(OrderState.START).target(OrderState.ACCOUNT_SUBMIT)
                    .event(OrderEvent.START_EVENT)

                    .and().withExternal()
                    .source(OrderState.ACCOUNT_SUBMIT).target(OrderState.COMPLETE)
                    .event(OrderEvent.ACCOUNT_SUBMIT_SUCCESS_EVENT)

                    .and().withExternal()
                    .source(OrderState.ACCOUNT_SUBMIT).target(OrderState.ORDER_CANCEL)
                    .event(OrderEvent.ACCOUNT_SUBMIT_FAIL_EVENT)

                    .and().withExternal()
                    .source(OrderState.ORDER_CANCEL).target(OrderState.FAIL)
                    .event(OrderEvent.ORDER_CANCEL_FAIL_EVENT)

                    .and().withExternal()
                    .source(OrderState.ORDER_CANCEL).target(OrderState.COMPLETE)
                    .event(OrderEvent.ORDER_CANCEL_SUCCESS_EVENT)
            ;
        }
    }

    @Configuration
    public static class ServiceConfig {

        @Bean(name = "orderMachineService")
        public StateMachineService<OrderState, OrderEvent> stateMachineService(
                StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
                StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister) {
            return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
        }
    }
}
