package utils.logging;

import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileAppender extends FileAppender {
    public static final String folder = "./logging";
    private final FileAppender fileAppender;
    private final FileAppender rateLimitAppender;

    public MyFileAppender() {
        fileAppender = new FileAppender();
        MyHTMLLayout layout = new MyHTMLLayout();
        layout.setTitle("All logs");
        fileAppender.setThreshold(Level.ALL);
        fileAppender.setFile(String.format("%s/log.html", folder));
        fileAppender.setLayout(layout);
        fileAppender.activateOptions();
        rateLimitAppender = new FileAppender();
        MyHTMLLayout rateLayout = new MyHTMLLayout();
        rateLayout.setTitle("RateLimit logs");
        rateLayout.setLink(false);
        rateLimitAppender.setThreshold(Level.ALL);
        rateLimitAppender.setFile(String.format("%s/rate_log.html", folder));
        rateLimitAppender.setLayout(layout);
        rateLimitAppender.activateOptions();
    }


    @Override
    public void close() {
        super.close();
        fileAppender.close();
        rateLimitAppender.close();
    }

    @Override
    public void doAppend(LoggingEvent loggingEvent) {
        if (!loggingEvent.getThreadName().contains("RateLimit")){
            if (loggingEvent.getLevel().isGreaterOrEqual(Level.WARN)) {
                try {
                    int i = new File(folder).listFiles().length;
                    FileWriter myWriter = new FileWriter(String.format("%s/log_%d.html", folder, i));
                    myWriter.write(this.layout.format(loggingEvent));
                    myWriter.close();
                    loggingEvent.setProperty("link", Integer.toString(i));
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
            fileAppender.doAppend(loggingEvent);
        } else {
            rateLimitAppender.append(loggingEvent);
        }
    }


    @Override
    public boolean requiresLayout() {
        return false;
    }
}
