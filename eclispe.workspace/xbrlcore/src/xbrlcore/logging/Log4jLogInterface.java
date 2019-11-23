package xbrlcore.logging;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * A logger that will output to Log4J
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 */
public class Log4jLogInterface implements LogInterface {
    static {
        String resourceName = Log4jLogInterface.class.getPackage().getName().replace('.', '/') + "/log4j.properties";
        //URL res = Thread.currentThread().getContextClassLoader().getResource(/*"de/bundesbank/baselsolv/*/"log4j.properties");
        URL res = Log4jLogInterface.class.getClassLoader().getResource(resourceName);
        if (res != null) {
            PropertyConfigurator.configure(res);
        }
    }

    public final Logger logger;

    public Log4jLogInterface(Logger logger) {
        this.logger = logger;
    }

    public Log4jLogInterface(Class<?> channel) {
        this.logger = Logger.getLogger(channel);
    }

    public Log4jLogInterface(Class<?> channel, LogLevel level) {
        this.logger = Logger.getLogger(channel);
        this.setLevel(level);
    }

    private static final org.apache.log4j.Level convertLevel(LogLevel level) {
        switch (level) {
            case ERROR:
                return org.apache.log4j.Level.ERROR;
            case WARNING:
                return org.apache.log4j.Level.WARN;
            case INFO:
            case VERBOSE:
                return org.apache.log4j.Level.INFO;
            case DEBUG:
                return org.apache.log4j.Level.DEBUG;
        }

        return null;
    }

    @Override
    public void setLevel(LogLevel level) {
        logger.setLevel(convertLevel(level));
    }

    @Override
    public void log(LogLevel level, Class<?> channel, Object message) {
        switch (level) {
            case INFO:
            case VERBOSE:
                logger.info(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case WARNING:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }
}
