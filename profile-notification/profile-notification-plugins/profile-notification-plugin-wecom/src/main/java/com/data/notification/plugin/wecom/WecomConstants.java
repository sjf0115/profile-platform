package com.data.notification.plugin.wecom;

public class WecomConstants {

    private WecomConstants() {
        throw new IllegalStateException(WecomConstants.class.getName());
    }

    public static final String MSG_TYPE = "msgtype";
    public static final String MARKDOWN = "markdown";
    public static final String CONTENT = "content";
    public static final String FIRST_TITLE_START = "# ";
    public static final String QUOTE_START = "> ";
    public static final String END = "\n";
}
