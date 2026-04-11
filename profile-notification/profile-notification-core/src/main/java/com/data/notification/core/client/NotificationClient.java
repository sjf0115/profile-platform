package com.data.notification.core.client;

import com.data.notification.api.entity.SlaConfigMessage;
import com.data.notification.api.entity.SlaNotificationMessage;
import com.data.notification.api.entity.SlaNotificationResult;
import com.data.notification.api.entity.SlaSenderMessage;

import java.util.Map;
import java.util.Set;

public interface NotificationClient {

    SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config);

}
