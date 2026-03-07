package com.scarlxrd.notification_service.service;


import com.scarlxrd.notification_service.dto.ClientCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendClientCreated(ClientCreatedEvent event) {

        String message = """
        📘 Novo cliente criado
        
        CPF: %s
        Nome: %s
        """.formatted(event.getCpf(), event.getName());

        String url = "https://api.telegram.org/bot" + token + "/sendMessage";

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", message
        );

        restTemplate.postForObject(url, body, String.class);

    }

}