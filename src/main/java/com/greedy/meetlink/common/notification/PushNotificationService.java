package com.greedy.meetlink.common.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    public void send(String token, String title, String body) {
        if (token == null || token.isBlank()) return;

        FirebaseApp app = firebaseAppProvider.getIfAvailable();

        if (app == null) {
            log.debug("Firebase not configured, skipping push notification");
            return;
        }

        Message message =
                Message.builder()
                        .setToken(token)
                        .setNotification(
                                Notification.builder().setTitle(title).setBody(body).build())
                        .build();

        try {
            String messageId = FirebaseMessaging.getInstance(app).send(message);
            log.debug("FCM message sent: {}", messageId);
        } catch (FirebaseMessagingException e) {
            if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode())) {
                log.warn(
                        "FCM token unregistered: token prefix={}",
                        token.substring(0, Math.min(10, token.length())));
            } else {
                log.error("FCM send failed: {}", e.getMessage());
            }
        }
    }

    public void send(List<String> tokens, String title, String body) {
        tokens.stream()
                .filter((t) -> t != null && !t.isBlank())
                .forEach((token) -> send(token, title, body));
    }
}
