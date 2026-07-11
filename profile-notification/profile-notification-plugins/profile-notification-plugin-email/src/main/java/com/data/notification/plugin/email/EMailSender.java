package com.data.notification.plugin.email;

import com.data.notification.api.entity.NotificationResultRecord;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.profile.common.utils.JSONUtils;
import com.sun.mail.smtp.SMTPProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Slf4j
@EqualsAndHashCode
@Data
public class EMailSender {

    private String mailProtocol = "smtp";

    private String mailSmtpHost;

    private String mailSmtpPort;

    private String mailSenderEmail;

    private String enableSmtpAuth;

    private String mailUser;

    private String mailPasswd;

    private String mailUseStartTLS;

    private String mailUseSSL;

    private String sslTrust;

    private final String mustNotNull = "must not be null";

    public EMailSender(NotificationSenderConfig senderMessage) {
        String configString = senderMessage.getConfig();
        Map<String, String> config = JSONUtils.toMap(configString);

        mailSmtpHost = config.get("serverHost");
        requireNonNull(mailSmtpHost, "serverHost" + mustNotNull);

        mailSmtpPort = config.get("serverPort");
        requireNonNull(mailSmtpPort, "serverPort" + mustNotNull);

        mailSenderEmail = config.get("sender");
        requireNonNull(mailSenderEmail, "sender" + mustNotNull);

        enableSmtpAuth = config.get("enableSmtpAuth");

        mailUser = config.get("user");
        requireNonNull(mailUser, "user" + mustNotNull);

        mailPasswd = config.get("passwd");
        requireNonNull(mailPasswd, "passwd" + mustNotNull);

        mailUseStartTLS = config.get("starttlsEnable");
        requireNonNull(mailUseStartTLS, "starttlsEnable" + mustNotNull);

        mailUseSSL = config.get("sslEnable");
        requireNonNull(mailUseSSL, "sslEnable" + mustNotNull);

        sslTrust = config.get("smtpSslTrust");
        requireNonNull(sslTrust, "smtpSslTrust" + mustNotNull);
    }

    public NotificationResultRecord sendMails(Set<String> receiverSet, Set<String> copyReceiverSet, String subject, String message) {
        NotificationResultRecord result = new NotificationResultRecord();
        if (CollectionUtils.isEmpty(receiverSet)) {
            return result;
        }
        receiverSet.removeIf(StringUtils::isEmpty);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        HtmlEmail email = new HtmlEmail();
        try {
            Session session = getSession();
            email.setMailSession(session);
            email.setFrom(mailSenderEmail);
            email.setCharset("UTF-8");
            if (CollectionUtils.isNotEmpty(receiverSet)) {
                for (String receiver : receiverSet) {
                    email.addTo(receiver);
                }
            }
            if (CollectionUtils.isNotEmpty(copyReceiverSet)) {
                for (String receiverCc : copyReceiverSet) {
                    email.addCc(receiverCc);
                }
            }
            if (message != null && message.trim().startsWith("<!")) {
                email.setMsg(message);
            } else {
                email.setMsg(wrapHtml(message));
            }
            email.setSubject(subject);
            email.setDebug(false);
            email.send();
            result.setStatus(true);
            return result;
        } catch (Exception e) {
            log.error("email send error", e);
        }
        return result;
    }

    private Session getSession() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", mailSmtpHost);
        props.setProperty("mail.smtp.port", mailSmtpPort);
        props.setProperty("mail.smtp.auth", enableSmtpAuth);
        props.setProperty("mail.transport.protocol", mailProtocol);
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.smtp.starttls.enable", mailUseStartTLS);
        props.setProperty("mail.smtp.ssl.enable", mailUseSSL);
        if (Boolean.parseBoolean(sslTrust)) {
            props.setProperty("mail.smtp.ssl.trust", mailSmtpHost);
        }

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUser, mailPasswd);
            }
        };

        Session session = Session.getInstance(props, auth);
        session.addProvider(new SMTPProvider());
        return session;
    }

    private String wrapHtml(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        return EmailConstants.HTML_HEADER_PREFIX
                + EmailConstants.TR + EmailConstants.TD + content + EmailConstants.TD_END + EmailConstants.TR_END
                + EmailConstants.TABLE_HTML_TAIL
                + EmailConstants.BODY_HTML_TAIL;
    }
}
