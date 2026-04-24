package com.scarlxrd.notification_service.service;


import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.impl.IdempotencyService;
import com.scarlxrd.notification_service.impl.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationSender telegramSender;
    private final IdempotencyService idempotencyService;

    public void process(NotificationPayload payload) {
        if (payload.getCustomerEmail() == null && payload.getStatus() == null) {
            log.info("Ignorando evento intermediário sem dados de contato");
            return;
        }

        if (payload.getStatus() == null && (payload.getAmount() == null || payload.getAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            log.info("Aguardando validação completa do pedido {} para notificar.", payload.getOrderId());
            return;
        }

        String context;
        String key;

        if (payload.getStatus() != null) {
            context = "payment";
            key = payload.getOrderId() + ":" + payload.getStatus();
        } else {
            context = "order";
            key = payload.getOrderId().toString();
        }

        boolean duplicate = idempotencyService.isDuplicate(
                context,
                key,
                Duration.ofMinutes(5)
        );

        if (duplicate) {
            log.warn("Evento duplicado ignorado: {}", key);
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (payload.getStatus() != null) {
            sb.append("💳 *ATUALIZAÇÃO DE PAGAMENTO*\n\n");
            sb.append("*Pedido:* ").append(payload.getOrderId()).append("\n");
            sb.append("*Status:* ").append(payload.getStatus().equals("SUCCESS") ? "✅ APROVADO" : "❌ FALHOU");

            // Se o valor vier nulo no pagamento, não mostra a linha ou mostra 0.00
            BigDecimal valor = payload.getAmount() != null ? payload.getAmount() : BigDecimal.ZERO;
            sb.append("\n*Valor:* R$ ").append(valor);

        } else {
            sb.append("📘 *NOVO PEDIDO REGISTRADO*\n\n");
            sb.append("*ID:* ").append(payload.getOrderId()).append("\n");


            String email = payload.getCustomerEmail() != null ? payload.getCustomerEmail() : "Usuário não identificado";
            sb.append("*E-mail:* ").append(email).append("\n");


            String total = payload.getAmount() != null ? "R$ " + payload.getAmount() : "Calculando...";
            sb.append("*Total:* ").append(total);
        }

        telegramSender.send(sb.toString());
    }
}