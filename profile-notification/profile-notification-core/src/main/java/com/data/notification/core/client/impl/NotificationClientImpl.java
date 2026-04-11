package com.data.notification.core.client.impl;

import com.data.notification.api.entity.SlaConfigMessage;
import com.data.notification.api.entity.SlaNotificationMessage;
import com.data.notification.api.entity.SlaNotificationResult;
import com.data.notification.api.entity.SlaSenderMessage;
import com.data.notification.core.NotificationManager;
import com.data.notification.core.client.NotificationClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

//@Component
public class NotificationClientImpl implements NotificationClient {

    @Autowired
    private NotificationManager notificationManager;

    @Override
    public SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config) {
        SlaNotificationResult result = notificationManager.notify(slaNotificationMessage, config);
        return result;
    }

}
