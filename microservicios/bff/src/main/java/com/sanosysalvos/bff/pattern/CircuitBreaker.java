package com.sanosysalvos.bff.pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Component
public class CircuitBreaker {

    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private static class CircuitState {
        State state = State.CLOSED;
        int failureCount = 0;
        long lastFailureTime = 0;
    }

    private final Map<String, CircuitState> circuits = new ConcurrentHashMap<>();
    
    private final int failureThreshold = 3;
    private final long resetTimeout = 10000; // 10 segundos

    public <T> T execute(String serviceName, Supplier<T> action, Supplier<T> fallback) {
        CircuitState circuit = circuits.computeIfAbsent(serviceName, k -> new CircuitState());

        synchronized (circuit) {
            if (circuit.state == State.OPEN) {
                if (System.currentTimeMillis() - circuit.lastFailureTime > resetTimeout) {
                    circuit.state = State.HALF_OPEN;
                    log.info("Circuit for {} is HALF_OPEN", serviceName);
                } else {
                    log.warn("Circuit for {} is OPEN. Executing fallback.", serviceName);
                    return fallback.get();
                }
            }
        }

        try {
            T result = action.get();
            synchronized (circuit) {
                if (circuit.state == State.HALF_OPEN) {
                    circuit.state = State.CLOSED;
                    circuit.failureCount = 0;
                    log.info("Circuit for {} is CLOSED", serviceName);
                }
            }
            return result;
        } catch (Exception e) {
            synchronized (circuit) {
                circuit.failureCount++;
                circuit.lastFailureTime = System.currentTimeMillis();
                if (circuit.failureCount >= failureThreshold) {
                    circuit.state = State.OPEN;
                    log.error("Circuit for {} Tripped OPEN due to: {}", serviceName, e.getMessage());
                }
            }
            return fallback.get();
        }
    }
}
