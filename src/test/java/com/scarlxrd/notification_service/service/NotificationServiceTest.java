package com.scarlxrd.notification_service.service;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationSender telegramSender;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        payload = new NotificationPayload();
        payload.setOrderId(UUID.randomUUID());

        lenient().when(idempotencyService.isDuplicate(any(), any(), any()))
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
        }

        @Test
        @DisplayName("Deve mostrar 0 quando amount for null")
        void shouldShowZeroWhenAmountIsNull() {
            // Given
            payload.setStatus("ATUALIZAÇÃO DE PAGAMENTO");
            payload.setAmount(null);

            // When
            notificationService.process(payload);

            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(telegramSender).send(captor.capture());

            assertThat(captor.getValue()).contains("0");
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
        }
    }

    @Nested
    @DisplayName("Idempotência")
    class IdempotencyTests {

        @Test
        @DisplayName("Deve ignorar evento duplicado")
        void shouldIgnoreDuplicateEvent() {
            payload.setStatus("SUCCESS");
            payload.setAmount(new BigDecimal("100"));

            when(idempotencyService.isDuplicate(any(), any(), any()))
                    .thenReturn(true);

            notificationService.process(payload);

            verify(telegramSender, never()).send(any());
        }
    }
}