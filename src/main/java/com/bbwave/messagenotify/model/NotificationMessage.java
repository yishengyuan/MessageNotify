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
 * @param service the originating service type/name. Used to route the message to
 *                a specific Telegram group; falls back to the default chat when unmatched.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationMessage(
        String title,
        String content,
        String level,
        String channel,
        String service
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

    /** Lower-cased, trimmed service key used for routing; empty string when absent. */
    public String safeService() {
        return (service == null || service.isBlank()) ? "" : service.trim().toLowerCase();
    }
}
