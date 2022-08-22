package lcm.java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import lcm.java.system.F;
import lcm.java.system.Log;

class LogTest {

    static final String TEST_FILE = "test.log";

    @AfterEach
    void deleteTestFile() {
        File testFile = new File(TEST_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
        Log.initFile(null);
    }

    @Test
    void testLogToFile() {
        Log.initFile(TEST_FILE);
        Log.d("Message one");
        Log.d("Message two");
        Log.d("Message three");
        var msgs = F.get(TEST_FILE).readAsList();
        assertTrue(msgs.get(0).endsWith("one"));
        assertTrue(msgs.get(1).endsWith("two"));
        assertTrue(msgs.get(2).endsWith("three"));
    }

    @Test
    void testLogBuffer() {
        Log.initBuffer();
        Log.d("Message one");
        Log.d("Message two");
        Log.d("Message three");
        Log.flushMessages(TEST_FILE, 2);
        var msgs = F.get(TEST_FILE).readAsList();
        assertTrue(msgs.get(0).endsWith("two"));
        assertTrue(msgs.get(1).endsWith("three"));
    }

    @Test
    void testLogBufferFail() {
        Log.d("Message one");
        assertThrows(IllegalStateException.class, () -> Log.flushMessages(TEST_FILE));
    }






}
