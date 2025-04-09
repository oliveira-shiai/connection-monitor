package com.exemplo.connectionmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConnectionMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConnectionMonitorApplication.class, args);
    }
}
