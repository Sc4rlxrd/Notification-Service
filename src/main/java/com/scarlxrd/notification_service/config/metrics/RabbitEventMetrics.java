package com.scarlxrd.notification_service.config.metrics;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitEventMetrics {

    private final MeterRegistry registry;

    public void consumed(String event) {
        Counter.builder("rabbitmq_events_consumed_total")
                .description("Total de eventos consumidos do RabbitMQ")
                .tag("service", "notification-service")
                .tag("event", event)
                .register(registry)
                .increment();
    }

    public void failed(String event) {
        Counter.builder("rabbitmq_events_failed_total")
                .description("Total de falhas ao consumir eventos RabbitMQ")
                .tag("service", "notification-service")
                .tag("event", event)
                .register(registry)
                .increment();
    }

    public void duplicated(String event) {
        Counter.builder("rabbitmq_events_duplicated_total")
                .description("Total de eventos duplicados do RabbitMQ")
                .tag("service", "notification-service")
                .tag("event", event)
                .register(registry)
                .increment();
    }
}