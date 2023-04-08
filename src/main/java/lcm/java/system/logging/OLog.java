package lcm.java.system.logging;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * OLog stands for "One-and-only Log". It's a singleton logger class providing utility methods to make logging easier.
 * It doesn't rely on other traditional logger APIs, but can be used together with them ({@link #setUtilLogger(java.util.logging.Logger)}, {@link #setSystemLogger(java.lang.System.Logger)}, {@link #setCustomOutputHandler(BiConsumer)}).
 * Contrary to traditional loggers, it doesn't require configuration files and can be totally configured programatically.
 * Also contrary to traditional loggers, it doesn't use package hierarchy for configuration, but a single application-wide setup.
 * Since there's only one instance of OLog, it doesn't need to be directly referenced, and its methods are acessible statically.
 * Finally, it is based on only 4 log severity levels: DEBUG, INFO, WARN and ERROR.
 * OLog aims to provide simplicity for logging in standalone applications. For servers or other multi-threaded applications, consider using TLog instead.
 * 
 * @see LogLevel
 * @see TLog
 */
public class OLog {

    static final OLog INST = new OLog();
    private BasicLogger logger;

    private OLog() {
        this.logger = new BasicLogger();
        logger.minimumLevel = LogLevel.INFO;
        logger.customHeader = "";
        logger.dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        logger.maxMessageLength = 0;
        logger.maxLineLength = 0;
        logger.printStream = null;
        logger.filePath = null;
        logger.systemLogger = null;
        logger.utilLogger = null;
        logger.customOutputHandler = null;
        logger.unflushedMessagesWarning = "THERE WERE BUFFERED MESSAGES IN OLOG THAT WEREN'T FLUSHED BEFORE APP END. FLUSHING NOW...";
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.finishInstance()));
    }

    /**
     * Defines the minimum log level for a message to be logged. INFO is used by default.
     * Messages with the level lower than the minimum set are ignored.
     * @param minimumLevel - The minimum log level to be set.
     * @see LogLevel
     */
    public static void setMinimumLevel(LogLevel minimumLevel) {
        INST.logger.minimumLevel = minimumLevel;
    }

    /**
     * Defines a custom header to be appendend with every message.
     * By default, no additional text is added to the message besides the date and level.
     * @param customHeader - A String to be concatenated just before each logged message.
     */
    public static void setCustomHeader(String customHeader) {
        INST.logger.customHeader = customHeader;
    }

    /**
     * Defines a date/time format to be used when logging messages.
     * By default, it is used the format "yyyy-MM-dd HH:mm:ss".
     * @param dateTimeFormat - A DateTimeFormatter to be used to display the instant when logging the messages.
     * @see DateTimeFormatter
     */
    public static void setDateTimeFormat(DateTimeFormatter dateTimeFormat) {
        INST.logger.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Defines a maximum length to be logged for each message. By default, no limit is set.
     * When a message surpasses this limit, it will be cut in the middle (text replaced by "(...)") to fit the limit. 
     * @param maxMessageLength - The maximum number of characters to be logged in each message.
     */
    public static void setMaxMessageLength(int maxMessageLength) {
        INST.logger.maxMessageLength = maxMessageLength;
    }

    /**
     * Forces line breaks when lines get too long. This is disabled by default.
     * When a line break is added for a line, an identation (\t) is also inserted to make this change more visible.
     * @param maxLineLength - Max length to be printed of a line before a line break is added to it.
     */
    public static void setMaxLineLength(int maxLineLength) {
        INST.logger.maxLineLength = maxLineLength;
    }

    /**
     * Defines a PrintStream to be used when printing log messages. By default, no one is used.
     * @param printStream - A PrintStream to be used for printing messages.
     */
    public static void setPrintStream(PrintStream printStream) {
        INST.logger.printStream = printStream;
    }

    /**
     * Defines a file to be used for printing log messages. By default, no one is used.
     * @param filePath - A String containing the file's path to be used for appending log messages.
     */
    public static void setFilePath(String filePath) {
        INST.logger.filePath = (filePath != null ? Path.of(filePath) : null);
    }

    /**
     * Defines a java.lang.System.Logger instance to be used together with OLog.
     * When a System.Logger is set, each message being logged in OLog will be forwarded to it with the equivalent log level.
     * WARNING: The messages will still be affected by OLog's settings, such as formatting and level (messages with level below OLog's minimum aren't forwarded).
     * @param systemLogger - A java.lang.System.Logger instance to forward messages to.
     * @see LogLevel
     */
    public static void setSystemLogger(java.lang.System.Logger systemLogger) {
        INST.logger.systemLogger = systemLogger;
    }

    /**
     * Defines a java.util.logging.Logger instance to be used together with OLog.
     * When a util.logging.Logger is set, each message being logged in OLog will be forwarded to it with the equivalent log level.
     * WARNING: The messages will still be affected by OLog's settings, such as formatting and level (messages with level below OLog's minimum aren't forwarded).
     * @param utilLogger - A java.util.logging.Logger instance to forward messages to.
     * @see LogLevel
     */
    public static void setUtilLogger(java.util.logging.Logger utilLogger) {
        INST.logger.utilLogger = utilLogger;
    }

    /**
     * Defines a custom output handler to be used when outputting messages.
     * This is useful for any different output method besides the ones provided:
     * {@link #setPrintStream(PrintStream)}, {@link #setFilePath(String)}, {@link #setSystemLogger(java.lang.System.Logger)}, {@link #setUtilLogger(java.util.logging.Logger)}
     * @param customOutputHandler - A custom BiConsumer that will receive a LogLevel and a text for each message being outputted.
     * @see LogLevel
     */
    public static void setCustomOutputHandler(BiConsumer<LogLevel, String> customOutputHandler) {
        INST.logger.customOutputHandler = customOutputHandler;
    }

    /**
     * Logs a DEBUG message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void debug(String message, Object... params) {
        INST.logger.debug(message, params);
    }

    /**
     * Logs an INFO message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void info(String message, Object... params) {
        INST.logger.info(message, params);
    }

    /**
     * Logs a WARN message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void warn(String message, Object... params) {
        INST.logger.warn(message, params);
    }

    /**
     * Logs an ERROR message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void error(String message, Object... params) {
        INST.logger.error(message, params);
    }

    /**
     * Logs an ERROR message. It may use optional parameters for String formatting.
     * Additionally, it prints information about the given Throwable error/exception, including its stacktrace.
     * @param throwable - Error/Exception to be summarized and have its stacktrace printed.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void error(Throwable throwable, String message, Object... params) {
        INST.logger.error(throwable, message, params);
    }

    /**
     * Logs an ERROR message. It may use optional parameters for String formatting.
     * Additionally, it prints information about the given Throwable error/exception, including its stacktrace.
     * The stackTraceLimit parameter limits the quantity of lines to be outputted from a same stacktrace.
     * Stacktraces surpassing the limit will be cut in the middle (preserving the start and the end) to fit the maximum number of lines.
     * Notice the limit applies to each unique stacktrace, so chained exceptions (with causes) may have a bigger total output.
     * @param throwable - Error/Exception to be summarized and have its stacktrace printed.
     * @param stackTraceLimit - Maximum number of lines to be printed for a single stacktrace.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void error(Throwable throwable, int stackTraceLimit, String message, Object... params) {
        INST.logger.error(throwable, stackTraceLimit, message, params);
    }

    /**
     * Defines wether lower level messages should be preserved. By default this is FALSE.
     * When preserving discarded messages, messages with LogLevel lower than the minimum set will be stored in memory and can be retrieved later.
     * This may be useful for unexpected errors handling, when lower level messages can be outputted on demand by calling {@link #flushDiscardedMessages()}.
     * @param mustPreserveDiscardedMessages - Boolean indicating wether lower than minimum level messages should be preserved in memory.
     * @see #flushDiscardedMessages()
     */
    public static void preserveDiscardedMessages(boolean mustPreserveDiscardedMessages) {
        INST.logger.preserveDiscardedMessages(mustPreserveDiscardedMessages);
    }

    /**
     * Defines wether logged messages must be buffered instead of outputted immediately. This is FALSE by default.
     * Buffering messages can be useful for grouping them to be displayed together. It may also increase performance by reducing I/O operations.
     * When this method is called with value TRUE, logged messages will only be outputted when {@link #flushBufferedMessages()} is called.
     * WARNING: Buffered messages are expected to be manually flushed eventually. If that never happens, OLog tries to forcefully flush them all when the application is being shutdown. 
     * @param mustBuffer - Boolean indicating if messages should be buffered instead of outputted immediately.
     * @see #flushBufferedMessages()
     */
    public static void bufferMessages(boolean mustBuffer) {
        INST.logger.bufferMessages(mustBuffer);
    }

    /**
     * Returns wether discarded (lower than minimum level) messages are being preserved in memory.
     * @return Boolean indicating if discarded messages are being preserved.
     * @see #preserveDiscardedMessages(boolean)
     * @see #flushDiscardedMessages()
     */
    public static boolean preserveDiscardedMessages() {
        return INST.logger.preserveDiscardedMessages();
    }

    /**
     * Returns wether messages are being buffered instead of being outputted immediately.
     * @return Boolean indicating if messages are being buffered.
     * @see #bufferMessages(boolean)
     * @see #flushBufferedMessages()
     */
    public static boolean bufferMessages() {
        return INST.logger.bufferMessages();
    }

    /**
     * Flushes all the discarded messages to the configured outputs (stream, file or custom) and returns them, emptying the list.
     * Discarded messages are the ones with a level lower than the minimum required to be printed.
     * They might be useful in exceptional situations, such as unexpected error handling.
     * WARNING: An error will be thrown if there was no previous call to preserve discarded messages ({@link #preserveDiscardedMessages(boolean)}).
     * Since this is a functionality for exceptional situations, storing discarded messages is disabled by default.
     * @return List of formatted strings containing all the messages flushed.
     * @see #preserveDiscardedMessages(boolean)
     */
    public static List<String> flushDiscardedMessages() {
        return INST.logger.flushDiscardedMessages();
    }

    /**
     * Flushes all the buffered messages to the configured outputs (stream, file or custom) and returns them, emptying the list.
     * WARNING: An error will be thrown if there was no previous call to buffer messages ({@link #bufferMessages(boolean)}).
     * Buffering messages is disabled by default.
     * @return List of formatted strings containing all the messages flushed.
     * @see #bufferMessages(boolean)
     */
    public static List<String> flushBufferedMessages() {
        return INST.logger.flushBufferedMessages();
    }
}
