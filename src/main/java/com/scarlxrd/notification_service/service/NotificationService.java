package com.scarlxrd.notification_service.service;

import com.scarlxrd.notification_service.config.metrics.NotificationMetrics;
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

    private static final String CHANNEL_TELEGRAM = "telegram";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(5);

    private final NotificationSender telegramSender;
    private final IdempotencyService idempotencyService;
    private final NotificationMetrics notificationMetrics;

    public void process(NotificationPayload payload) {
        if (shouldIgnore(payload)) {
            notificationMetrics.ignored("missing_contact_and_status");
            log.info("Ignorando evento intermediário sem dados de contato");
            return;
        }

        String context = resolveContext(payload);
        String key = resolveIdempotencyKey(payload);

        if (idempotencyService.wasProcessed(context, key, IDEMPOTENCY_TTL)) {
            notificationMetrics.duplicated();
            log.warn("Evento duplicado ignorado. context={}, key={}", context, key);
            return;
        }

        String message = buildMessage(payload);

        try {
            telegramSender.send(message);

            idempotencyService.markAsProcessed(context, key);

            notificationMetrics.sent(CHANNEL_TELEGRAM);

            log.info(
                    "Notificação enviada com sucesso. channel={}, context={}, key={}",
                    CHANNEL_TELEGRAM,
                    context,
                    key
            );

        } catch (Exception ex) {
            notificationMetrics.failed(CHANNEL_TELEGRAM, "send_error");

            log.error(
                    "Falha ao enviar notificação. channel={}, context={}, key={}, error={}",
                    CHANNEL_TELEGRAM,
                    context,
                    key,
                    ex.getMessage(),
                    ex
            );

            throw ex;
        }
    }

    private boolean shouldIgnore(NotificationPayload payload) {
        return payload.getCustomerEmail() == null && payload.getStatus() == null;
    }

    private String resolveContext(NotificationPayload payload) {
        if (payload.getStatus() != null) {
            return "payment";
        }

        return "order";
    }

    private String resolveIdempotencyKey(NotificationPayload payload) {
        if (payload.getStatus() != null) {
            return payload.getOrderId() + ":" + payload.getStatus();
        }

        return payload.getOrderId().toString();
    }

    private String buildMessage(NotificationPayload payload) {
        if (payload.getStatus() != null) {
            return buildPaymentMessage(payload);
        }

        return buildOrderMessage(payload);
    }

    private String buildPaymentMessage(NotificationPayload payload) {
        StringBuilder sb = new StringBuilder();

        sb.append("💳 *ATUALIZAÇÃO DE PAGAMENTO*\n\n");
        sb.append("*Pedido:* ").append(payload.getOrderId()).append("\n");
        sb.append("*Status:* ")
                .append(payload.getStatus().equals("SUCCESS") ? "✅ APROVADO" : "❌ FALHOU");

        BigDecimal valor = payload.getAmount() != null
                ? payload.getAmount()
                : BigDecimal.ZERO;

        sb.append("\n*Valor:* R$ ").append(valor);

        return sb.toString();
    }

    private String buildOrderMessage(NotificationPayload payload) {
        StringBuilder sb = new StringBuilder();

        sb.append("📘 *NOVO PEDIDO REGISTRADO*\n\n");
        sb.append("*ID:* ").append(payload.getOrderId()).append("\n");

        String email = payload.getCustomerEmail() != null
                ? payload.getCustomerEmail()
                : "Usuário não identificado";

        sb.append("*E-mail:* ").append(email).append("\n");

        String total = payload.getAmount() != null
                ? "R$ " + payload.getAmount()
                : "Calculando...";

        sb.append("*Total:* ").append(total);

        return sb.toString();
    }
}