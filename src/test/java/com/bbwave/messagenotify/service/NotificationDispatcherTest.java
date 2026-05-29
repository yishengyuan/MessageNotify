package com.bbwave.messagenotify.service;

import com.bbwave.messagenotify.config.NotificationProperties;
import com.bbwave.messagenotify.model.NotificationMessage;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDispatcherTest {

    private static class RecordingSender implements NotificationSender {
        private final String channel;
        private final boolean enabled;
        final List<NotificationMessage> received = new ArrayList<>();

        RecordingSender(String channel, boolean enabled) {
            this.channel = channel;
            this.enabled = enabled;
        }

        @Override
        public String channel() {
            return channel;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void send(NotificationMessage message) {
            received.add(message);
        }
    }

    private NotificationProperties propertiesWithDefault(String defaultChannel) {
        NotificationProperties props = new NotificationProperties();
        props.setDefaultChannel(defaultChannel);
        return props;
    }

    @Test
    void dispatchesToSpecifiedChannelOnly() {
        RecordingSender telegram = new RecordingSender("telegram", true);
        RecordingSender teams = new RecordingSender("teams", true);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(telegram, teams), propertiesWithDefault("telegram"), new SimpleMeterRegistry());

        dispatcher.dispatch(new NotificationMessage("hi", "body", "INFO", "teams", null));

        assertThat(telegram.received).isEmpty();
        assertThat(teams.received).hasSize(1);
    }

    @Test
    void allChannelFansOutToEverySender() {
        RecordingSender telegram = new RecordingSender("telegram", true);
        RecordingSender teams = new RecordingSender("teams", true);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(telegram, teams), propertiesWithDefault("telegram"), new SimpleMeterRegistry());

        dispatcher.dispatch(new NotificationMessage("hi", "body", "INFO", "all", null));

        assertThat(telegram.received).hasSize(1);
        assertThat(teams.received).hasSize(1);
    }

    @Test
    void usesDefaultChannelWhenUnspecified() {
        RecordingSender telegram = new RecordingSender("telegram", true);
        RecordingSender teams = new RecordingSender("teams", true);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(telegram, teams), propertiesWithDefault("teams"), new SimpleMeterRegistry());

        dispatcher.dispatch(new NotificationMessage("hi", "body", "INFO", null, null));

        assertThat(telegram.received).isEmpty();
        assertThat(teams.received).hasSize(1);
    }

    @Test
    void disabledSenderIsSkipped() {
        RecordingSender telegram = new RecordingSender("telegram", false);
        NotificationDispatcher dispatcher = new NotificationDispatcher(
                List.of(telegram), propertiesWithDefault("telegram"), new SimpleMeterRegistry());

        dispatcher.dispatch(new NotificationMessage("hi", "body", "INFO", "telegram", null));

        assertThat(telegram.received).isEmpty();
    }
}
