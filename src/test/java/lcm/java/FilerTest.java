package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        f.write("Hello Universes!");
        assertEquals("Hello Universes!", f.read());
        f.write("Hello Galaxy!");
        assertEquals("Hello Galaxy!", f.read());
        assertEquals("Hello Galaxy!", Filer.get(TEST_FILE).read());
        Filer.getForWriting(TEST_FILE).write("Hello World");
        assertEquals("Hello World", f.read());
        assertEquals("Hello World", Filer.getForWriting(TEST_FILE).read());
        Filer.get(TEST_FILE).write("Hi!");
        assertEquals("Hi!", Filer.get(TEST_FILE).read());
        assertEquals("Hi!", f.read());
    }

    @Test
    void testAppend() {
        Filer f = Filer.getForWriting(TEST_FILE);
        f.appendLn("Hello World");
        assertEquals("Hello World", f.read().trim());
        f.append("Hello Galaxy");
        var lines = f.readAsList();
        assertEquals("Hello World", lines.get(0));
        assertEquals("Hello Galaxy", lines.get(1));
    }

    @Test
    void testWriteToReadOnlyFile(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("read-only-file.txt");
        Path folderPath = tempDir.resolve("read-only-folder");

        // Create a set of read-only file permissions
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.OTHERS_READ);

        // Create a file with read-only permissions
        FileAttribute<Set<PosixFilePermission>> attrs = 
                PosixFilePermissions.asFileAttribute(permissions);
        Files.createFile(filePath, attrs);
        Files.createDirectory(folderPath, attrs);

        assertThrows(IOException.class, () -> Files.writeString(filePath, "test"));
        assertThrows(IOException.class, () -> Files.createFile(folderPath.resolve("test.txt")));
        assertTrue(Filer.canCreateOrModify(TEST_FILE));
        Filer.create(TEST_FILE);
        assertTrue(Filer.canCreateOrModify(TEST_FILE));
        assertTrue(Filer.canModify(TEST_FILE));
        assertFalse(Filer.canCreateOrModify(filePath.toAbsolutePath().toString()));
        assertFalse(Filer.canCreateOrModify(folderPath.resolve("test.txt").toAbsolutePath().toString()));
    }
}
