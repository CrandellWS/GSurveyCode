package videoworker;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class LogbackFileUtils {

    public static final String MY_LOGGER = "MY_LOGGER";
    private static FileAppender<ILoggingEvent> fileAppender;
    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger myLogger = loggerContext.getLogger(MY_LOGGER);

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(loggerContext);
            encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %msg %n");
            encoder.start();

            fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setContext(loggerContext);
            fileAppender.setName("MY_FILE_LOGGER");
            fileAppender.setAppend(false);
            fileAppender.setEncoder(encoder);
            myLogger.addAppender(fileAppender);

            initialized = true;
        }
    }

    public static void start(String filePath) {
        init();
        stop();
        fileAppender.setFile(filePath);
        fileAppender.start();
    }

    public static void stop() {
        if (fileAppender.isStarted()) {
            fileAppender.stop();
        }
    }
}