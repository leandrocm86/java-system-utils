package lcm.java.system.logging;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Thread based logger for handling log messages.
 * It doesn't rely on other traditional logger APIs, but can be used together with them ({@link #setUtilLogger(java.util.logging.Logger)}, {@link #setSystemLogger(java.lang.System.Logger)}, {@link #setCustomOutputHandler(BiConsumer)}).
 * Contrary to traditional loggers, it doesn't require configuration files and can be totally configured programatically.
 * Also contrary to traditional loggers, it doesn't use package hierarchy for configuration, but shares configuration by threads.
 * Messages in the same Thread can share properties and logging options, and may even be grouped to be displayed together.
 * Since each Thread is mapped to a single instance of TLog, the instance doesn't need to be directly referenced outside.
 * Instead, its methods can be accessed statically, and the current thread will define which TLog instance will be used.
 * 
 * WARNING: While using TLog, it's always advisable to call {@link #clean()} in a finally block at the end of each thread.
 * This can be easily done in servers, with a single line at the most abstract class which is executed for every request.
 * It serves 3 important purposes: freeing memory, checking for unbuffered messages and enabling thread reuse with consistency.
 * 
 * @see LogLevel
 */
public class TLog {

    private static LogLevel globalDefaultMinimumLevel = LogLevel.INFO;
    private static String globalDefaultCustomHeader = "";
    private static DateTimeFormatter globalDefaultDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static int globalDefaultMaxMessageLength = 0;
    private static int globalDefaultMaxLineLength = 0;
    private static PrintStream globalDefaultPrintStream = null;
    private static String globalDefaultFilePath = null;
    private static java.lang.System.Logger globalDefaultSystemLogger = null;
    private static java.util.logging.Logger globalDefaultUtilLogger = null;
    private static BiConsumer<LogLevel, String> globalDefaultCustomOutputHandler = null;
    private static final String UNFLUSHED_MESSAGES_WARNING = "THERE WERE BUFFERED MESSAGES IN TLOG THAT WEREN'T FLUSHED BEFORE THREAD END. FLUSHING NOW...";

    private static ThreadLocal<BasicLogger> threadLocal = new ThreadLocal<>() {
        @Override
        protected BasicLogger initialValue() {
            return createLogger();
        }
        @Override
        public void remove() {
            super.get().finishInstance();
            super.remove();
        }
    };

    /**
     * Cleans TLog's data for the current thread, while also checking for possible unbuffered messages.
     * This is advised to be called inside a finally block, at the end of the thread's processing.
     * Besides freeing memory, any forgotten unbuffered message will be flushed with a warning.
     */
    public static void clean() {
        threadLocal.remove();
    }

    private static BasicLogger getInstance() {
        return threadLocal.get();
    }

    /**
     * Global default configuration for minimum log level.
     * For details about it and how to change it per thread, see {@link #setMinimumLevel(LogLevel)}.
     * @param globalDefaultMinimumLevel - Global default minimum log level to be set.
     * @see LogLevel
     * @see #setMinimumLevel(LogLevel)
     */
    public static void setGlobalDefaultMinimumLevel(LogLevel globalDefaultMinimumLevel) {
        TLog.globalDefaultMinimumLevel = globalDefaultMinimumLevel;
    }

    /**
     * Global default configuration for custom header.
     * For details about it and how to change it per thread, see {@link #setCustomHeader(String)}.
     * @param globalDefaultCustomHeader - Global default String to be concatenated just before each logged message.
     * @see #setCustomHeader(String)
     */
    public static void setGlobalDefaultCustomHeader(String globalDefaultCustomHeader) {
        TLog.globalDefaultCustomHeader = globalDefaultCustomHeader;
    }

    /**
     * Global default configuration for date/time format.
     * For details about it and how to change it per thread, see {@link #setDateTimeFormat(DateTimeFormatter)}.
     * @param globalDefaultDateTimeFormat - Global default DateTimeFormatter to be used to display the instant when logging messages.
     * @see DateTimeFormatter
     * @see #setDateTimeFormat(DateTimeFormatter)
     */
    public static void setGlobalDefaultDateTimeFormat(DateTimeFormatter globalDefaultDateTimeFormat) {
        TLog.globalDefaultDateTimeFormat = globalDefaultDateTimeFormat;
    }

    /**
     * Global default configuration for maximum message length.
     * For details about it and how to change it per thread, see {@link #setMaxMessageLength(int)}.
     * @param globalDefaultMaxMessageLength - Global default maximum number of characters to be logged in each message.
     * @see #setMaxMessageLength(int)
     */
    public static void setGlobalDefaultMaxMessageLength(int globalDefaultMaxMessageLength) {
        TLog.globalDefaultMaxMessageLength = globalDefaultMaxMessageLength;
    }

    /**
     * Global default configuration for max line length.
     * For details about it and how to change it per thread, see {@link #setMaxLineLength(int)}.
     * @param globalDefaultMaxLineLength - Global default max length to be printed of a line before a line break is added to it.
     * @see #setMaxLineLength(int)
     */
    public static void setGlobalDefaultMaxLineLength(int globalDefaultMaxLineLength) {
        TLog.globalDefaultMaxLineLength = globalDefaultMaxLineLength;
    }

    /**
     * Global default PrintStream to be used.
     * For details about it and how to change it per thread, see {@link #setPrintStream(PrintStream)}.
     * @param globalDefaultPrintStream - Global default PrintStream to be used for printing messages.
     * @see #setPrintStream(PrintStream)
     */
    public static void setGlobalDefaultPrintStream(PrintStream globalDefaultPrintStream) {
        TLog.globalDefaultPrintStream = globalDefaultPrintStream;
    }

    /**
     * Global default file path to be used.
     * For details about it and how to change it per thread, see {@link #setFilePath(String)}.
     * @param globalDefaultFilePath - Global default file path to be used for printing messages.
     * @see #setFilePath(String)
     */
    public static void setGlobalDefaultFilePath(String globalDefaultFilePath) {
        TLog.globalDefaultFilePath = globalDefaultFilePath;
    }

    /**
     * Global default java.lang.System.Logger instance to be used together with TLog.
     * For details about it and how to change it per thread, see {@link #setSystemLogger(java.lang.System.Logger)}.
     * @param globalDefaultSystemLogger - Global default java.lang.System.Logger instance to forward messages to.
     * @see LogLevel
     * @see #setSystemLogger(java.lang.System.Logger)
     */
    public static void setGlobalDefaultSystemLogger(java.lang.System.Logger globalDefaultSystemLogger) {
        TLog.globalDefaultSystemLogger = globalDefaultSystemLogger;
    }

    /**
     * Global default java.util.logging.Logger instance to be used together with TLog.
     * For details about it and how to change it per thread, see {@link #setUtilLogger(java.util.logging.Logger)}.
     * @param globalDefaultUtilLogger - Global default java.util.logging.Logger instance to forward messages to.
     * @see LogLevel
     * @see #setUtilLogger(java.util.logging.Logger)
     */
    public static void setGlobalDefaultUtilLogger(java.util.logging.Logger globalDefaultUtilLogger) {
        TLog.globalDefaultUtilLogger = globalDefaultUtilLogger;
    }

    /**
     * Global default output handler to be used when outputting messages.
     * For details about it and how to change it per thread, see {@link #setCustomOutputHandler(BiConsumer)}.
     * @param globalDefaultCustomOutputHandler - A custom BiConsumer that will receive a LogLevel and a text for each message being outputted.
     * @see LogLevel
     * @see #setCustomOutputHandler(BiConsumer)
     */
    public static void setGlobalDefaultCustomOutputHandler(BiConsumer<LogLevel, String> globalDefaultCustomOutputHandler) {
        TLog.globalDefaultCustomOutputHandler = globalDefaultCustomOutputHandler;
    }

    /**
     * Defines the minimum log level for a message to be logged on the current thread.
     * INFO is used by default if no Global was defined with {@link #setGlobalDefaultMinimumLevel(LogLevel)}.
     * Messages with the level lower than the minimum set are ignored.
     * @param minimumLevel - The minimum log level to be set on the current thread.
     * @see LogLevel
     */
    public static void setMinimumLevel(LogLevel minimumLevel) {
        getInstance().minimumLevel = minimumLevel;
    }

    /**
     * Defines a custom header to be appendend with every message on the current thread.
     * By default, no additional text is added to the message besides the date and level if no Global was defined with {@link #setGlobalDefaultCustomHeader(String)}.
     * @param customHeader - A String to be concatenated just before each logged message on the current thread.
     */
    public static void setCustomHeader(String customHeader) {
        getInstance().customHeader = customHeader;
    }

    /**
     * Defines a date/time format to be used when logging messages on the current thread.
     * By default, it is used the format "yyyy-MM-dd HH:mm:ss", if no Global was defined with {@link #setGlobalDefaultDateTimeFormat(DateTimeFormatter)}.
     * @param dateTimeFormat - A DateTimeFormatter to be used to display the instant when logging the messages on the current thread.
     * @see DateTimeFormatter
     */
    public static void setDateTimeFormat(DateTimeFormatter dateTimeFormat) {
        getInstance().dateTimeFormat = dateTimeFormat;
    }

    /**
     * Defines a maximum length to be logged for each message on the current thread.
     * By default, no limit is set if no Global was defined with {@link #setGlobalDefaultMaxMessageLength(int)}.
     * When a message surpasses this limit, it will be cut in the middle (text replaced by "(...)") to fit the limit. 
     * @param maxMessageLength - The maximum number of characters to be logged in each message on the current thread.
     */
    public static void setMaxMessageLength(int maxMessageLength) {
        getInstance().maxMessageLength = maxMessageLength;
    }

    /**
     * Forces line breaks when lines get too long. This is disabled by default, if no Global was defined with {@link #setGlobalDefaultMaxLineLength(int)}.
     * When a line break is added for a line, an identation (\t) is also inserted to make this change more visible.
     * @param maxLineLength - Max length to be printed of a line before a line break is added to it, for every message on the current thread.
     */
    public static void setMaxLineLength(int maxLineLength) {
        getInstance().maxLineLength = maxLineLength;
    }

    /**
     * Defines a PrintStream to be used when printing log messages on the current thread.
     * By default, no one is used if no Global was defined with {@link #setGlobalDefaultPrintStream(PrintStream)}.
     * @param printStream - A PrintStream to be used for printing messages on the current thread.
     */
    public static void setPrintStream(PrintStream printStream) {
        getInstance().printStream = printStream;
    }

    /**
     * Defines a file to be used for printing log messages on the current thread.
     * By default, no one is used if no Global was defined with {@link #setGlobalDefaultFilePath(String)}.
     * @param filePath - A String containing the file's path to be used for appending log messages from the current thread.
     */
    public static void setFilePath(String filePath) {
        getInstance().setFilePath(filePath);
    }

    /**
     * Defines a java.lang.System.Logger instance to be used together with TLog on the current thread.
     * By default, no one is used if no Global was defined with {@link #setGlobalDefaultSystemLogger(java.lang.System.Logger)}.
     * When a System.Logger is set, each message on the current thread being logged in TLog will be forwarded to it with the equivalent log level.
     * WARNING: The messages will still be affected by TLog's settings, such as formatting and level (messages with level below TLog's minimum aren't forwarded).
     * @param systemLogger - A java.lang.System.Logger instance for the current thread to forward messages to.
     * @see LogLevel
     */
    public static void setSystemLogger(java.lang.System.Logger systemLogger) {
        getInstance().systemLogger = systemLogger;
    }

    /**
     * Defines a java.util.logging.Logger instance to be used together with TLog on the current thread.
     * By default, no one is used if no Global was defined with {@link #setGlobalDefaultUtilLogger(java.util.logging.Logger)}.
     * When a util.logging.Logger is set, each message on the current thread being logged in TLog will be forwarded to it with the equivalent log level.
     * WARNING: The messages will still be affected by TLog's settings, such as formatting and level (messages with level below TLog's minimum aren't forwarded).
     * @param utilLogger - A java.util.logging.Logger instance for the current thread to forward messages to.
     * @see LogLevel
     */
    public static void setUtilLogger(java.util.logging.Logger utilLogger) {
        getInstance().utilLogger = utilLogger;
    }

    /**
     * Defines a custom output handler to be used when outputting messages on the current thread.
     * To define one globally, use {@link #setGlobalDefaultCustomOutputHandler(BiConsumer)}.
     * This is useful for any different output method besides the ones provided:
     * {@link #setPrintStream(PrintStream)}, {@link #setFilePath(String)}, {@link #setSystemLogger(java.lang.System.Logger)}, {@link #setUtilLogger(java.util.logging.Logger)}
     * @param customOutputHandler - A custom BiConsumer that will receive a LogLevel and a text for each message being outputted on the current thread.
     * @see LogLevel
     */
    public static void setCustomOutputHandler(BiConsumer<LogLevel, String> customOutputHandler) {
        getInstance().customOutputHandler = customOutputHandler;
    }

    /**
     * Logs a DEBUG message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void debug(String message, Object... params) {
        getInstance().debug(message, params);
    }

    /**
     * Logs an INFO message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void info(String message, Object... params) {
        getInstance().info(message, params);
    }

    /**
     * Logs a WARN message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void warn(String message, Object... params) {
        getInstance().warn(message, params);
    }

    /**
     * Logs an ERROR message. It may use optional parameters for String formatting.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void error(String message, Object... params) {
        getInstance().error(message, params);
    }

    /**
     * Logs an ERROR message. It may use optional parameters for String formatting.
     * Additionally, it prints information about the given Throwable error/exception, including its stacktrace.
     * @param throwable - Error/Exception to be summarized and have its stacktrace printed.
     * @param message - String with the text to be logged.
     * @param params - Parameters to be used for formatting the message with String.format (Optional).
     */
    public static void error(Throwable throwable, String message, Object... params) {
        getInstance().error(throwable, message, params);
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
        getInstance().error(throwable, stackTraceLimit, message, params);
    }

    /**
     * Defines wether lower level messages on the current thread should be preserved. By default this is FALSE.
     * When preserving discarded messages, messages with LogLevel lower than the minimum set will be stored in memory and can be retrieved later.
     * This may be useful for unexpected errors handling, when lower level messages can be outputted on demand by calling {@link #flushDiscardedMessages()}.
     * @param mustPreserveDiscardedMessages - Boolean indicating if lower than minimum level messages should be preserved in memory for the current thread.
     * @see #flushDiscardedMessages()
     */
    public static void preserveDiscardedMessages(boolean mustPreserveDiscardedMessages) {
        getInstance().preserveDiscardedMessages(mustPreserveDiscardedMessages);
    }

    /**
     * Defines wether logged messages on the current thread must be buffered instead of outputted immediately. This is FALSE by default.
     * Buffering messages can be useful for grouping them to be displayed together. It may also increase performance by reducing I/O operations.
     * When this method is called with value TRUE, logged messages will only be outputted when {@link #flushBufferedMessages()} is called.
     * WARNING: Buffered messages are expected to be manually flushed eventually. If that never happens, TLog tries to forcefully flush them all when the thread is being finished. 
     * @param mustBuffer - Boolean indicating if messages should be buffered instead of outputted immediately on the current thread.
     * @see #flushBufferedMessages()
     */
    public static void bufferMessages(boolean mustBuffer) {
        getInstance().bufferMessages(mustBuffer);
    }

    /**
     * Returns wether discarded (lower than minimum level) messages on the current thread are being preserved in memory.
     * @return Boolean indicating if discarded messages are being preserved on the current thread.
     * @see #preserveDiscardedMessages(boolean)
     * @see #flushDiscardedMessages()
     */
    public static boolean preserveDiscardedMessages() {
        return getInstance().preserveDiscardedMessages();
    }

    /**
     * Returns wether messages on the current thread are being buffered instead of being outputted immediately.
     * @return Boolean indicating if messages are being buffered on the current thread.
     * @see #bufferMessages(boolean)
     * @see #flushBufferedMessages()
     */
    public static boolean bufferMessages() {
        return getInstance().bufferMessages();
    }

    /**
     * Flushes all the discarded messages from the current thread to the configured outputs (stream, file or custom) and returns them, emptying the list.
     * Discarded messages are the ones with a level lower than the minimum required to be printed.
     * They might be useful in exceptional situations, such as unexpected error handling.
     * WARNING: An error will be thrown if there was no previous call in the current thread to preserve discarded messages ({@link #preserveDiscardedMessages(boolean)}).
     * Since this is a functionality for exceptional situations, storing discarded messages is disabled by default.
     * @return List of formatted strings containing all the messages flushed.
     * @see #preserveDiscardedMessages(boolean)
     */
    public static List<String> flushDiscardedMessages() {
        return getInstance().flushDiscardedMessages();
    }

    /**
     * Flushes all the buffered messages from the current thread to the configured outputs (stream, file or custom) and returns them, emptying the list.
     * WARNING: An error will be thrown if there was no previous call in the current thread to buffer messages ({@link #bufferMessages(boolean)}).
     * Buffering messages is disabled by default.
     * @return List of formatted strings containing all the messages flushed.
     * @see #bufferMessages(boolean)
     */
    public static List<String> flushBufferedMessages() {
        return getInstance().flushBufferedMessages();
    }

    private static BasicLogger createLogger() {
        BasicLogger logger = new BasicLogger();
        logger.minimumLevel = globalDefaultMinimumLevel;
        logger.customHeader = globalDefaultCustomHeader;
        logger.dateTimeFormat = globalDefaultDateTimeFormat;
        logger.maxMessageLength = globalDefaultMaxMessageLength;
        logger.maxLineLength = globalDefaultMaxLineLength;
        logger.printStream = globalDefaultPrintStream;
        logger.setFilePath(globalDefaultFilePath);
        logger.systemLogger = globalDefaultSystemLogger;
        logger.utilLogger = globalDefaultUtilLogger;
        logger.customOutputHandler = globalDefaultCustomOutputHandler;
        logger.unflushedMessagesWarning = UNFLUSHED_MESSAGES_WARNING;
        return logger;
    }

}
