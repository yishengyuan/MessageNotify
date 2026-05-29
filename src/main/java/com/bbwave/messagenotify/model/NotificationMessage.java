package com.bbwave.messagenotify.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The notification payload consumed from Kafka.
 *
 * @param title   short headline of the notification
 * @param content the message body
 * @param level   severity, e.g. INFO / WARN / ERROR (used for formatting only)
 * @param channel target channel: {@code telegram}, {@code teams} or {@code all}.
 *                When {@code null} or blank the configured default channel is used.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationMessage(
        String title,
        String content,
        String level,
        String channel
) {
    public String safeLevel() {
        return (level == null || level.isBlank()) ? "INFO" : level.trim().toUpperCase();
    }

    public String safeTitle() {
        return (title == null || title.isBlank()) ? "Notification" : title.trim();
    }

    public String safeContent() {
        return content == null ? "" : content;
    }
}
