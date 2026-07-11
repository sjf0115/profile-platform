package com.data.notification.plugin.dingtalk.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Objects;

@Data
@ToString
public class ReceiverConfig {

    private String webhook;

    private String secret;

    private String keyword;

    private String atMobiles;

    private String atDingtalkIds;

    private Boolean isAtAll;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReceiverConfig that = (ReceiverConfig) o;
        return Objects.equals(atMobiles, that.atMobiles) && isAtAll == that.isAtAll;
    }

    @Override
    public int hashCode() {
        return Objects.hash(atMobiles, atDingtalkIds);
    }
}
