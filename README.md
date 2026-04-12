<div align="center">

# 📩 Notification Service

Microserviço responsável por consumir eventos do RabbitMQ e enviar notificações via Telegram.

Parte do ecossistema da **Book API**, utilizando uma arquitetura **Event-Driven** baseada em mensageria.

</div>

---

## 📚 Sobre o Projeto

O **Notification Service** escuta eventos publicados pela API principal e envia notificações automaticamente para um bot do Telegram.

Quando um pedido é criado ou atualizado na API principal, um evento é publicado no RabbitMQ com a routing key apropriada.

Esse serviço consome o evento e envia uma mensagem para o Telegram.

---

## 🏗 Arquitetura

```
Book API
   │
   │ publish event
   ▼
RabbitMQ (exchange: book.events)
   │
   │ routing key: order.created.notify / payment.updated.notify
   ▼
notification.order.queue
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

## ⚙️ Tecnologias

- Java 21
- Spring Boot 4.0.3
- RabbitMQ
- Spring AMQP
- Maven
- Lombok
- Telegram Bot API

---

## 📁 Estrutura do Projeto

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

## 🐇 RabbitMQ Topology

A API principal cria a seguinte infraestrutura no RabbitMQ.

### Exchanges

```
book.events
book.events.dlx
```

### Filas

```
order.book.queue
order.book.queue.retry
order.book.queue.dlq
notification.order.queue
```

### Routing Keys

```
order.created
payment.updated
order.retry
order.dlq
order.created.notify
payment.updated.notify
```

---

## 📩 Evento Consumido

Exchange:

```
book.events
```

Routing Key:

```
order.created.notify
payment.updated.notify
```

Fila:

```
notification.order.queue
```

---

## 📦 Exemplo de Evento

Mensagem enviada pela API principal para novo pedido:

```json
{
  "orderId": "uuid-do-pedido",
  "amount": 100.00,
  "customerEmail": "cliente@email.com",
  "customerName": "Nome do Cliente"
}
```

Para atualização de pagamento:

```json
{
  "orderId": "uuid-do-pedido",
  "amount": 100.00,
  "status": "SUCCESS"
}
```

---

## 📲 Exemplo de Notificação

Mensagem enviada pelo bot do Telegram para novo pedido:

```
📘 *NOVO PEDIDO REGISTRADO*

*ID:* uuid-do-pedido
*E-mail:* cliente@email.com
*Total:* R$ 100.00
```

Para atualização de pagamento aprovado:

```
💳 *ATUALIZAÇÃO DE PAGAMENTO*

*Pedido:* uuid-do-pedido
*Status:* ✅ APROVADO
*Valor:* R$ 100.00
```

---

## ⚙️ Configuração

Exemplo do `application.yaml`:

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

## ▶ Executando o Projeto

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

```bash
./mvnw spring-boot:run
```

ou

```bash
mvn spring-boot:run
```

---

## 🧪 Fluxo de Funcionamento

1️⃣ Pedido criado ou pagamento atualizado na API principal  
2️⃣ Evento publicado no RabbitMQ  
3️⃣ Notification Service consome o evento  
4️⃣ Telegram recebe a mensagem  


---

## 👨‍💻 Autor

Desenvolvido por **Guilherme Dos Santos**
