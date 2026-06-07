package com.scarlxrd.notification_service.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private final MeterRegistry registry;

    public void sent(String channel) {
        Counter.builder("notifications_sent_total")
                .description("Total de notificações enviadas")
                .tag("service", "notification-service")
                .tag("channel", channel)
                .register(registry)
                .increment();
    }

    public void failed(String channel, String reason) {
        Counter.builder("notifications_failed_total")
                .description("Total de falhas ao enviar notificações")
                .tag("service", "notification-service")
                .tag("channel", channel)
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void duplicated() {
        Counter.builder("notifications_duplicated_total")
                .description("Total de notificações duplicadas ignoradas")
                .tag("service", "notification-service")
                .register(registry)
                .increment();
    }

    public void ignored(String reason) {
        Counter.builder("notifications_ignored_total")
                .description("Total de notificações ignoradas")
                .tag("service", "notification-service")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }
}