package com.data.notification.api.spi;

import com.data.notification.api.entity.SlaConfigMessage;
import com.data.notification.api.entity.SlaNotificationMessage;
import com.data.notification.api.entity.SlaNotificationResult;
import com.data.notification.api.entity.SlaSenderMessage;
import com.data.spi.SPI;

import java.util.Map;
import java.util.Set;

@SPI
public interface SlasHandlerPlugin {
    SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config);

    String getConfigSenderJson();

    String getConfigJson();
}
