/**
 * @(#)St1Config.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.config.statemachine.st1;

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
public class St1Config {
    @Configuration
    public static class JpaPersisterConfig {
        @Bean(name = "s1Persist")
        public StateMachineRuntimePersister<St1States, St1Events, String> stateMachineRuntimePersister(
                JpaStateMachineRepository jpaStateMachineRepository) {
            return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
        }
    }

    @Configuration
    @EnableStateMachineFactory(name = "s1f")
    public static class MachineConfig extends StateMachineConfigurerAdapter<St1States, St1Events> {

        @Autowired
        private StateMachineRuntimePersister<St1States, St1Events, String> stateMachineRuntimePersister;

        @Override
        public void configure(StateMachineConfigurationConfigurer<St1States, St1Events> config)
                throws Exception {
            config
                    .withPersistence()
                    .runtimePersister(stateMachineRuntimePersister);
        }

        @Override
        public void configure(StateMachineStateConfigurer<St1States, St1Events> states)
                throws Exception {
            states
                    .withStates()
                    .initial(St1States.ST1_S1)
                    .states(EnumSet.allOf(St1States.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<St1States, St1Events> transitions)
                throws Exception {
            transitions
                    .withExternal()
                    .source(St1States.ST1_S1).target(St1States.ST1_S2)
                    .event(St1Events.ST1_E1)
                    .and()
                    .withExternal()
                    .source(St1States.ST1_S2).target(St1States.ST1_S3)
                    .event(St1Events.ST1_E2)
                    .and()
                    .withExternal()
                    .source(St1States.ST1_S3).target(St1States.ST1_S1)
                    .event(St1Events.ST1_E3);
        }
    }

    @Configuration
    public static class ServiceConfig {

        @Bean(name = "s1MachineService")
        public StateMachineService<St1States, St1Events> stateMachineService(
                StateMachineFactory<St1States, St1Events> stateMachineFactory,
                StateMachineRuntimePersister<St1States, St1Events, String> stateMachineRuntimePersister) {
            return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
        }
    }
}
