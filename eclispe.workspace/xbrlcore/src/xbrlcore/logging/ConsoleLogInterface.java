package xbrlcore.logging;

/**
 * A logger that will output to the system console
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 */
public class ConsoleLogInterface implements LogInterface {
    private LogLevel level = LogLevel.INFO;

    public ConsoleLogInterface() {
    }

    public ConsoleLogInterface(LogLevel level) {
        setLevel(level);
    }

    @Override
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    @Override
    public void log(LogLevel level, Class<?> channel, Object message) {
        if (level.ordinal() <= this.level.ordinal()) {
            if ((level == LogLevel.WARNING) || (level == LogLevel.ERROR))
                System.err.println(message);
            else
                System.out.println(message);
        }
    }
}
