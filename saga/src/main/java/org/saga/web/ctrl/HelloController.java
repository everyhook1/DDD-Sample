/**
 * @(#)ctrl.java, 3月 23, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.saga.web.ctrl;

import org.saga.config.statemachine.st1.St1Events;
import org.saga.config.statemachine.st1.St1States;
import org.saga.config.statemachine.st2.St2Events;
import org.saga.config.statemachine.st2.St2States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author liubin01
 */
@RestController
public class HelloController {

    @Autowired
    private StateMachineService<St1States, St1Events> s1MachineService;

    @Autowired
    private StateMachineService<St2States, St2Events> s2MachineService;


    @GetMapping(value = "/{id:\\d+}/{machineId}")
    public Object getMachine(@PathVariable int id, @PathVariable String machineId) {
        StateMachine stateMachine;
        if (id == 1) {
            stateMachine = s1MachineService.acquireStateMachine(machineId, false);
        } else {
            stateMachine = s2MachineService.acquireStateMachine(machineId, false);
        }
        stateMachine.startReactively();
        return true;
    }

    @PutMapping(value = "/{id:\\d+}/{machineId}")
    public Object sendEvent(@PathVariable int id, @PathVariable String machineId, @RequestParam String event) {
        if (id == 1)
            s1MachineService.acquireStateMachine(machineId).sendEvent(Mono.just(MessageBuilder.withPayload(St1Events.valueOf(event)).build())).blockLast();
        else
            s2MachineService.acquireStateMachine(machineId).sendEvent(Mono.just(MessageBuilder.withPayload(St2Events.valueOf(event)).build())).blockLast();
        return "发送成功";
    }

}
