package com.data.notification.plugin.wecom;

import com.data.notification.api.entity.NotificationResultRecord;
import com.data.notification.plugin.wecom.entity.ReceiverConfig;
import com.data.notification.plugin.wecom.entity.WecomRes;
import com.data.notification.plugin.wecom.utils.ContentUtil;
import com.data.profile.common.utils.JSONUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.data.notification.plugin.wecom.WecomConstants.*;

@Slf4j
@EqualsAndHashCode
@Data
public class WecomSender {

    public NotificationResultRecord sendMsg(Set<ReceiverConfig> receiverSet, String subject, String message) {
        NotificationResultRecord result = new NotificationResultRecord();
        if (CollectionUtils.isEmpty(receiverSet)) {
            return result;
        }
        receiverSet.removeIf(receiver -> StringUtils.isEmpty(receiver.getWebhook()));
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        Set<String> failToReceivers = new HashSet<>();
        for (ReceiverConfig receiver : receiverSet) {
            try {
                String markdownMessage = getMarkdownMessage(subject, message);
                Map<String, Object> paramMap = ContentUtil.createParamMap(
                        MSG_TYPE, MARKDOWN,
                        MARKDOWN, ContentUtil.createParamMap(CONTENT, markdownMessage));
                String paramJson = JSONUtils.toJsonString(paramMap);

                HttpPost httpPost = new HttpPost(receiver.getWebhook());
                StringEntity entity = new StringEntity(paramJson, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
                httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                     CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    HttpEntity responseEntity = response.getEntity();
                    String res = EntityUtils.toString(responseEntity, "UTF-8");
                    EntityUtils.consume(responseEntity);

                    WecomRes wecomBotRes = WecomRes.parseFromJson(res);
                    if (!wecomBotRes.success()) {
                        failToReceivers.add(receiver.getWebhook());
                        log.info("wecom sender error, webhook: {}, resp: {}", receiver.getWebhook(), res);
                    }
                }
            } catch (Exception e) {
                failToReceivers.add(receiver.getWebhook());
                log.error("wecom send error", e);
            }
        }

        if (!CollectionUtils.isEmpty(failToReceivers)) {
            String recordMessage = String.format("send to %s fail", String.join(",", failToReceivers));
            result.setStatus(false);
            result.setMessage(recordMessage);
        } else {
            result.setStatus(true);
        }
        return result;
    }

    private String getMarkdownMessage(String subject, String content) {
        StringBuilder contents = new StringBuilder(100);
        if (StringUtils.isNotEmpty(subject)) {
            contents.append(FIRST_TITLE_START).append(subject).append(END);
        }
        if (StringUtils.isNotEmpty(content)) {
            // 按行分割，每行加引用前缀
            String[] lines = content.split("\n");
            for (String line : lines) {
                contents.append(QUOTE_START).append(line).append(END);
            }
        }
        return contents.toString();
    }
}
