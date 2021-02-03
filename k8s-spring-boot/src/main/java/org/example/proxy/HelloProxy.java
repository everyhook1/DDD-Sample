/**
 * @(#)HelloProxy.java, 2月 03, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.proxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author liubin01
 */
@RestController
public class HelloProxy {

    private final WebClient webClient = WebClient.create();

    @GetMapping
    public Mono<String> index() {
        return webClient.get().uri("http://k8s-workshop-name-service")
                .retrieve()
                .toEntity(String.class)
                .map(entity -> {
                    String host = Objects.requireNonNull(entity.getHeaders().get("k8s-host")).get(0);
                    return "Hello " + entity.getBody() + " from " + host;
                });
    }
}
