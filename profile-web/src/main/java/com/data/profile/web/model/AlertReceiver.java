package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警接收人条目（用于 alert_receivers JSON 序列化）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertReceiver {
    /** 接收人类型：owner/user/group/role */
    private String type;
    /** 接收人标识（owner 类型无值，user 类型为 userId） */
    private String value;
}
