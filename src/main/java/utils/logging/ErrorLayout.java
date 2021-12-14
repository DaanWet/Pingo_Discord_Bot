package utils.logging;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class ErrorLayout extends Layout {
    @Override
    public String format(LoggingEvent loggingEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        sb.append(LINE_SEP).append("<html>").append(LINE_SEP);
        sb.append("<head>");
        sb.append("<title>").append("Log").append("</title>").append(LINE_SEP);
        sb.append("<style type=\"text/css\">").append(LINE_SEP);
        sb.append("<!--").append(LINE_SEP);
        sb.append("body, table {font-family: arial,sans-serif; font-size: x-small;}").append(LINE_SEP);
        sb.append("th {background: #336699; color: #FFFFFF; text-align: left;}").append(LINE_SEP);
        sb.append("-->").append(LINE_SEP);
        sb.append("</style>").append(LINE_SEP);
        sb.append("</head>").append(LINE_SEP);
        sb.append(loggingEvent.getRenderedMessage());
        sb.append(": ");
        sb.append("<br><body>");
        String LINE_SEP = "<br>";
        if (loggingEvent.getThrowableStrRep() != null) {
            for (String s : loggingEvent.getThrowableStrRep()){
                sb.append(s);
                sb.append(LINE_SEP);
            }
        }
        sb.append(LINE_SEP);
        if (loggingEvent.getNDC() != null){
            sb.append(loggingEvent.getNDC());
            sb.append(LINE_SEP);
        }
        sb.append(loggingEvent.fqnOfCategoryClass);
        sb.append(LINE_SEP);
        sb.append(loggingEvent.getFQNOfLoggerClass());
        sb.append(LINE_SEP);
        for (Object s : loggingEvent.getPropertyKeySet()){
            sb.append(s.toString());
            sb.append(": ");
            sb.append(loggingEvent.getProperty(s.toString()));
            sb.append(LINE_SEP);
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {

    }
}
