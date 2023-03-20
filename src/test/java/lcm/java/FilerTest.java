package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import lcm.java.system.Filer;

class FilerTest {

    static final String TEST_FILE = "ftest.txt";

    void makeTestFile() {
        try {
            new File(TEST_FILE).createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create test file", e);
        }
    }

    @AfterEach
    void deleteTestFile() {
        File testFile = new File(TEST_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testGetOK() {
        makeTestFile();
        Filer f = Filer.get(TEST_FILE);
        assertTrue(f != null);
    }

    @Test
    void testGetFail() {
        assertThrows(IllegalArgumentException.class, () -> Filer.get(TEST_FILE));
    }

    @Test
    void testCreateOK() {
        Filer f = Filer.create(TEST_FILE);
        assertTrue(f != null);
    }

    @Test
    void testCreateFail() {
        makeTestFile();
        assertThrows(IllegalArgumentException.class, () -> Filer.create(TEST_FILE));
    }

    @Test
    void testWriteRead() {
        makeTestFile();
        Filer f = Filer.get(TEST_FILE);
        f.write("Hello World");
        assertEquals("Hello World", f.read());
        f.write("Hello Galaxy");
        assertEquals("Hello Galaxy", f.read());
    }

    @Test
    void testAppend() {
        Filer f = Filer.getOrCreate(TEST_FILE);
        f.appendLn("Hello World");
        assertEquals("Hello World", f.read().trim());
        f.append("Hello Galaxy");
        var lines = f.readAsList();
        assertEquals("Hello World", lines.get(0));
        assertEquals("Hello Galaxy", lines.get(1));
    }

}
