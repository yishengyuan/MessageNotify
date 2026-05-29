package com.bbwave.messagenotify.service;

import com.bbwave.messagenotify.config.NotificationProperties;
import com.bbwave.messagenotify.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Sends notifications to Telegram ("小飞机") via the Bot API {@code sendMessage} method.
 */
@Component
public class TelegramNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationSender.class);

    private final NotificationProperties.Telegram config;
    private final RestClient restClient;

    public TelegramNotificationSender(NotificationProperties properties, RestClient restClient) {
        this.config = properties.getTelegram();
        this.restClient = restClient;
    }

    @Override
    public String channel() {
        return "telegram";
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled()
                && config.getBotToken() != null && !config.getBotToken().isBlank()
                && config.getChatId() != null && !config.getChatId().isBlank();
    }

    @Override
    public void send(NotificationMessage message) {
        String url = "%s/bot%s/sendMessage".formatted(stripTrailingSlash(config.getApiBaseUrl()), config.getBotToken());
        Map<String, Object> body = Map.of(
                "chat_id", config.getChatId(),
                "text", render(message),
                "parse_mode", "HTML"
        );

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("Telegram notification delivered to chat {}", config.getChatId());
    }

    private String render(NotificationMessage message) {
        return "<b>[%s] %s</b>%n%s".formatted(
                escape(message.safeLevel()),
                escape(message.safeTitle()),
                escape(message.safeContent()));
    }

    /** Minimal HTML escaping for the Telegram HTML parse mode. */
    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
