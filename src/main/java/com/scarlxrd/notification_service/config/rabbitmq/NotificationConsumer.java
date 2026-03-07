package com.scarlxrd.notification_service.config.rabbitmq;

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

    @RabbitListener(queues = "notification.client.created.queue")
    public void receive(NotificationPayload payload) {

        log.info("Evento recebido para notificação: {}", payload);

        notificationService.process(payload);
    }

}