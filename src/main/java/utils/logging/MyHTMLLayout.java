package utils.logging;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyHTMLLayout extends HTMLLayout {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM - HH:mm:ss");
    private final String TD = "<td>";
    private final String CTD = "</td>";
    private static String url = "https://pingo.wettinck.be/logs/";

    @Override
    public String format(@NotNull LoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP).append("<tr>").append(LINE_SEP);
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault());
        sb.append(TD).append(dtf.format(time)).append(CTD).append(LINE_SEP);

        sb.append(TD);
        if (event.getLevel().equals(Level.DEBUG)) {
            sb.append("<font color=\"#339933\">");
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
            sb.append("</font>");
        } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            sb.append("<font color=\"#993300\"><strong>");
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
            sb.append("</strong></font>");
        } else {
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
        }
        sb.append(CTD).append(LINE_SEP);
        LocationInfo locInfo = event.getLocationInformation();
        sb.append(TD);
        sb.append(Transform.escapeTags(locInfo.getFileName()));
        sb.append(':');
        sb.append(locInfo.getLineNumber());
        sb.append(CTD).append(LINE_SEP);
        String message = Transform.escapeTags(event.getRenderedMessage());
        sb.append(TD).append(message).append(CTD);
        sb.append(TD);
        String link = event.getProperty("link");
        if (link != null)
            sb.append("<a href=\"").append(url).append(link).append("\">").append(link).append("</a>");
        sb.append(CTD);
        return sb.toString();
    }

    @Override
    public String getHeader(){
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        sb.append(LINE_SEP).append("<html>").append(LINE_SEP);
        sb.append("<head>").append(LINE_SEP);
        sb.append("<title>").append(getTitle()).append("</title>").append(LINE_SEP);
        sb.append("<style type=\"text/css\">").append(LINE_SEP);
        sb.append("<!--").append(LINE_SEP);
        sb.append("body, table {font-family: arial,sans-serif; font-size: x-small;}").append(LINE_SEP);
        sb.append("th {background: #336699; color: #FFFFFF; text-align: left;}").append(LINE_SEP);
        sb.append("-->").append(LINE_SEP);
        sb.append("</style>").append(LINE_SEP);
        sb.append("</head>").append(LINE_SEP);
        sb.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">").append(LINE_SEP);
        sb.append("<hr size=\"1\" noshade>").append(LINE_SEP);
        sb.append("Log session start time ").append(new Date()).append("<br>").append(LINE_SEP);
        sb.append("<br>").append(LINE_SEP);
        sb.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">").append(LINE_SEP);
        sb.append("<tr>").append(LINE_SEP);
        sb.append("<th>Time</th>").append(LINE_SEP);
        sb.append("<th>Level</th>").append(LINE_SEP);
        sb.append("<th>Location</th>").append(LINE_SEP);
        sb.append("<th>Message</th>").append(LINE_SEP);
        sb.append("<th>Link</th>").append(LINE_SEP);
        sb.append("</tr>").append(LINE_SEP);
        return sb.toString();
    }
}
