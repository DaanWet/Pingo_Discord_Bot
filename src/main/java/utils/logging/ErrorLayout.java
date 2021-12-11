package utils.logging;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class ErrorLayout extends Layout {
    @Override
    public String format(LoggingEvent loggingEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append(loggingEvent.getRenderedMessage());
        sb.append(": ");
        sb.append(Layout.LINE_SEP);
        if (loggingEvent.getThrowableStrRep() != null) {
            for (String s : loggingEvent.getThrowableStrRep()){
                sb.append(s);
                sb.append(Layout.LINE_SEP);
            }
        }
        sb.append(Layout.LINE_SEP);
        if (loggingEvent.getNDC() != null){
            sb.append(loggingEvent.getNDC());
            sb.append(Layout.LINE_SEP);
        }
        sb.append(loggingEvent.fqnOfCategoryClass);
        sb.append(Layout.LINE_SEP);
        sb.append(loggingEvent.getFQNOfLoggerClass());
        sb.append(Layout.LINE_SEP);
        for (Object s : loggingEvent.getPropertyKeySet()){
            sb.append(s.toString());
            sb.append(": ");
            sb.append(loggingEvent.getProperty(s.toString()));
            sb.append(Layout.LINE_SEP);
        }
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
