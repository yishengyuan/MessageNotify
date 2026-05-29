package com.bbwave.messagenotify.service;

import com.bbwave.messagenotify.model.NotificationMessage;

/**
 * A target capable of delivering a {@link NotificationMessage}.
 */
public interface NotificationSender {

    /** Logical channel name this sender handles, e.g. {@code telegram} or {@code teams}. */
    String channel();

    /** Whether this sender is enabled (configured) and able to deliver messages. */
    boolean isEnabled();

    /** Deliver the message. Implementations should throw on a non-recoverable failure. */
    void send(NotificationMessage message);
}
