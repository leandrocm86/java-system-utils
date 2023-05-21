package lcm.java.system.logging;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.TestAbortedException;

class BasicLoggerTest {
    
    BasicLogger logger;
    OutputHandlerMock outMock = new OutputHandlerMock();
    
    @BeforeEach
    void init() {
        logger = new BasicLogger();
        logger.minimumLevel = LogLevel.INFO;
        logger.dateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        logger.customHeader = "";
        logger.customOutputHandler = outMock;
        logger.setFilePath(OutputHandlerMock.TEST_FILE);
        logger.printStream = OutputHandlerMock.PS_MOCK;
        logger.systemLogger = OutputHandlerMock.SYSLOGGER_MOCK;
        logger.utilLogger = OutputHandlerMock.UTILLOGGER_MOCK;
        logger.unflushedMessagesWarning = "MESSAGES UNFLUSHED. FLUSHING THEM NOW...";
    }

    @AfterEach
    void deleteTestFile() {
        outMock.clear(true);
    }

    @Test
    void testBasicUsage() {
        logger.info("Message one");
        logger.info("Message two");
        logger.info("Message three");
        outMock.verifyAllOutputs("Message one", "Message two", "Message three");
    }

    @Test
    void testFormattedBasicUsage() {
        logger.customHeader = "CUSTOM HEADER";
        logger.info("Message %s", "one");
        logger.info("Message %s", "two");
        logger.info("Message %d", 3);
        var outputs = outMock.verifyAllOutputs("Message one", "Message two", "Message 3");
        for (String out : outputs) {
            assertTrue(out.contains("INFO"));
            assertTrue(out.contains("CUSTOM HEADER"));
        }
    }

    @Test
    void testLogBuffer() {
        logger.bufferMessages(true);
        logger.info("Message one");
        logger.info("Message two");
        logger.info("Message three");
        outMock.verifyNoOutput();
        var flushed = logger.flushBufferedMessages();
        var out = outMock.verifyAllOutputs("Message one", "Message two", "Message three");
        assertEquals(flushed, out);
    }

    @Test
    void testLogBufferFail() {
        logger.info("Message one");
        assertThrows(AssertionError.class, () -> logger.flushBufferedMessages());
    }

    @Test
    void testUnbufferedMessages() {
        logger.bufferMessages(true);
        logger.info("Message one");
        logger.info("Message two");
        logger.finishInstance();
        outMock.verifyAllOutputs("FLUSHING THEM NOW...", "Message one", "Message two");
    }

    @Test
    void testLogLevel() {
        logger.info("Message one");
        logger.debug("Message two");
        logger.warn("Message three");
        logger.error("Message four");
        outMock.verifyAllOutputs("Message one", "Message three", "Message four");
    }

    @Test
    void testDiscardedMessages() {
        logger.preserveDiscardedMessages(true);
        logger.info("Message one");
        logger.debug("Message two");
        logger.warn("Message three");
        logger.error("Message four");
        outMock.verifyAllOutputs("Message one", "Message three", "Message four");
        outMock.clear(false);
        var discardedMessages = logger.flushDiscardedMessages();
        assertEquals(discardedMessages, outMock.verifyAllOutputs("Message two"));
    }

    @Test
    void testDiscardedMessagesFail() {
        logger.info("Message one");
        logger.debug("Message two");
        assertThrows(AssertionError.class, () -> logger.flushDiscardedMessages());
    }

    @Test
    void testMaxMessageLength() {
        logger.maxMessageLength = 10;
        logger.info("1234567890abcdefghij");
        outMock.verifyAllOutputs("12345(...)fghij");
    }

    @Test
    void testMaxLineLength() {
        logger.maxLineLength = 5;
        logger.info("1234567890abcdefghij");
        var out = outMock.messages.get(0);
        out = out.substring(out.indexOf("[INFO]"));
        assertArrayEquals(new String[]{"[INFO] 12345", "\t67890", "\tabcde", "\tfghij"}, out.split(System.lineSeparator()));
    }

    @Test
    void testException() {
        try {
            throw new TestAbortedException("First Exception Message");
        } catch (TestAbortedException e) {
            try {
                throw new TestAbortedException("Second Exception Message", e);
            }
            catch (TestAbortedException e2) {
                logger.error(e2, "Expected exception while testing logger");
            }
        }
        var out = outMock.messages.get(0).split(System.lineSeparator());
        assertTrue(out[0].endsWith("[ERROR] Expected exception while testing logger"));
        assertEquals("\tEXCEPTION: org.opentest4j.TestAbortedException: Second Exception Message", out[1]);
        assertEquals("\tCAUSE: org.opentest4j.TestAbortedException: First Exception Message", out[2]);
        assertTrue(Arrays.asList(out).contains("CAUSED BY: org.opentest4j.TestAbortedException: First Exception Message"));
        assertTrue(out.length > 100);
    }

    @Test
    void testExceptionStackTraceLimit() {
        try {
            throw new TestAbortedException("First Exception Message");
        } catch (TestAbortedException e) {
            try {
                throw new TestAbortedException("Second Exception Message", e);
            }
            catch (TestAbortedException e2) {
                logger.error(e2, 10, "Expected exception while testing logger");
            }
        }
        var out = outMock.messages.get(0).split(System.lineSeparator());
        assertTrue(out[0].endsWith("[ERROR] Expected exception while testing logger"));
        assertEquals("\tEXCEPTION: org.opentest4j.TestAbortedException: Second Exception Message", out[1]);
        assertEquals("\tCAUSE: org.opentest4j.TestAbortedException: First Exception Message", out[2]);
        assertTrue(out[4].startsWith("lcm.java.system.logging.BasicLoggerTest.testExceptionStackTraceLimit(BasicLoggerTest.java:"));

        var lines = List.of(out);
        assertEquals(2, lines.stream().filter(s -> s.equals("(...)")).count());
        assertTrue(lines.contains("CAUSED BY: org.opentest4j.TestAbortedException: First Exception Message"));
        assertTrue(out.length > 25 && out.length < 35);
    }

    @Test
    void testInvalidFilePath(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("read-only-file.txt");

        // Create a set of read-only file permissions
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);

        // Create a file with read-only permissions
        FileAttribute<Set<PosixFilePermission>> attrs = 
                PosixFilePermissions.asFileAttribute(permissions);
        Files.createFile(filePath, attrs);

        // This must output an error message, but not raise an exception.
        logger.setFilePath(filePath.toString());

        assertTrue(outMock.messages.size() == 1);
        var errorMsg = outMock.messages.get(0);
        assertTrue(errorMsg.contains("ERROR") && errorMsg.contains(filePath.toString()));
    }

}
