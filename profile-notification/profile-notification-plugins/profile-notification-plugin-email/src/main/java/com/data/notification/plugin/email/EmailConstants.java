package com.data.notification.plugin.email;

public class EmailConstants {

    private EmailConstants() {
        throw new IllegalStateException(EmailConstants.class.getName());
    }

    public static final String XLS_FILE_PATH = "xls.file.path";

    public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

    public static final String DEFAULT_SMTP_PORT = "25";

    public static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";

    public static final int NUMBER_1000 = 1000;

    public static final String TR = "<tr>";

    public static final String TD = "<td>";

    public static final String TD_END = "</td>";

    public static final String TR_END = "</tr>";

    public static final String TITLE = "title";

    public static final String CONTENT = "content";

    public static final String TH = "<th>";

    public static final String TH_END = "</th>";

    public static final String MARKDOWN_QUOTE = ">";

    public static final String MARKDOWN_ENTER = "\n";

    public static final String HTML_HEADER_PREFIX = "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>" +
            "<html>" +
            "<head>" +
            "<title>datavines</title>" +
            "<meta name='Keywords' content=''>" +
            "<meta name='Description' content=''>" +
            "<style type=\"text/css\">" +
            "</style>" +
            "</head>" +
            "<body style=\"margin:0;padding:0\"><table border=\"1px\" cellpadding=\"5px\" cellspacing=\"-10px\" style=\"font-size: 15px;\"> ";

    public static final String TABLE_HTML_TAIL = "</table></html>";

    public static final String BODY_HTML_TAIL = "</body></html>";

    public static final String UTF_8 = "UTF-8";

    public static final String EXCEL_SUFFIX_XLSX = ".xlsx";

    public static final String SINGLE_SLASH = "/";

    public static final String URL="<a href=\"mailto:";
}
