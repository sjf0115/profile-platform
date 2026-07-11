package com.data.notification.api.spi;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.spi.SPI;

import java.util.Map;
import java.util.Set;

@SPI
public interface NotificationHandlerPlugin {
    NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config);

    String getConfigSenderJson();

    String getConfigJson();
}
