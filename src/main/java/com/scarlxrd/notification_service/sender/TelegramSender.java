package com.scarlxrd.notification_service.sender;

import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.impl.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Slf4j
@Component
public class TelegramSender implements NotificationSender {
    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String message) {
        String url = "https://api.telegram.org/bot" + token + "/sendMessage";

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", message,
                "parse_mode", "Markdown"
        );

        try {
            restTemplate.postForObject(url, body, String.class);
            log.info("Notificação enviada para Telegram com sucesso");
        } catch (Exception e) {
            log.error("Erro ao enviar para o Telegram: {}", e.getMessage());
        }
    }
}
