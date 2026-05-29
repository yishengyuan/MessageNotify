# message-notify

A Spring Boot service that consumes notification messages from **Kafka** and
forwards them to **Telegram (小飞机)** and/or **Microsoft Teams**.

* Application (HTTP API) port: **8080**
* Metrics / management port (Prometheus): **8088**
* Java: **25**
* Spring Boot: **4.0.6** (latest GA)

## Architecture

```
Kafka topic ──▶ NotificationConsumer ──▶ NotificationDispatcher ──┬──▶ TelegramNotificationSender ──▶ Telegram Bot API
                                                                  └──▶ TeamsNotificationSender    ──▶ Teams incoming webhook
```

Each incoming Kafka record is a JSON `NotificationMessage`. The dispatcher routes
it to one or all channels and records Micrometer metrics, which are scraped from
the dedicated management port.

## Message format

The Kafka value (and the REST body) is JSON:

```json
{
  "title": "Service alert",
  "content": "CPU usage above 90% on node-01",
  "level": "ERROR",
  "channel": "telegram",
  "service": "order-service"
}
```

| Field     | Required | Description                                                                 |
|-----------|----------|-----------------------------------------------------------------------------|
| `title`   | no       | Headline (defaults to `Notification`).                                       |
| `content` | no       | Message body.                                                               |
| `level`   | no       | `INFO` / `WARN` / `ERROR` … used for formatting/colour (defaults to `INFO`). |
| `channel` | no       | `telegram`, `teams`, or `all`. Falls back to `notify.default-channel`.       |
| `service` | no       | Originating service type. Used to route messages to a specific Telegram group / Teams channel (see below). |

## Routing by service type

Both channels can deliver to **different destinations per service type** by mapping
the message's `service` field to a target. Matching is case-insensitive; anything
unmatched falls back to the channel's default destination.

Telegram — `notify.telegram.chat-routes` (`serviceType -> chatId`):

```yaml
notify:
  telegram:
    enabled: true
    bot-token: 123:abc
    chat-id: "-1000000000000"          # default group
    chat-routes:
      order-service: "-1001111111111"   # order-service -> group A
      payment-service: "-1002222222222" # payment-service -> group B
```

Teams — `notify.teams.webhook-routes` (`serviceType -> webhookUrl`):

```yaml
notify:
  teams:
    enabled: true
    webhook-url: "https://outlook.office.com/webhook/default..."  # default channel
    webhook-routes:
      order-service: "https://outlook.office.com/webhook/order..."     # -> channel A
      payment-service: "https://outlook.office.com/webhook/payment..." # -> channel B
```

> Note: the `service` value is set by each **producing** service. To also keep
> per-service ordering in Kafka, producers should use `service` as the message key
> (so a service's messages land on the same partition). See `docs/data-flow.drawio`
> for the end-to-end data flow and partition mapping.

## Configuration

All settings can be supplied via environment variables (see `application.yml`):

| Env var                 | Default                   | Description                              |
|-------------------------|---------------------------|------------------------------------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092`        | Kafka brokers.                           |
| `KAFKA_GROUP_ID`        | `message-notify`          | Consumer group id.                       |
| `NOTIFY_KAFKA_TOPIC`    | `notifications`           | Topic to consume.                        |
| `NOTIFY_DEFAULT_CHANNEL`| `telegram`                | Default channel when none is specified.  |
| `TELEGRAM_ENABLED`      | `false`                   | Enable the Telegram channel.             |
| `TELEGRAM_BOT_TOKEN`    | —                         | Bot token from @BotFather.               |
| `TELEGRAM_CHAT_ID`      | —                         | Target chat / group / channel id.        |
| `TELEGRAM_API_BASE_URL` | `https://api.telegram.org`| Override for a proxy / self-hosted API.  |
| `TEAMS_ENABLED`         | `false`                   | Enable the Teams channel.                |
| `TEAMS_WEBHOOK_URL`     | —                         | Microsoft Teams incoming webhook URL.    |

## Build & run

Requires JDK 25.

```bash
# build + test
mvn clean verify

# run
TELEGRAM_ENABLED=true \
TELEGRAM_BOT_TOKEN=123:abc \
TELEGRAM_CHAT_ID=-1001234567890 \
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
java -jar target/message-notify-1.0.0.jar
```

## Endpoints

Application (port 8080):

```bash
# manually dispatch a notification (same path the Kafka consumer takes)
curl -X POST http://localhost:8080/api/notifications \
  -H 'Content-Type: application/json' \
  -d '{"title":"hello","content":"world","channel":"telegram"}'
```

Management / metrics (port 8088):

* `GET /actuator/health`
* `GET /actuator/prometheus`
* `GET /actuator/metrics`

## Metrics

Custom counters exposed under `/actuator/prometheus`:

| Metric                       | Tags                       | Meaning                              |
|------------------------------|----------------------------|--------------------------------------|
| `notify_messages_sent_total` | `channel`, `result`        | Deliveries attempted (success/failure). |
| `notify_messages_skipped_total` | `channel`               | Channel matched but was disabled.    |
| `notify_messages_dropped_total` | `channel`               | No sender matched the channel.       |

## Producing a test message to Kafka

```bash
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic notifications
> {"title":"Deploy done","content":"v1.2.3 is live","level":"INFO","channel":"all"}
```
