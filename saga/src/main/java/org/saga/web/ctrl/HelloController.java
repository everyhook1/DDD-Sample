/**
 * @(#)ctrl.java, 3月 23, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.web.ctrl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.saga.config.statemachine.st1.St1Events;
import org.saga.config.statemachine.st1.St1States;
import org.saga.config.statemachine.st2.St2Events;
import org.saga.config.statemachine.st2.St2States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

/**
 * @author liubin01
 */
@OpenAPIDefinition
@RestController
public class HelloController {

    @Autowired
    private StateMachineService<St1States, St1Events> s1MachineService;

    @Autowired
    private StateMachinePersist<St1States, St1Events, String> s1Persist;

    @Autowired
    private StateMachineService<St2States, St2Events> s2MachineService;

    @Autowired
    private StateMachinePersist<St2States, St2Events, String> s2Persist;

    private StateMachineService<?, ?> getMachine(int id) {
        if (id == 1)
            return s1MachineService;
        return s2MachineService;
    }

    private StateMachinePersist<?, ?, String> getPersist(int id) {
        if (id == 1)
            return s1Persist;
        else
            return s2Persist;
    }

    @GetMapping(value = "/{id:\\d+}/{machineId}")
    public Object getMachine(@PathVariable int id, @PathVariable String machineId) throws Exception {

        Object obj = getPersist(id).read(machineId);
        return obj;
    }

    @PutMapping(value = "/{id:\\d+}/{machineId}")
    public Object sendEvent(@PathVariable int id, @PathVariable String machineId, @RequestParam String event) {
        if (id == 1)
            s1MachineService.acquireStateMachine(machineId).sendEvent(Mono.just(MessageBuilder.withPayload(St1Events.valueOf(event)).build())).blockLast();
        else
            s2MachineService.acquireStateMachine(machineId).sendEvent(Mono.just(MessageBuilder.withPayload(St2Events.valueOf(event)).build())).blockLast();
        return "发送成功";
    }

    @PostConstruct
    public void justTest(){
        s1MachineService.acquireStateMachine("s1m1").sendEvent(Mono.just(MessageBuilder.withPayload(St1Events.ST1_E1).build())).blockLast();
        s2MachineService.acquireStateMachine("s2m1").sendEvent(Mono.just(MessageBuilder.withPayload(St2Events.ST2_E1).build())).blockLast();
    }

    @PostMapping(value = "/{id:\\d+}/{machineId}")
    public Object createMachine(@PathVariable int id, @PathVariable String machineId) {
        getMachine(id).acquireStateMachine(machineId, false).startReactively();
        return "存在";
    }
}
