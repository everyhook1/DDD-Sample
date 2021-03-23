/**
 * @(#)ctrl.java, 3月 23, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.web.ctrl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.saga.config.StateMachineConfig;
import org.saga.dao.PersonRepository;
import org.saga.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liubin01
 */
@OpenAPIDefinition
@RestController
public class HelloController {

    @Autowired
    private PersonRepository personRepository;

    private final StateMachineLogListener listener = new StateMachineLogListener();

    @Autowired
    private StateMachineService<StateMachineConfig.States, StateMachineConfig.Events> stateMachineService;

    @Autowired
    private StateMachinePersist<StateMachineConfig.States, StateMachineConfig.Events, String> stateMachinePersist;


    @GetMapping
    public Collection<Person> get() {
        return personRepository.findAll();
    }

    @PostMapping
    public Person save(@RequestBody Person person) {
        return personRepository.save(person);
    }


    @GetMapping(value = "/machineId")
    public Object getMachine(@RequestParam String machineId) throws Exception {
        return stateMachinePersist.read(machineId);
    }

    @PutMapping(value = "/machineId")
    public Object sendEvent(@RequestParam String machineId, @RequestParam StateMachineConfig.Events event) throws Exception {
        stateMachineService.acquireStateMachine(machineId).sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).blockLast();
        return "发送成功";
    }

    @PostMapping(value = "/machineId")
    public Object createMachine(@RequestParam String machineId) throws Exception {
        StateMachine<StateMachineConfig.States, StateMachineConfig.Events> stateMachine = stateMachineService.acquireStateMachine(machineId, false);
        stateMachine.startReactively();
        return "存在";
    }

    public class StateMachineLogListener extends StateMachineListenerAdapter<StateMachineConfig.States, StateMachineConfig.Events> {

        private final LinkedList<String> messages = new LinkedList<String>();

        public List<String> getMessages() {
            return messages;
        }

        public void resetMessages() {
            messages.clear();
        }

        @Override
        public void stateContext(StateContext<StateMachineConfig.States, StateMachineConfig.Events> stateContext) {
            if (stateContext.getStage() == StateContext.Stage.STATE_ENTRY) {
                messages.addFirst("Enter " + stateContext.getTarget().getId());
            } else if (stateContext.getStage() == StateContext.Stage.STATE_EXIT) {
                messages.addFirst("Exit " + stateContext.getSource().getId());
            } else if (stateContext.getStage() == StateContext.Stage.STATEMACHINE_START) {
                messages.addLast("Machine started");
            } else if (stateContext.getStage() == StateContext.Stage.STATEMACHINE_STOP) {
                messages.addFirst("Machine stopped");
            }
        }
    }
}
