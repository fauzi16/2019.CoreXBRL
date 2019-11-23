package xbrlcore.logging;

/**
 * A dummy logger that will produce no output
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 */
public class DevNullLogInterface implements LogInterface {
    public DevNullLogInterface() {
    }

    @Override
    public void setLevel(LogLevel level) {
    }

    @Override
    public void log(LogLevel level, Class<?> channel, Object message) {
    }
}
