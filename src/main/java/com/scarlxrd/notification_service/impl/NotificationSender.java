package com.scarlxrd.notification_service.impl;


import com.scarlxrd.notification_service.dto.NotificationPayload;

public interface NotificationSender {

    void send(NotificationPayload payload);

}