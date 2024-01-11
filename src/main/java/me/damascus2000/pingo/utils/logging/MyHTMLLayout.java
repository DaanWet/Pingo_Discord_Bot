package me.damascus2000.pingo.utils.logging;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.jetbrains.annotations.NotNull;
import me.damascus2000.pingo.utils.Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MyHTMLLayout extends HTMLLayout {

    private final String url;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM - HH:mm:ss");
    private boolean link = true;

    public MyHTMLLayout(){
        this.url = Utils.config.getProperty("logging.url");
    }


    @Override
    public String format(@NotNull LoggingEvent event){
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP).append("<tr>").append(LINE_SEP);
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault());
        String TD = "<td>";
        String CTD = "</td>";
        sb.append(TD).append(dtf.format(time)).append(CTD).append(LINE_SEP);

        sb.append(TD);
        if (event.getLevel().equals(Level.DEBUG)){
            sb.append("<font color=\"#339933\">");
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
            sb.append("</font>");
        } else if (event.getLevel().isGreaterOrEqual(Level.WARN)){
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
        if (this.link && link != null)
            sb.append("<a href=\"").append(url).append("log_").append(link).append("\">").append(link).append("</a>");
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
        sb.append("<script>\n" +
                          "function myFunction() {\n" +
                          "  // Declare variables\n" +
                          "  var input, filter, table, tr, td, i, txtValue;\n" +
                          "  input = document.getElementById(\"myInput\");\n" +
                          "  filter = input.value.toUpperCase();\n" +
                          "  table = document.getElementById(\"myTable\");\n" +
                          "  tr = table.getElementsByTagName(\"tr\");\n" +
                          "\n" +
                          "  // Loop through all table rows, and hide those who don't match the search query\n" +
                          "  for (i = 0; i < tr.length; i++) {\n" +
                          "    td = tr[i].getElementsByTagName(\"td\")[1];\n" +
                          "    if (td) {\n" +
                          "      txtValue = td.textContent || td.innerText;\n" +
                          "      if (txtValue.toUpperCase().indexOf(filter) > -1) {\n" +
                          "        tr[i].style.display = \"\";\n" +
                          "      } else {\n" +
                          "        tr[i].style.display = \"none\";\n" +
                          "      }\n" +
                          "    }\n" +
                          "  }\n" +
                          "}\n" +
                          "</script>");
        sb.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">").append(LINE_SEP);
        sb.append("<hr size=\"1\" noshade>").append(LINE_SEP);
        sb.append("Log session start time ").append(new Date()).append("<br>").append(LINE_SEP);
        sb.append("<br>").append(LINE_SEP);
        sb.append("<input type=\"text\" id=\"myInput\" onkeyup=\"myFunction()\" placeholder=\"Search for Level..\">");
        sb.append("<table id=\"myTable\" cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">").append(LINE_SEP);
        sb.append("<tr>").append(LINE_SEP);
        sb.append("<th>Time</th>").append(LINE_SEP);
        sb.append("<th>Level</th>").append(LINE_SEP);
        sb.append("<th>Location</th>").append(LINE_SEP);
        sb.append("<th>Message</th>").append(LINE_SEP);
        if (this.link)
            sb.append("<th>Link</th>").append(LINE_SEP);
        sb.append("</tr>").append(LINE_SEP);
        return sb.toString();
    }

    public void setLink(boolean link){
        this.link = link;
    }
}
