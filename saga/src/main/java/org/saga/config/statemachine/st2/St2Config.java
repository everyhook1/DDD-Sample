/**
 * @(#)config.java, 3月 24, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.config.statemachine.st2;

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
public class St2Config {

    @Configuration
    public static class JpaPersisterConfig {
        @Bean(name = "s2Persist")
        public StateMachineRuntimePersister<St2States, St2Events, String> stateMachineRuntimePersister(
                JpaStateMachineRepository jpaStateMachineRepository) {
            return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
        }
    }

    @Configuration
    @EnableStateMachineFactory(name = "s2f")
    public static class MachineConfig extends StateMachineConfigurerAdapter<St2States, St2Events> {

        @Autowired
        private StateMachineRuntimePersister<St2States, St2Events, String> stateMachineRuntimePersister;

        @Override
        public void configure(StateMachineConfigurationConfigurer<St2States, St2Events> config)
                throws Exception {
            config
                    .withPersistence()
                    .runtimePersister(stateMachineRuntimePersister);
        }

        @Override
        public void configure(StateMachineStateConfigurer<St2States, St2Events> states)
                throws Exception {
            states
                    .withStates()
                    .initial(St2States.ST2_S1)
                    .states(EnumSet.allOf(St2States.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<St2States, St2Events> transitions)
                throws Exception {
            transitions
                    .withExternal()
                    .source(St2States.ST2_S1).target(St2States.ST2_S2)
                    .event(St2Events.ST2_E1)
                    .and()
                    .withExternal()
                    .source(St2States.ST2_S2).target(St2States.ST2_S3)
                    .event(St2Events.ST2_E2)
                    .and()
                    .withExternal()
                    .source(St2States.ST2_S3).target(St2States.ST2_S1)
                    .event(St2Events.ST2_E3);
        }
    }

    @Configuration
    public static class ServiceConfig {

        //tag::snippetE[]
        @Bean(name = "s2MachineService")
        public StateMachineService<St2States, St2Events> stateMachineService(
                StateMachineFactory<St2States, St2Events> stateMachineFactory,
                StateMachineRuntimePersister<St2States, St2Events, String> stateMachineRuntimePersister) {
            return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
        }
        //end::snippetE[]
    }
}
