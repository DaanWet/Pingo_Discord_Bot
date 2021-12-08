package utils.logging;

import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileAppender implements Appender {
    public static final String folder = "./logging";
    private final FileAppender fileAppender;

    public MyFileAppender(){
        fileAppender = new FileAppender();
        HTMLLayout layout = new MyHTMLLayout();
        layout.setTitle("All logs");
        fileAppender.setThreshold(Level.ALL);
        fileAppender.setFile(String.format("%s/log.html", MyFileAppender.folder));
        fileAppender.setLayout(layout);
        fileAppender.activateOptions();
    }


    @Override
    public void addFilter(Filter filter) {

    }

    @Override
    public Filter getFilter() {
        return null;
    }

    @Override
    public void clearFilters() {

    }

    @Override
    public void close() {
        fileAppender.close();
    }

    @Override
    public void doAppend(LoggingEvent loggingEvent) {
        if (loggingEvent.getLevel().isGreaterOrEqual(Level.WARN)){
            try {
                int i = new File(folder).listFiles().length;
                FileWriter myWriter = new FileWriter(String.format("%s/log_%d", folder, i));
                myWriter.write(loggingEvent.getRenderedMessage());
                myWriter.write(": ");
                myWriter.write(Layout.LINE_SEP);
                if (loggingEvent.getThrowableStrRep() != null) {
                    for (String s : loggingEvent.getThrowableStrRep()){
                        myWriter.write(s);
                        myWriter.write(Layout.LINE_SEP);
                    }
                }
                myWriter.write(Layout.LINE_SEP);
                if (loggingEvent.getNDC() != null){
                    myWriter.write(loggingEvent.getNDC());
                    myWriter.write(Layout.LINE_SEP);
                }
                myWriter.write(loggingEvent.fqnOfCategoryClass);
                myWriter.write(Layout.LINE_SEP);
                myWriter.write(loggingEvent.getFQNOfLoggerClass());
                myWriter.write(Layout.LINE_SEP);
                for (Object s : loggingEvent.getPropertyKeySet()){
                    myWriter.write(s.toString());
                    myWriter.write(": ");
                    myWriter.write(loggingEvent.getProperty(s.toString()));
                    myWriter.write(Layout.LINE_SEP);
                }
                myWriter.close();
                loggingEvent.setProperty("link", Integer.toString(i));
            } catch (IOException exc){
                exc.printStackTrace();
            }
        }
        fileAppender.doAppend(loggingEvent);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {

    }

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void setLayout(Layout layout) {

    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
