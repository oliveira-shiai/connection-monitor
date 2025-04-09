package com.exemplo.connectionmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class ConnectionMonitor {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private Connection connection;
    private final int maxRetries = 5;

    @PostConstruct
    public void init() {
        connectToDatabase();
    }

    @Scheduled(fixedRate = 15000)
    public void checkConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                log("Conexão perdida! Tentando reconectar...");
                generateThreadDump();
                attemptReconnect();
            } else {
                log("Conexão OK");
            }
        } catch (SQLException e) {
            log("Erro ao verificar conexão: " + e.getMessage());
            generateThreadDump();
            attemptReconnect();
        }
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            log("Conexão inicial estabelecida com sucesso.");
        } catch (SQLException e) {
            log("Erro na conexão inicial: " + e.getMessage());
            generateThreadDump();
            attemptReconnect();
        }
    }

    private void attemptReconnect() {
        for (int i = 1; i <= maxRetries; i++) {
            try {
                log("Tentativa de reconexão " + i + " de " + maxRetries);
                Thread.sleep(2000);
                connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                log("Reconexão bem-sucedida.");
                return;
            } catch (Exception e) {
                log("Falha na tentativa " + i + ": " + e.getMessage());
            }
        }

        log("Não foi possível restabelecer a conexão após " + maxRetries + " tentativas. Finalizando aplicação.");
        shutdownApplication();
    }

    private void shutdownApplication() {
        try {
            log("Encerrando aplicação - PID: " + ProcessHandle.current().pid());
        } catch (Exception e) {
            log("Erro ao recuperar PID: " + e.getMessage());
        }

        System.exit(1);
    }

    private void generateThreadDump() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        String timestamp = LocalDateTime.now().toString();
        log("=== Thread Dump em " + timestamp + " ===");
        for (ThreadInfo threadInfo : threadInfos) {
            log(threadInfo.toString());
        }
    }

    private void log(String message) {
        System.out.println(LocalDateTime.now() + " - " + message);
    }
}
