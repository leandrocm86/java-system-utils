package lcm.java.system.logging;

/**
 * Defines four basic log levels to be used in logging.
 * Each of them is associated with a severity (int), and analogous levels from APIs lang.System.Logger and util.logging.
 */
public enum LogLevel {
    DEBUG(1, java.lang.System.Logger.Level.DEBUG, java.util.logging.Level.FINE),
    INFO(2, java.lang.System.Logger.Level.INFO, java.util.logging.Level.INFO),
    WARN(3, java.lang.System.Logger.Level.WARNING, java.util.logging.Level.WARNING),
    ERROR(4, java.lang.System.Logger.Level.ERROR, java.util.logging.Level.SEVERE);

    /**
     * Severity of the log level. The higher the severity, the most importance is given to the messages.
     */
    public final int code;

    /**
     * The equivalent java.lang.System.Logger.Level associated with the log level.
     */
    public final java.lang.System.Logger.Level systemLoggerLevel;

    /**
     * The equivalent java.util.Logger.Level associated with the log level.
     */
    public final java.util.logging.Level utilLoggerLevel;

    private LogLevel(int code, 
            java.lang.System.Logger.Level systemLoggerLevel, 
            java.util.logging.Level utilLoggerLevel) {
        this.code = code;
        this.systemLoggerLevel = systemLoggerLevel;
        this.utilLoggerLevel = utilLoggerLevel;
    }
}