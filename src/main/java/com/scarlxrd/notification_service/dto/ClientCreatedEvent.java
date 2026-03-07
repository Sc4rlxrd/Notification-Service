package com.scarlxrd.notification_service.dto;

import lombok.Data;

@Data
public class ClientCreatedEvent {

    private String cpf;
    private String name;
}
