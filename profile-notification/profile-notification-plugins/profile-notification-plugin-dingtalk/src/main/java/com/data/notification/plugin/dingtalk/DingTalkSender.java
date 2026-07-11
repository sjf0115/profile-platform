package com.data.notification.plugin.dingtalk;

import com.data.notification.api.entity.NotificationResultRecord;
import com.data.notification.plugin.dingtalk.entity.ReceiverConfig;
import com.data.profile.common.utils.JSONUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.data.notification.plugin.dingtalk.DingTalkConstants.MSG_TYPE;

@Slf4j
@EqualsAndHashCode
@Data
public class DingTalkSender {

    private String msgType;

    public NotificationResultRecord sendCardMsg(Set<ReceiverConfig> receiverSet, String subject, String message) {
        NotificationResultRecord result = new NotificationResultRecord();
        if (CollectionUtils.isEmpty(receiverSet)) {
            return result;
        }
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        Set<ReceiverConfig> failToReceivers = new HashSet<>();
        for (ReceiverConfig receiverConfig : receiverSet) {
            try {
                String msg = generateMsgJson(subject, message, receiverConfig);
                String url = constructUrl(receiverConfig.getWebhook(), receiverConfig.getSecret());
                HttpPost httpPost = constructHttpPost(url, msg);
                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    CloseableHttpResponse response = httpClient.execute(httpPost);
                    String resp;
                    try {
                        HttpEntity entity = response.getEntity();
                        resp = EntityUtils.toString(entity, "UTF-8");
                        EntityUtils.consume(entity);
                    } finally {
                        response.close();
                    }
                    log.info("DingTalk send msg: {}, resp: {}", msg, resp);
                }
            } catch (Exception e) {
                failToReceivers.add(receiverConfig);
                log.error("dingtalk send error", e);
            }
        }

        if (!CollectionUtils.isEmpty(failToReceivers)) {
            String recordMessage = String.format("send to %s fail",
                    String.join(",", failToReceivers.stream()
                            .map(ReceiverConfig::getAtMobiles)
                            .filter(Objects::nonNull)
                            .toArray(String[]::new)));
            result.setStatus(false);
            result.setMessage(recordMessage);
        } else {
            result.setStatus(true);
        }

        return result;
    }

    private String constructUrl(String webHook, String secret) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        if (org.apache.commons.lang3.StringUtils.isBlank(secret)) {
            return webHook;
        }
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");

        return String.format(webHook + "&sign=%s&timestamp=%d", sign, timestamp);
    }

    private String generateMsgJson(String title, String content, ReceiverConfig receiverConfig) {
        final String atMobiles = receiverConfig.getAtMobiles();
        final String atDingtalkIds = receiverConfig.getAtDingtalkIds();
        final Boolean isAtAll = receiverConfig.getIsAtAll();

        if (org.apache.commons.lang3.StringUtils.isBlank(msgType)) {
            msgType = DingTalkConstants.DING_TALK_MSG_TYPE_TEXT;
        }
        Map<String, Object> items = new HashMap<>();
        items.put(MSG_TYPE, msgType);
        Map<String, Object> text = new HashMap<>();
        items.put(msgType, text);

        if (DingTalkConstants.DING_TALK_MSG_TYPE_MARKDOWN.equals(msgType)) {
            StringBuilder builder = new StringBuilder(content);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(receiverConfig.getKeyword())) {
                builder.append(" ");
                builder.append(receiverConfig.getKeyword());
            }
            builder.append("\n\n");
            if (org.apache.commons.lang3.StringUtils.isNotBlank(atMobiles)) {
                Arrays.stream(atMobiles.split(",")).forEach(value -> {
                    builder.append("@");
                    builder.append(value);
                    builder.append(" ");
                });
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(atDingtalkIds)) {
                Arrays.stream(atDingtalkIds.split(",")).forEach(value -> {
                    builder.append("@");
                    builder.append(value);
                    builder.append(" ");
                });
            }
            byte[] byt = StringUtils.getBytesUtf8(builder.toString());
            String txt = StringUtils.newStringUtf8(byt);
            text.put("title", title);
            text.put("text", txt);
        } else {
            StringBuilder builder = new StringBuilder(title);
            builder.append("\n");
            builder.append(content);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(receiverConfig.getKeyword())) {
                builder.append(" ");
                builder.append(receiverConfig.getKeyword());
            }
            byte[] byt = StringUtils.getBytesUtf8(builder.toString());
            String txt = StringUtils.newStringUtf8(byt);
            text.put("content", txt);
        }

        Map<String, Object> at = new HashMap<>();
        String[] atMobileArray = org.apache.commons.lang3.StringUtils.isNotBlank(atMobiles)
                ? atMobiles.split(",") : new String[0];
        String[] atUserArray = org.apache.commons.lang3.StringUtils.isNotBlank(atDingtalkIds)
                ? atDingtalkIds.split(",") : new String[0];
        at.put("atMobiles", atMobileArray);
        at.put("atDingtalkIds", atUserArray);
        at.put("isAtAll", isAtAll);

        items.put("at", at);
        return JSONUtils.toJsonString(items);
    }

    private static HttpPost constructHttpPost(String url, String msg) {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(msg, StandardCharsets.UTF_8);
        post.setEntity(entity);
        post.addHeader("Content-Type", "application/json; charset=utf-8");
        return post;
    }
}
