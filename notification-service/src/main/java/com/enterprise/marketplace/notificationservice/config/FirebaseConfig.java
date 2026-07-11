package com.enterprise.marketplace.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "marketplace.notification", name = "fcm-enabled", havingValue = "true")
public class FirebaseConfig {

    @Bean
    FirebaseMessaging firebaseMessaging(NotificationProperties properties)
            throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            if (!StringUtils.hasText(properties.getFcmServiceAccountPath())) {
                throw new IllegalStateException("FCM service account path is required when FCM is enabled");
            }
            try (FileInputStream serviceAccount = new FileInputStream(properties.getFcmServiceAccountPath())) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
        return FirebaseMessaging.getInstance();
    }
}
