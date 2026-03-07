package com.scarlxrd.notification_service.service;


import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.impl.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSender telegramSender;

    public void process(NotificationPayload payload) {

        telegramSender.send(payload);

    }
}