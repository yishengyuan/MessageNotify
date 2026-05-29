package com.bbwave.messagenotify.web;

import com.bbwave.messagenotify.model.NotificationMessage;
import com.bbwave.messagenotify.service.NotificationDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Convenience endpoint (served on the application port 8080) to dispatch a
 * notification directly, mirroring what the Kafka consumer does. Useful for
 * smoke tests and manual triggering.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationDispatcher dispatcher;

    public NotificationController(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> send(@RequestBody NotificationMessage message) {
        dispatcher.dispatch(message);
        return ResponseEntity.accepted().body(Map.of("status", "dispatched"));
    }
}
