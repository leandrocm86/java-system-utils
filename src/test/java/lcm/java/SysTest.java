package lcm.java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import lcm.java.system.Filer;
import lcm.java.system.Sys;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class SysTest {

    private static final String TEST_FILE = "sys.test";
    private static final String LIST_CMD = Sys.isWindows() ? "dir": "ls";
    private static final String DELETE_CMD = (Sys.isWindows() ? "del " : "rm ") + TEST_FILE;

    @BeforeEach
    void createTestFile() {
        Filer.create(TEST_FILE);
    }

    @AfterEach
    void deleteTestFile() {
        Filer.deleteIfExists(TEST_FILE);
    }

    @Test
    void testRead() {
        var out = Sys.read(LIST_CMD);
        assertTrue(out.contains(TEST_FILE));
    }

    @Test
    void testExecOK() {
        Sys.exec(DELETE_CMD);
        assertFalse(Filer.exists(TEST_FILE));
    }

    @Test
    void testExecFail() {
        assertThrows(IllegalStateException.class, () -> {
            Sys.exec(DELETE_CMD);
            Sys.exec(DELETE_CMD);
        });
    }

    @Test
    void testExecAndIgnore() {
        Sys.exec(DELETE_CMD);
        Sys.execAndIgnore(DELETE_CMD);
    }

}
