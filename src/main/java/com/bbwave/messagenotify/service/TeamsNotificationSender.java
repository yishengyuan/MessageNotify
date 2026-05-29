package com.bbwave.messagenotify.service;

import com.bbwave.messagenotify.config.NotificationProperties;
import com.bbwave.messagenotify.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends notifications to a Microsoft Teams channel via an incoming webhook.
 *
 * The payload uses the legacy MessageCard format which is broadly supported by
 * Teams incoming webhooks / Workflows.
 */
@Component
public class TeamsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(TeamsNotificationSender.class);

    private final NotificationProperties.Teams config;
    private final RestClient restClient;

    public TeamsNotificationSender(NotificationProperties properties, RestClient restClient) {
        this.config = properties.getTeams();
        this.restClient = restClient;
    }

    @Override
    public String channel() {
        return "teams";
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled() && hasAnyWebhook();
    }

    private boolean hasAnyWebhook() {
        boolean hasDefault = config.getWebhookUrl() != null && !config.getWebhookUrl().isBlank();
        return hasDefault || !config.getWebhookRoutes().isEmpty();
    }

    @Override
    public void send(NotificationMessage message) {
        String webhookUrl = config.resolveWebhookUrl(message.safeService());
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalStateException(
                    "No Teams webhook resolved for service '%s' and no default webhook-url configured"
                            .formatted(message.safeService()));
        }

        Map<String, Object> card = Map.of(
                "@type", "MessageCard",
                "@context", "https://schema.org/extensions",
                "themeColor", themeColor(message.safeLevel()),
                "summary", message.safeTitle(),
                "title", "[%s] %s".formatted(message.safeLevel(), message.safeTitle()),
                "sections", List.of(Map.of("text", message.safeContent()))
        );

        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(card)
                .retrieve()
                .toBodilessEntity();

        log.info("Teams notification for service '{}' delivered via webhook", message.safeService());
    }

    private String themeColor(String level) {
        return switch (level) {
            case "ERROR", "CRITICAL", "FATAL" -> "D32F2F";
            case "WARN", "WARNING" -> "F9A825";
            default -> "2E7D32";
        };
    }
}
