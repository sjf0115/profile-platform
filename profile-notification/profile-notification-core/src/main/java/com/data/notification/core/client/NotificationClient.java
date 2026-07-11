package com.data.notification.core.client;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;

import java.util.Map;
import java.util.Set;

public interface NotificationClient {

    NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config);

}
