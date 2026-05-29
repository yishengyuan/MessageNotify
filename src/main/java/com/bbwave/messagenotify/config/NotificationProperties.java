package com.bbwave.messagenotify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for the notification channels.
 *
 * Bound from the {@code notify.*} prefix in application.yml / environment variables.
 */
@ConfigurationProperties(prefix = "notify")
public class NotificationProperties {

    /**
     * Default channel used when an incoming message does not specify one.
     * One of: telegram, teams, all.
     */
    private String defaultChannel = "telegram";

    private final Telegram telegram = new Telegram();
    private final Teams teams = new Teams();

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public Teams getTeams() {
        return teams;
    }

    /** Telegram ("小飞机") bot settings. */
    public static class Telegram {
        /** Whether the Telegram channel is enabled. */
        private boolean enabled = false;
        /** Bot token issued by @BotFather. */
        private String botToken;
        /** Default chat id (user, group or channel) used when no service route matches. */
        private String chatId;
        /**
         * Per-service-type routing: {@code serviceType -> chatId}. When an incoming
         * message carries a {@code service} that matches a key here, it is delivered
         * to the mapped group instead of the default {@link #chatId}. Keys are matched
         * case-insensitively.
         */
        private Map<String, String> chatRoutes = new HashMap<>();
        /** Base API url; override when using a proxy / self-hosted bot api. */
        private String apiBaseUrl = "https://api.telegram.org";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public Map<String, String> getChatRoutes() {
            return chatRoutes;
        }

        public void setChatRoutes(Map<String, String> chatRoutes) {
            this.chatRoutes = chatRoutes;
        }

        /** Resolve the chat id for a service key, falling back to the default chat id. */
        public String resolveChatId(String serviceKey) {
            if (serviceKey != null && !serviceKey.isBlank()) {
                for (Map.Entry<String, String> entry : chatRoutes.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(serviceKey)) {
                        return entry.getValue();
                    }
                }
            }
            return chatId;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }

    /** Microsoft Teams incoming webhook settings. */
    public static class Teams {
        /** Whether the Teams channel is enabled. */
        private boolean enabled = false;
        /** Default incoming webhook url, used when no service route matches. */
        private String webhookUrl;
        /**
         * Per-service-type routing: {@code serviceType -> webhookUrl}. When an incoming
         * message carries a {@code service} that matches a key here, it is delivered to
         * the mapped Teams channel instead of the default {@link #webhookUrl}. Keys are
         * matched case-insensitively.
         */
        private Map<String, String> webhookRoutes = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public Map<String, String> getWebhookRoutes() {
            return webhookRoutes;
        }

        public void setWebhookRoutes(Map<String, String> webhookRoutes) {
            this.webhookRoutes = webhookRoutes;
        }

        /** Resolve the webhook url for a service key, falling back to the default webhook. */
        public String resolveWebhookUrl(String serviceKey) {
            if (serviceKey != null && !serviceKey.isBlank()) {
                for (Map.Entry<String, String> entry : webhookRoutes.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(serviceKey)) {
                        return entry.getValue();
                    }
                }
            }
            return webhookUrl;
        }
    }
}
