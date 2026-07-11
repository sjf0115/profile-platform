package com.data.notification.core.client.impl;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.notification.core.NotificationManager;
import com.data.notification.core.client.NotificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class NotificationClientImpl implements NotificationClient {

    @Autowired
    private NotificationManager notificationManager;

    @Override
    public NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config) {
        NotificationResult result = notificationManager.notify(notificationMessage, config);
        return result;
    }

}
