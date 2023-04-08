package lcm.java.system.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import lcm.java.system.Filer;

class PrintStreamMock extends PrintStream {
    ArrayList<String> printedLines = new ArrayList<>();
    public PrintStreamMock(java.io.OutputStream arg0) {
        super(arg0);
    }
    @Override
    public void println(java.lang.String arg0) {
        printedLines.add(arg0);
    }
}

class SysLoggerMock implements java.lang.System.Logger {
    ArrayList<String> loggedMessages = new ArrayList<>();
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }
    @Override
    public boolean isLoggable(Level arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'isLoggable'");
    }
    @Override
    public void log(Level arg0, ResourceBundle arg1, String arg2, Throwable arg3) {
        throw new UnsupportedOperationException("Unimplemented method 'log'");
    }
    @Override
    public void log(Level arg0, ResourceBundle arg1, String arg2, Object... arg3) {
        loggedMessages.add(arg2);
    }
}

class UtilLoggerMock extends java.util.logging.Logger {
    ArrayList<String> loggedMessages = new ArrayList<>();
    UtilLoggerMock() {
        super(UtilLoggerMock.class.getName(), null);
    }
    @Override
    public void log(java.util.logging.Level level, String message) {
        loggedMessages.add(message);
    }
}

class OutputHandlerMock implements BiConsumer<LogLevel, String> {
    static final String TEST_FILE = "test.log";
    static final PrintStreamMock PS_MOCK = new PrintStreamMock(System.out);
    static final SysLoggerMock SYSLOGGER_MOCK = new SysLoggerMock();
    static final UtilLoggerMock UTILLOGGER_MOCK = new UtilLoggerMock();

    ArrayList<String> messages = new ArrayList<>();

    List<String> verifyOutput(String... expectedMsgs) {
        assertEquals(expectedMsgs.length, this.messages.size());
        for (int i = 0; i < expectedMsgs.length; i++)
            assertTrue(this.messages.get(i).endsWith(expectedMsgs[i]));
        return new ArrayList<>(this.messages);
    }

    List<String> verifyFileOutput(String... expectedMsgs) {
        assertTrue(Filer.exists(TEST_FILE));
        var fileMsgs = Filer.get(TEST_FILE).readAsList();
        for (int i = 0; i < expectedMsgs.length; i++)
            assertTrue(fileMsgs.get(i).endsWith(expectedMsgs[i]));
        return fileMsgs;
    }

    List<String> verifyStreamOutput(String... expectedMsgs) {
        if (PS_MOCK.printedLines.size() == 1 && expectedMsgs.length > 1) { // Assuming messages might be buffered
            var lines = PS_MOCK.printedLines.get(0).split(System.lineSeparator());
            assertEquals(expectedMsgs.length, lines.length);
            for (int i = 0; i < expectedMsgs.length; i++)
                assertTrue(lines[i].endsWith(expectedMsgs[i]));
            return List.of(lines);
        }
        else { // Messages were not buffered
            assertEquals(expectedMsgs.length, PS_MOCK.printedLines.size());
            for (int i = 0; i < expectedMsgs.length; i++)
                assertTrue(PS_MOCK.printedLines.get(i).endsWith(expectedMsgs[i]));
            return new ArrayList<>(PS_MOCK.printedLines);
        }
    }

    List<String> verifySysLoggerOutput(String... expectedMsgs) {
        assertEquals(expectedMsgs.length, SYSLOGGER_MOCK.loggedMessages.size());
        for (int i = 0; i < expectedMsgs.length; i++)
            assertTrue(SYSLOGGER_MOCK.loggedMessages.get(i).endsWith(expectedMsgs[i]));
        return new ArrayList<>(SYSLOGGER_MOCK.loggedMessages);
    }

    List<String> verifyUtilLoggerOutput(String... expectedMsgs) {
        assertEquals(expectedMsgs.length, UTILLOGGER_MOCK.loggedMessages.size());
        for (int i = 0; i < expectedMsgs.length; i++)
            assertTrue(UTILLOGGER_MOCK.loggedMessages.get(i).endsWith(expectedMsgs[i]));
        return new ArrayList<>(UTILLOGGER_MOCK.loggedMessages);
    }

    List<String> verifyAllOutputs(String... expectedMsgs) {
        var customOutput = verifyOutput(expectedMsgs);
        var fileOutput = verifyFileOutput(expectedMsgs);
        var streamOutput = verifyStreamOutput(expectedMsgs);
        var sysLoggerOutput = verifySysLoggerOutput(expectedMsgs);
        var utilLoggerOutput = verifyUtilLoggerOutput(expectedMsgs);
        assertEquals(customOutput, fileOutput);
        assertEquals(streamOutput, fileOutput);
        assertEquals(streamOutput, sysLoggerOutput);
        assertEquals(utilLoggerOutput, sysLoggerOutput);
        return customOutput;
    }

    void verifyNoOutput() {
        assertFalse(Filer.exists(TEST_FILE));
        assertTrue(this.messages.isEmpty());
        assertTrue(PS_MOCK.printedLines.isEmpty());
        assertTrue(SYSLOGGER_MOCK.loggedMessages.isEmpty());
        assertTrue(UTILLOGGER_MOCK.loggedMessages.isEmpty());
    }
    
    void clear() {
        this.messages.clear();
        Filer.deleteIfExists(TEST_FILE);
        PS_MOCK.printedLines.clear();
        SYSLOGGER_MOCK.loggedMessages.clear();
        UTILLOGGER_MOCK.loggedMessages.clear();
    }

    @Override
    public void accept(LogLevel arg0, String arg1) {
        messages.add(arg1);
    }
}
