package com.scarlxrd.notification_service.config.swagger;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.gateway-url:http://localhost:8080}") String gatewayUrl) {
        return new OpenAPI()
                .servers(buildServers(gatewayUrl))
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(buildComponents())
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório do projeto")
                        .url("https://github.com/Sc4rlxrd/Notification-Service"));
    }

    private List<Server> buildServers(String gatewayUrl) {
        return List.of(
                new Server()
                        .url(gatewayUrl)
                        .description("Gateway")
        );
    }


    private Info buildInfo() {
        return new Info()
                .title("Notification-Service API")
                .description("""
                        Serviço responsável pelo envio de notificações do BookCommerce via Telegram Bot.
                        
                        **Este serviço não expõe endpoints HTTP diretos.**
                        A comunicação é feita exclusivamente via RabbitMQ:
                        
                        **Consome:**
                        - `notification.order.queue` → eventos de pedidos e pagamentos
                        
                        **Notificações enviadas:**
                        - `order.#` → novo pedido registrado 📘
                        - `payment.#` → atualização de pagamento 💳
                        
                        **Idempotência:**
                        - Eventos duplicados são ignorados via `InMemoryIdempotencyService`
                        - TTL de 5 minutos por evento
                        
                        **Destino:**
                        - Telegram Bot `@book_events_notifier_bot`
                        """)
                .version("v1")
                .contact(new Contact()
                        .name("Scarlxrd")
                        .url("https://github.com/Sc4rlxrd")
                        .email("contato@exemplo.com"));
    }

    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }
}