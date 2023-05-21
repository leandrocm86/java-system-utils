package lcm.java.system.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lcm.java.system.Filer;


class TLogTest {

    OutputHandlerMock outMock;
    OutputHandlerMock outMock2;

    @BeforeEach
    void init() {
        outMock = new OutputHandlerMock();
        outMock2 = new OutputHandlerMock();
        TLog.setGlobalDefaultCustomOutputHandler(outMock);

        TLog.setGlobalDefaultMinimumLevel(LogLevel.INFO);
        TLog.setGlobalDefaultDateTimeFormat(DateTimeFormatter.ISO_LOCAL_TIME);
        TLog.setGlobalDefaultCustomHeader("");
        TLog.setGlobalDefaultMaxMessageLength(0);
        TLog.setGlobalDefaultMaxLineLength(0);
        TLog.setGlobalDefaultPrintStream(null);
        TLog.setGlobalDefaultFilePath(null);
        TLog.setGlobalDefaultSystemLogger(null);
        TLog.setGlobalDefaultUtilLogger(null);

        outMock.clear(true);
        outMock2.clear(true);
    }

    @Test
    void testGlobalSettings() {
        TLog.setGlobalDefaultMinimumLevel(LogLevel.DEBUG);
        TLog.setGlobalDefaultDateTimeFormat(DateTimeFormatter.ISO_LOCAL_TIME);
        TLog.setGlobalDefaultCustomHeader("HEADER TEST");
        TLog.setGlobalDefaultMaxMessageLength(10);
        TLog.setGlobalDefaultMaxLineLength(5);
        TLog.setGlobalDefaultPrintStream(OutputHandlerMock.PS_MOCK);
        TLog.setGlobalDefaultFilePath(OutputHandlerMock.TEST_FILE);
        TLog.setGlobalDefaultCustomOutputHandler(outMock2);
        TLog.setGlobalDefaultSystemLogger(OutputHandlerMock.SYSLOGGER_MOCK);
        TLog.setGlobalDefaultUtilLogger(OutputHandlerMock.UTILLOGGER_MOCK);

        new TestThread(() -> {
            TLog.debug("12345678901234");
            var output = outMock2.verifyAllOutputs("12345" + System.lineSeparator() + "\t(...)" + System.lineSeparator() + "\t01234");
            assertTrue(output.get(0).contains("HEADER TEST"));
            assertTrue(output.get(0).charAt(2) == ':' && output.get(0).charAt(5) == ':');
        }).startAndJoin();

        TLog.debug("12345678901234"); // This should be ignored, because the main thread was born after the global change.
        assertTrue(outMock.messages.isEmpty());
    }

    @Test
    void testParallelLogging() {
        TLog.setGlobalDefaultFilePath(OutputHandlerMock.TEST_FILE);
        new TestThread(() -> {
            TLog.info("T1 message one");
            TLog.info("T1 message two");
        }).startAndJoin();
        new TestThread(() -> {
            TLog.setCustomOutputHandler(outMock2);
            TLog.info("T2 message one");
            TLog.info("T2 message two");
        }).startAndJoin();

        outMock.verifyOutput("T1 message one", "T1 message two");
        outMock2.verifyOutput("T2 message one", "T2 message two");
        assertEquals(Stream.concat(outMock.messages.stream(), outMock2.messages.stream()).toList(), Filer.get(OutputHandlerMock.TEST_FILE).readAsList());
    }

    @Test
    void testUnbufferedMessages() {
        new TestThread(() -> {
            TLog.bufferMessages(true);
            TLog.info("Message one");
            TLog.info("Message two");
            TLog.clean();
        }).startAndJoin();
        outMock.verifyOutput("FLUSHING NOW...", "Message one", "Message two");
    }

    private class TestThread {
        final Thread thread;
        Throwable error = null;
        public TestThread(Runnable runnable) {
            thread = new Thread(() -> {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    error = e;
                }
            });
        }
        public void startAndJoin() {
            start();
            joinAndVerify();
        }
        public void start() {
            thread.start();
        }

        public void joinAndVerify() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("Something wrong with Tlog's test thread. Thread interrupted!");
            }
            if (error != null) {
                error.printStackTrace();
                fail("Something wrong with Tlog's test thread: " + error.getMessage());
            }
        }
    }

}
