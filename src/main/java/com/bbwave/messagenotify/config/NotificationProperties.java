package com.bbwave.messagenotify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
        /** Target chat id (user, group or channel). */
        private String chatId;
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
        /** Incoming webhook url of the Teams channel. */
        private String webhookUrl;

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
    }
}
