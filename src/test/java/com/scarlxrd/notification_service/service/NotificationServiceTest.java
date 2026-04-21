package com.scarlxrd.notification_service.service;

import com.scarlxrd.notification_service.dto.NotificationPayload;
import com.scarlxrd.notification_service.impl.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class NotificationServiceTest {

    @Mock
    private NotificationSender telegramSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        payload = new NotificationPayload();
        payload.setOrderId(UUID.randomUUID());
    }


    @Test
    @DisplayName("Deve ignorar payload sem status e sem email")
    void shouldIgnorePayloadWithoutStatusAndEmail() {
        payload.setStatus(null);
        payload.setCustomerEmail(null);

        notificationService.process(payload);

        verify(telegramSender, never()).send(any());
    }


    @Test
    @DisplayName("Deve enviar mensagem de pagamento APROVADO quando status for SUCCESS")
    void shouldSendApprovedMessageWhenStatusIsSuccess() {
        payload.setStatus("SUCCESS");
        payload.setAmount(new BigDecimal("180.00"));

        notificationService.process(payload);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(telegramSender).send(captor.capture());

        String message = captor.getValue();
        assertThat(message).contains("ATUALIZAÇÃO DE PAGAMENTO");
        assertThat(message).contains("APROVADO");
        assertThat(message).contains("180.00");
        assertThat(message).contains(payload.getOrderId().toString());
    }

    @Test
    @DisplayName("Deve enviar mensagem de pagamento FALHOU quando status não for SUCCESS")
    void shouldSendFailedMessageWhenStatusIsNotSuccess() {
        payload.setStatus("FAILED");
        payload.setAmount(new BigDecimal("180.00"));

        notificationService.process(payload);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(telegramSender).send(captor.capture());

        assertThat(captor.getValue()).contains("FALHOU");
    }

    @Test
    @DisplayName("Deve mostrar 0.00 quando amount for null no pagamento")
    void shouldShowZeroWhenAmountIsNullOnPayment() {
        payload.setStatus("ATUALIZAÇÃO DE PAGAMENTO");
        payload.setAmount(null);

        notificationService.process(payload);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(telegramSender).send(captor.capture());

        assertThat(captor.getValue()).contains("0");
    }

    @Test
    @DisplayName("Deve enviar mensagem de novo pedido com email e valor")
    void shouldSendNewOrderMessageWithEmailAndAmount() {
        payload.setCustomerEmail("client@teste.com");
        payload.setAmount(new BigDecimal("360.00"));

        notificationService.process(payload);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(telegramSender).send(captor.capture());

        String message = captor.getValue();
        assertThat(message).contains("NOVO PEDIDO REGISTRADO");
        assertThat(message).contains("client@teste.com");
        assertThat(message).contains("R$ 360.00");
        assertThat(message).contains(payload.getOrderId().toString());
    }

    @Test
    @DisplayName("Deve mostrar Calculando... quando amount for null no novo pedido")
    void shouldShowCalculatingWhenAmountIsNullOnNewOrder() {
        payload.setCustomerEmail("cliente@teste.com");
        payload.setAmount(null);

        notificationService.process(payload);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(telegramSender).send(captor.capture());

        assertThat(captor.getValue()).contains("Calculando...");
    }

    @Test
    @DisplayName("Deve mostrar 'Usuário não identificado' quando email for null no novo pedido")
    void shouldShowUnknownUserWhenEmailIsNull() {
        payload.setCustomerEmail(null);
        payload.setStatus(null);
        payload.setAmount(new BigDecimal("180.00"));

        notificationService.process(payload);


        verify(telegramSender, never()).send(any());
    }
}