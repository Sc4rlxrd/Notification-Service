package com.scarlxrd.notification_service.dto;

import lombok.Data;

@Data
public class NotificationPayload {

    private String eventType;
    private String cpf;
    private String name;
}
