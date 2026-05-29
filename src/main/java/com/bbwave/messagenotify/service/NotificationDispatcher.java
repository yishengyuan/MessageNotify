package com.bbwave.messagenotify.service;

import com.bbwave.messagenotify.config.NotificationProperties;
import com.bbwave.messagenotify.model.NotificationMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes a {@link NotificationMessage} to the appropriate {@link NotificationSender}(s)
 * and records delivery metrics.
 */
@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);
    private static final String ALL = "all";

    private final List<NotificationSender> senders;
    private final NotificationProperties properties;
    private final MeterRegistry meterRegistry;

    public NotificationDispatcher(List<NotificationSender> senders,
                                  NotificationProperties properties,
                                  MeterRegistry meterRegistry) {
        this.senders = senders;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public void dispatch(NotificationMessage message) {
        String target = resolveChannel(message.channel());
        log.info("Dispatching notification '{}' to channel '{}'", message.safeTitle(), target);

        boolean handled = false;
        for (NotificationSender sender : senders) {
            if (!ALL.equals(target) && !sender.channel().equalsIgnoreCase(target)) {
                continue;
            }
            handled = true;
            deliver(sender, message);
        }

        if (!handled) {
            log.warn("No sender matched channel '{}'; message dropped: {}", target, message.safeTitle());
            counter("notify.messages.dropped", "channel", target).increment();
        }
    }

    private void deliver(NotificationSender sender, NotificationMessage message) {
        if (!sender.isEnabled()) {
            log.warn("Sender '{}' is not enabled; skipping", sender.channel());
            counter("notify.messages.skipped", "channel", sender.channel()).increment();
            return;
        }
        try {
            sender.send(message);
            counter("notify.messages.sent", "channel", sender.channel(), "result", "success").increment();
        } catch (Exception ex) {
            counter("notify.messages.sent", "channel", sender.channel(), "result", "failure").increment();
            log.error("Failed to deliver notification via '{}': {}", sender.channel(), ex.getMessage(), ex);
        }
    }

    private String resolveChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return properties.getDefaultChannel().toLowerCase();
        }
        return channel.trim().toLowerCase();
    }

    private Counter counter(String name, String... tags) {
        return Counter.builder(name).tags(tags).register(meterRegistry);
    }
}
