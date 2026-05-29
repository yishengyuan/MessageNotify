package com.bbwave.messagenotify.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamsWebhookRoutingTest {

    private NotificationProperties.Teams teamsWithRoutes() {
        NotificationProperties.Teams teams = new NotificationProperties.Teams();
        teams.setWebhookUrl("https://hook/default");
        teams.getWebhookRoutes().put("order-service", "https://hook/order");
        teams.getWebhookRoutes().put("payment-service", "https://hook/payment");
        return teams;
    }

    @Test
    void routesKnownServiceToItsChannel() {
        NotificationProperties.Teams teams = teamsWithRoutes();
        assertThat(teams.resolveWebhookUrl("order-service")).isEqualTo("https://hook/order");
        assertThat(teams.resolveWebhookUrl("payment-service")).isEqualTo("https://hook/payment");
    }

    @Test
    void matchingIsCaseInsensitive() {
        NotificationProperties.Teams teams = teamsWithRoutes();
        assertThat(teams.resolveWebhookUrl("Order-Service")).isEqualTo("https://hook/order");
    }

    @Test
    void fallsBackToDefaultWebhookForUnknownOrMissingService() {
        NotificationProperties.Teams teams = teamsWithRoutes();
        assertThat(teams.resolveWebhookUrl("unknown-service")).isEqualTo("https://hook/default");
        assertThat(teams.resolveWebhookUrl(null)).isEqualTo("https://hook/default");
        assertThat(teams.resolveWebhookUrl("")).isEqualTo("https://hook/default");
    }
}
