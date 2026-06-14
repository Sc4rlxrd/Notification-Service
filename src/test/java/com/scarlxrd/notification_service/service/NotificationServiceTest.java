package com.scarlxrd.notification_service.service;

import com.scarlxrd.notification_service.config.metrics.NotificationMetrics;
import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.impl.IdempotencyService;
import com.scarlxrd.notification_service.impl.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationSender telegramSender;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private NotificationMetrics notificationMetrics;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        payload = new NotificationPayload();
        payload.setOrderId(UUID.randomUUID());

        lenient().when(idempotencyService.wasProcessed(any(), any(), any()))
                .thenReturn(false);
    }

    @Nested
    @DisplayName("Cenários ignorados")
    class IgnoreTests {

        @Test
        @DisplayName("Deve ignorar payload sem status e sem email")
        void shouldIgnorePayloadWithoutStatusAndEmail() {
            // Given
            payload.setStatus(null);
            payload.setCustomerEmail(null);

            // When
            notificationService.process(payload);

            // Then
            verify(telegramSender, never()).send(any());
            verify(idempotencyService, never()).wasProcessed(any(), any(), any());
            verify(idempotencyService, never()).markAsProcessed(any(), any());

            verify(notificationMetrics).ignored("missing_contact_and_status");
            verify(notificationMetrics, never()).sent(anyString());
            verify(notificationMetrics, never()).duplicated();
            verify(notificationMetrics, never()).failed(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve ignorar quando email for null no novo pedido")
        void shouldIgnoreWhenEmailIsNull() {
            // Given
            payload.setCustomerEmail(null);
            payload.setStatus(null);
            payload.setAmount(new BigDecimal("180.00"));

            // When
            notificationService.process(payload);

            // Then
            verify(telegramSender, never()).send(any());
            verify(idempotencyService, never()).wasProcessed(any(), any(), any());
            verify(idempotencyService, never()).markAsProcessed(any(), any());

            verify(notificationMetrics).ignored("missing_contact_and_status");
        }
    }

    @Nested
    @DisplayName("Notificação de pagamento")
    class PaymentTests {

        @Test
        @DisplayName("Deve enviar mensagem de pagamento APROVADO")
        void shouldSendApprovedMessage() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("180.00"));

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            String message = captor.getValue();
            assertThat(message).contains("ATUALIZAÇÃO DE PAGAMENTO");
            assertThat(message).contains("APROVADO");
            assertThat(message).contains("180.00");
            assertThat(message).contains(payload.getOrderId().toString());

            verify(idempotencyService).wasProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":SUCCESS"),
                    eq(Duration.ofMinutes(5))
            );

            verify(idempotencyService).markAsProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":SUCCESS")
            );

            verify(notificationMetrics).sent("telegram");
            verify(notificationMetrics, never()).failed(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve enviar mensagem de pagamento FALHOU")
        void shouldSendFailedMessage() {
            // Given
            payload.setStatus("FAILED");
            payload.setAmount(new BigDecimal("180.00"));

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            assertThat(captor.getValue()).contains("FALHOU");

            verify(idempotencyService).markAsProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":FAILED")
            );

            verify(notificationMetrics).sent("telegram");
        }

        @Test
        @DisplayName("Deve mostrar 0 quando amount for null")
        void shouldShowZeroWhenAmountIsNull() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(null);

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            assertThat(captor.getValue()).contains("R$ 0");

            verify(idempotencyService).markAsProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":SUCCESS")
            );

            verify(notificationMetrics).sent("telegram");
        }
    }

    @Nested
    @DisplayName("Notificação de novo pedido")
    class OrderTests {

        @Test
        @DisplayName("Deve enviar mensagem de novo pedido com email e valor")
        void shouldSendNewOrderMessage() {
            // Given
            payload.setCustomerEmail("client@teste.com");
            payload.setAmount(new BigDecimal("360.00"));

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            String message = captor.getValue();
            assertThat(message).contains("NOVO PEDIDO REGISTRADO");
            assertThat(message).contains("client@teste.com");
            assertThat(message).contains("R$ 360.00");
            assertThat(message).contains(payload.getOrderId().toString());

            verify(idempotencyService).wasProcessed(
                    eq("order"),
                    eq(payload.getOrderId().toString()),
                    eq(Duration.ofMinutes(5))
            );

            verify(idempotencyService).markAsProcessed(
                    eq("order"),
                    eq(payload.getOrderId().toString())
            );

            verify(notificationMetrics).sent("telegram");
        }

        @Test
        @DisplayName("Deve mostrar Calculando quando amount for null")
        void shouldShowCalculatingWhenAmountIsNull() {
            // Given
            payload.setCustomerEmail("cliente@teste.com");
            payload.setAmount(null);

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            assertThat(captor.getValue()).contains("Calculando...");

            verify(idempotencyService).markAsProcessed(
                    eq("order"),
                    eq(payload.getOrderId().toString())
            );

            verify(notificationMetrics).sent("telegram");
        }
    }

    @Nested
    @DisplayName("Idempotência")
    class IdempotencyTests {

        @Test
        @DisplayName("Deve ignorar evento duplicado")
        void shouldIgnoreDuplicateEvent() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("100"));

            when(idempotencyService.wasProcessed(any(), any(), any()))
                    .thenReturn(true);

            // When
            notificationService.process(payload);

            // Then
            verify(telegramSender, never()).send(any());
            verify(idempotencyService, never()).markAsProcessed(any(), any());

            verify(notificationMetrics).duplicated();
            verify(notificationMetrics, never()).sent(anyString());
            verify(notificationMetrics, never()).failed(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve usar chave correta para pagamento")
        void shouldUseCorrectKeyForPayment() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("180.00"));

            // When
            notificationService.process(payload);

            // Then
            verify(idempotencyService).wasProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":SUCCESS"),
                    any()
            );
        }

        @Test
        @DisplayName("Deve usar chave correta para pedido")
        void shouldUseCorrectKeyForOrder() {
            // Given
            payload.setCustomerEmail("cliente@teste.com");

            // When
            notificationService.process(payload);

            // Then
            verify(idempotencyService).wasProcessed(
                    eq("order"),
                    eq(payload.getOrderId().toString()),
                    any()
            );
        }

        @Test
        @DisplayName("Deve chamar idempotencyService com TTL de 5 minutos")
        void shouldCallIdempotencyWithCorrectTtl() {
            // Given
            payload.setStatus("SUCCESS");

            // When
            notificationService.process(payload);

            // Then
            verify(idempotencyService).wasProcessed(
                    any(),
                    any(),
                    eq(Duration.ofMinutes(5))
            );
        }

        @Test
        @DisplayName("Deve marcar como processado após enviar notificação")
        void shouldMarkAsProcessedAfterSendingNotification() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("180.00"));

            // When
            notificationService.process(payload);

            // Then
            verify(telegramSender).send(anyString());

            verify(idempotencyService).markAsProcessed(
                    eq("payment"),
                    eq(payload.getOrderId() + ":SUCCESS")
            );
        }
    }

    @Nested
    @DisplayName("Falha no envio da notificação")
    class SendFailureTests {

        @Test
        @DisplayName("Deve lançar exceção quando telegramSender falhar")
        void shouldThrowExceptionWhenTelegramSenderFails() {
            // Given
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("180.00"));

            RuntimeException exception = new RuntimeException("Telegram unavailable");

            doThrow(exception)
                    .when(telegramSender)
                    .send(anyString());

            // When / Then
            assertThatThrownBy(() -> notificationService.process(payload))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Telegram unavailable");

            verify(notificationMetrics).failed("telegram", "send_error");
            verify(notificationMetrics, never()).sent(anyString());

            verify(idempotencyService, never()).markAsProcessed(any(), any());
        }
    }
}