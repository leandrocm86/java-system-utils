package lcm.java.system;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Helper class for logging in file and/or print stream.
 */
public class Log {

    private static final DateTimeFormatter dtfmt = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");

    private static F file;
    private static PrintStream stream;
    private static List<String> buffer;

    /**
     * Sets a file in the given path to have each log message appended.
     * If the given path is null, no file will be used.
     */
    public static void initFile(String filePath) {
        file = filePath != null ? F.getOrCreate(filePath) : null;
    }

    /**
     * Sets a PrintStream to redirect each log message to.
     */
    public static void initStream(PrintStream stream) {
        Log.stream = stream;
    }

    /**
     * Prepares a buffer to start storing log messages in memory.
     * @see #flushMessages(String)
     * @see #flushMessages(String, int)
     */
    public static void initBuffer() {
        buffer = new ArrayList<>();
    }

    /**
     * Saves all buffered messages in a given file.
     * The buffer must have been initialized with {@link #initBuffer()}
     * @param filePath - The path of the file to append the messages to.
     * @throws IllegalStateException if the buffer was not initialized.
     */
    public static void flushMessages(String filePath) {
        flushMessages(filePath, 0);
    }

    /**
     * Saves the last messages being buffered in a given file.
     * The buffer must have been initialized with {@link #initBuffer()}
     * @param filePath - The path of the file to append the messages to.
     * @param maxToSave - Maximum number of messages to save. The oldest messages will be discarded if needed.
     * @throws IllegalStateException if the buffer was not initialized.
     */
    public static void flushMessages(String filePath, int maxToSave) {
        if (buffer == null)
            throw new IllegalStateException("Log buffer was not initialized. There's nothing to save.");
        if (maxToSave > 0 && maxToSave < buffer.size()) {
            buffer = buffer.subList(buffer.size() - maxToSave, buffer.size());
        }
        F.getOrCreate(filePath).append(buffer);
        buffer.clear();
    }

    /**
     * Logs a DEBUG message.
     */
    public static void d(String msg) {
        log(" [DEBUG] " + msg);
    }

    /**
     * Logs an INFO message.
     */
    public static void i(String msg) {
        log(" [INFO] " + msg);
    }

    /**
     * Logs a WARN message.
     */
    public static void w(String msg) {
        log(" [WARN] " + msg);
    }

    /**
     * Logs an ERROR message.
     */
    public static void e(String msg) {
        e(msg, null, 0);
    }

    /**
     * Logs an abstract of a given exception.
     */
    public static void e(Throwable t) {
        e(null, t, 0);
    }

    /**
     * Logs an abstract of a given exception and a portion of its stacktrace.
     */
    public static void e(Throwable t, int totalTraceLines) {
        e(null, t, totalTraceLines);
    }

    /**
     * Logs an ERROR message and an abstract of a given exception.
     */
    public static void e(String msg, Throwable t) {
        e(msg, t, 0);
    }

    /**
     * Logs an ERROR message, an abstract of a given exception and a portion of its stacktrace.
     */
    public static void e(String msg, Throwable t, int totalTraceLines) {
        String errorMsg = " [ERROR] ";
        if (msg != null)
            errorMsg += msg + System.lineSeparator();
        if (t != null)
            errorMsg += exToString(t, totalTraceLines);
		log(errorMsg);
	}

    private static String exToString(Throwable t) {
        String ex = t.getClass().getName();
        if (t.getMessage() != null)
            ex += ": " + t.getMessage();
        return ex;
    }

    private static String exToString(Throwable t, int totalTraceLines) {
        StringJoiner sj = new StringJoiner(System.lineSeparator());
		sj.add(exToString(t));
		Throwable cause = t.getCause();
		while (cause != null) {
			sj.add("Cause: " + exToString(cause));
			cause = cause.getCause();
		}
        if (totalTraceLines > 0) {
            sj.add("Stacktrace:");
            Arrays.stream(t.getStackTrace()).limit(totalTraceLines).forEach(line -> sj.add(line.toString()));
        }
		return sj.toString();
	}

    private static void log(String message) {
        message = dtfmt.format(LocalDateTime.now()) + message;
        if (file != null) {
            file.appendLn(message);
        }
        if (stream != null) {
            stream.println(message);
        }
        if (buffer != null) {
            buffer.add(message);
        }
    }

}
