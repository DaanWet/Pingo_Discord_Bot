package utils.logging;

import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileAppender extends FileAppender {
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
    public void close() {
        super.close();
        fileAppender.close();
    }

    @Override
    public void doAppend(LoggingEvent loggingEvent) {
        if (loggingEvent.getLevel().isGreaterOrEqual(Level.WARN)){
            try {
                int i = new File(folder).listFiles().length;
                FileWriter myWriter = new FileWriter(String.format("%s/log_%d", folder, i));
                myWriter.write(this.layout.format(loggingEvent));
                myWriter.close();
                loggingEvent.setProperty("link", Integer.toString(i));
            } catch (IOException exc){
                exc.printStackTrace();
            }
        }
        fileAppender.doAppend(loggingEvent);
    }


    @Override
    public boolean requiresLayout() {
        return false;
    }
}
