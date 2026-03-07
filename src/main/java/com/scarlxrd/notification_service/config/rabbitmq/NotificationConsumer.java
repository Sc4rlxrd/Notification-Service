package com.scarlxrd.notification_service.config.rabbitmq;

import com.scarlxrd.notification_service.dto.ClientCreatedEvent;
import com.scarlxrd.notification_service.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TelegramService telegramService;

    @RabbitListener(queues = "client.book.queue")
    public void receive(ClientCreatedEvent event) {

        telegramService.sendClientCreated(event);

    }

}