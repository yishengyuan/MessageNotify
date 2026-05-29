package com.bbwave.messagenotify.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramChatRoutingTest {

    private NotificationProperties.Telegram telegramWithRoutes() {
        NotificationProperties.Telegram telegram = new NotificationProperties.Telegram();
        telegram.setChatId("-100default");
        telegram.getChatRoutes().put("order-service", "-100order");
        telegram.getChatRoutes().put("payment-service", "-100payment");
        return telegram;
    }

    @Test
    void routesKnownServiceToItsGroup() {
        NotificationProperties.Telegram telegram = telegramWithRoutes();
        assertThat(telegram.resolveChatId("order-service")).isEqualTo("-100order");
        assertThat(telegram.resolveChatId("payment-service")).isEqualTo("-100payment");
    }

    @Test
    void matchingIsCaseInsensitive() {
        NotificationProperties.Telegram telegram = telegramWithRoutes();
        assertThat(telegram.resolveChatId("Order-Service")).isEqualTo("-100order");
    }

    @Test
    void fallsBackToDefaultChatForUnknownOrMissingService() {
        NotificationProperties.Telegram telegram = telegramWithRoutes();
        assertThat(telegram.resolveChatId("unknown-service")).isEqualTo("-100default");
        assertThat(telegram.resolveChatId(null)).isEqualTo("-100default");
        assertThat(telegram.resolveChatId("")).isEqualTo("-100default");
    }
}
