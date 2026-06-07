package com.scarlxrd.notification_service.config.rabbitmq;

import com.scarlxrd.notification_service.config.metrics.RabbitEventMetrics;
import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final RabbitEventMetrics rabbitMetrics;

    @RabbitListener(
            queues = "notification.order.queue",
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receive(NotificationPayload payload) {

        rabbitMetrics.consumed("notification_order");

        log.info("Evento recebido para notificação: {}", payload);

        notificationService.process(payload);
    }

}