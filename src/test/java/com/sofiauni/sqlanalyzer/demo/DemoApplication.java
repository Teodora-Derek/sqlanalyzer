package com.sofiauni.sqlanalyzer.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test/demo host application. It depends on the analyzer purely via auto-configuration — there is
 * no manual wiring — which demonstrates the zero-code integration goal (criterion C5).
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
