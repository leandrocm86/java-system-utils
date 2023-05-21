package lcm.java.system.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import lcm.java.system.Filer;

class BasicLogger {

    class LogMessage {
        final LogLevel level;
        final LocalDateTime timestamp;
        final String text;
        private String formattedText;

        LogMessage(LogLevel level, LocalDateTime timestamp, String text, Object... params) {
            this.level = level;
            this.timestamp = timestamp;
            this.text = params != null ? String.format(text, params) : text;
        }

        public String getFormattedText() {
            if (formattedText == null) {
                formattedText = text;
                String date = dateTimeFormat.format(timestamp);
                if (maxMessageLength > 0 && formattedText.length() > maxMessageLength) {
                    int charsToCut = formattedText.length() - maxMessageLength;
                    int cutStartIndex = formattedText.length() / 2 - charsToCut / 2;
                    int cutEndIndex = formattedText.length() / 2 + charsToCut / 2;
                    formattedText = formattedText.substring(0, cutStartIndex) + "(...)" + formattedText.substring(cutEndIndex);
                }
                if (maxLineLength > 0 && formattedText.length() > maxLineLength) {
                    StringJoiner sj = new StringJoiner(System.lineSeparator() + "\t");
                    for (String line : formattedText.split(System.lineSeparator())) {
                        while (line.length() > maxLineLength) {
                            sj.add(line.substring(0, maxLineLength));
                            line = line.substring(maxLineLength);
                        }
                        sj.add(line);
                    }
                    formattedText = sj.toString();
                }
                formattedText = String.format("%s [%s] %s%s", date, level.name(), customHeader, formattedText);
            }
            return formattedText;
        }
    }

    LogLevel minimumLevel;
    String customHeader;
    DateTimeFormatter dateTimeFormat;
    int maxMessageLength;
    int maxLineLength;
    PrintStream printStream;
    private Path filePath;
    java.lang.System.Logger systemLogger;
    java.util.logging.Logger utilLogger;
    BiConsumer<LogLevel, String> customOutputHandler;
    List<LogMessage> discardedMessages = null;
    List<LogMessage> bufferedMessages = null;
    String unflushedMessagesWarning = null;

    protected void setFilePath(String filePath) {
        this.filePath = null;
        if (filePath == null)
            return;
        try {
            this.filePath = Filer.getForWriting(filePath).getFilePath();
        } catch (Throwable e) {
            error(e, "Couldn't create/open file to write on %s", filePath);
        }
    }

    void debug(String message, Object... params) {
        logMessage(LogLevel.DEBUG, message, params);
    }

    void info(String message, Object... params) {
        logMessage(LogLevel.INFO, message, params);
    }

    void warn(String message, Object... params) {
        logMessage(LogLevel.WARN, message, params);
    }

    void error(String message, Object... params) {
        logMessage(LogLevel.ERROR, message, params);
    }

    void error(Throwable t, String message, Object... params) {
        error(t, 0, message, params);
    }

    void error(Throwable t, int stackTraceLimit, String message, Object... params) {
        if (message == null)
            message = "";
        message += System.lineSeparator() + summary(t);

        while (t != null) {
            message += System.lineSeparator() + "STACKTRACE:" + System.lineSeparator();
            StackTraceElement[] stack = t.getStackTrace();
            if (stackTraceLimit > 0 && stack.length >  stackTraceLimit) {
                int linesToCut = stack.length - stackTraceLimit;
                int cutStartIndex = stack.length / 2 - linesToCut / 2;
                int cutEndIndex = stack.length / 2 + linesToCut / 2;
                message += stackTraceToString(t, 0, cutStartIndex)
                + System.lineSeparator() + "(...)" + System.lineSeparator()
                + stackTraceToString(t, cutEndIndex, stack.length);
            } else {
                message += stackTraceToString(t, 0, stack.length);
            }
            t = t.getCause();
            if (t != null)
                message += System.lineSeparator() + "CAUSED BY: " + t.getClass().getName() + (t.getMessage() != null ? ": " + t.getMessage() : "");
        }
        logMessage(LogLevel.ERROR, message, params);
    }

    String summary(Throwable t) {
		String summary = "\tEXCEPTION: " + t.getClass().getName();
		if (t.getMessage() != null)
			summary += ": " + t.getMessage();
		Throwable cause = t.getCause();
		while (cause != null) {
			summary += System.lineSeparator() + "\tCAUSE: " + cause.getClass().getName();
            if (cause.getMessage() != null)
                summary += ": " + cause.getMessage();
			cause = cause.getCause();
		}
		return summary;
	}

    String stackTraceToString(Throwable t, int startingIndex, int endingIndex) {
        StringJoiner sj = new StringJoiner(System.lineSeparator());
		for (int i = startingIndex; i < endingIndex ; i++)
			sj.add(t.getStackTrace()[i].toString());
		return sj.toString();
	}

    void preserveDiscardedMessages(boolean mustPreserveDiscardedMessages) {
        discardedMessages = mustPreserveDiscardedMessages ? new ArrayList<>() : null;
    }

    void bufferMessages(boolean mustBuffer) {
        bufferedMessages = mustBuffer ? new ArrayList<>() : null;
    }

    void finishInstance() {
        if (bufferedMessages != null && !bufferedMessages.isEmpty()) {
            if (unflushedMessagesWarning != null) {
                warn(unflushedMessagesWarning);
                bufferedMessages.add(0, bufferedMessages.remove(bufferedMessages.size() - 1)); // Moves the warning message above to be printed first.
            }
            flushBufferedMessages();
        }
    }

    boolean preserveDiscardedMessages() {
        return discardedMessages != null;
    }

    boolean bufferMessages() {
        return bufferedMessages != null;
    }

    /**
     * Flushes all the discarded messages to the configured outputs (stream, file or custom) and returns them, emptying the list.
     * Discarded messages are the ones with a level lower than the minimum required to be printed.
     * They might be useful in exceptional situations, such as unexpected error handling.
     * An error will be thrown if there was no previous call in the current thread to persist discarded messages ({@link #preserveDiscardedMessages(boolean)}).
     * Since this is a functionality for exceptional situations, storing discarded messages is disabled by default.
     * 
     * @return List of formatted strings containing all the messages flushed.
     */
    List<String> flushDiscardedMessages() {
        assert discardedMessages != null : "Call to flush discarded messages, but persistDiscardedMessages(true) wasn't called on this thread.";
        return flushMessages(discardedMessages);
    }

    List<String> flushBufferedMessages() {
        assert bufferedMessages != null : "Call to flush buffered messages, but bufferMessages(true) wasn't called on this thread.";
        return flushMessages(bufferedMessages);
    }

    private List<String> flushMessages(List<LogMessage> messages) {
        ArrayList<String> formattedMessages = new ArrayList<>();
        for (LogMessage logMessage : messages) {
            String text = logMessage.getFormattedText();
            formattedMessages.add(text);
            delegate(logMessage.level, text);
        }
        print(String.join(System.lineSeparator(), formattedMessages));
        messages.clear();
        return formattedMessages;
    }

    synchronized void logMessage(LogLevel level, String message, Object... params) {
        if (level.code >= minimumLevel.code) {
            LogMessage logMessage = new LogMessage(level, LocalDateTime.now(), message, params);
            if (bufferedMessages != null)
                bufferedMessages.add(logMessage);
            else {
                delegate(level, logMessage.getFormattedText());
                print(logMessage.getFormattedText());
            }
        } else if (discardedMessages != null) {
            discardedMessages.add(new LogMessage(level, LocalDateTime.now(), message, params));
        }
    }

    void print(String text) {
        if (printStream != null)
            printStream.println(text);
        if (filePath != null)
            synchronized (this) {
                try {
                    Files.writeString(filePath, text + System.lineSeparator(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Couldn't append on file " + filePath, e);
                }
            }
    }

    void delegate(LogLevel level, String text) {
        if (systemLogger != null)
            systemLogger.log(level.systemLoggerLevel, text);
        if (utilLogger != null)
            utilLogger.log(level.utilLoggerLevel, text);
        if (customOutputHandler != null)
            customOutputHandler.accept(level, text);
    }

}
