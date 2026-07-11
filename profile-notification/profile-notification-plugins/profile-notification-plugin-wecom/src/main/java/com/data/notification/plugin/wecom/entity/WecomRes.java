package com.data.notification.plugin.wecom.entity;

import com.data.profile.common.utils.JSONUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
public class WecomRes implements Serializable {

    private static final long serialVersionUID = -1L;

    private String errcode;

    private String errmsg;

    /**
     * check req success
     */
    public boolean success() {
        return StringUtils.equalsIgnoreCase(errcode, "0");
    }

    public static WecomRes parseFromJson(String json) {
        return JSONUtils.parseObject(json, WecomRes.class);
    }
}
