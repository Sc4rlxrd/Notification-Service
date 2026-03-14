<div align="center">

# 📩 Notification Service

Microserviço responsável por consumir eventos do RabbitMQ e enviar notificações via Telegram.

Parte do ecossistema da **Book API**, utilizando uma arquitetura **Event-Driven** baseada em mensageria.

</div>

---

# 📚 Sobre o Projeto

O **Notification Service** escuta eventos publicados pela API principal e envia notificações automaticamente para um bot do Telegram.

Quando um cliente é criado na API principal, um evento é publicado no RabbitMQ com a routing key:

```
client.created.notify
```

Esse serviço consome o evento e envia uma mensagem para o Telegram.

---

# 🏗 Arquitetura

```
Book API
   │
   │ publish event
   ▼
RabbitMQ (exchange: book.events)
   │
   │ routing key: client.created.notify
   ▼
notification.client.created.queue
   │
   ▼
Notification Service
   │
   ▼
Telegram Bot API
   │
   ▼
Mensagem enviada no Telegram
```

---

# ⚙️ Tecnologias

- Java 21
- Spring Boot
- RabbitMQ
- Spring AMQP
- Maven
- Lombok
- Telegram Bot API

---

# 📁 Estrutura do Projeto

```
src/main/java/com/scarlxrd/notification_service

├── config
│   └── rabbitmq
│       ├── RabbitConfig.java
│       └── NotificationConsumer.java
│
├── dto
│   └── NotificationPayload.java
│
├── impl
│   └── NotificationSender.java
│
├── sender
│   └── TelegramSender.java
│
├── service
│   └── NotificationService.java
│
└── NotificationServiceApplication.java
```

---

# 🐇 RabbitMQ Topology

A API principal cria a seguinte infraestrutura no RabbitMQ.

## Exchanges

```
book.events
book.events.dlx
```

## Filas

```
client.book.queue
client.book.queue.retry
client.book.queue.dlq
notification.client.created.queue
```

## Routing Keys

```
client.created
client.retry
client.created.dlq
client.created.notify
```

---

# 📩 Evento Consumido

Exchange:

```
book.events
```

Routing Key:

```
client.created.notify
```

Fila:

```
notification.client.created.queue
```

---

# 📦 Exemplo de Evento

Mensagem enviada pela API principal:

```json
{
  "eventType": "CLIENT_CREATED",
  "cpf": "477.946.290-80",
  "name": "Nico"
}
```

---

# 📲 Exemplo de Notificação

Mensagem enviada pelo bot do Telegram:

```
📚 Novo cliente cadastrado

Nome: Nico
CPF: 477.946.290-80
Evento: CLIENT_CREATED
```

---

# ⚙️ Configuração

Exemplo do `application.yaml`

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: book_user
    password: book_password

telegram:
  bot:
    token: SEU_BOT_TOKEN
    chat-id: SEU_CHAT_ID
```

---

# ▶ Executando o Projeto

### 1️⃣ Subir RabbitMQ

```bash
docker run -d \
--hostname rabbitmq \
--name rabbitmq \
-p 5672:5672 \
-p 15672:15672 \
rabbitmq:3-management
```

Acessar painel:

```
http://localhost:15672
```

---

### 2️⃣ Executar aplicação

```
./mvnw spring-boot:run
```

ou

```
mvn spring-boot:run
```

---

# 🧪 Fluxo de Funcionamento

1️⃣ Cliente criado na API principal  
2️⃣ Evento publicado no RabbitMQ  
3️⃣ Notification Service consome o evento  
4️⃣ Telegram recebe a mensagem  

---

# 🚀 Melhorias Futuras

- Retry para falha no envio de notificação
- Suporte a múltiplos canais de notificação

Exemplos:

- Email
- Slack

---

# 👨‍💻 Autor

Desenvolvido por **Guilherme Dos Santos**
