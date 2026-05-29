package com.bbwave.messagenotify;

import com.bbwave.messagenotify.config.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties.class)
public class MessageNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageNotifyApplication.class, args);
    }
}
