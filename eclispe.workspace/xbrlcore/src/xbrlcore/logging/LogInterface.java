package xbrlcore.logging;

/**
 * A common Interface for logging. 
 * It makes possible to implement different logging strategies.
 * 
 * <br>TODO: Note by Seki : it could be replaced by a common use logging facade like JCL, SLF4J, ...
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 */
public interface LogInterface {
	
    public static enum LogLevel {
        ERROR,
        WARNING,
        INFO,
        VERBOSE,
        DEBUG,
        ;
    }

    /**
     * Setter for the log level
     * @param level the new {@link LogLevel}
     */
    public void setLevel(LogLevel level);

    /**
     * Output of a log message 
     * @param level
     * @param channel
     * @param message
     */
    public void log(LogLevel level, Class<?> channel, Object message);
}
