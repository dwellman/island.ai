package com.demo.island;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        // Force CLI/non-web mode even if WebFlux is on the classpath (Spring AI pulls it in).
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
