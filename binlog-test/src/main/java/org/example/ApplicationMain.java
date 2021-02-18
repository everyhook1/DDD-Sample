/**
 * @(#)ApplicationMain.java, 1月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author liubin01
 */
@SpringBootApplication
public class ApplicationMain {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationMain.class);
        application.run(args);
    }
}
