package lcm.java.system;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * A wrapper for java.io.File and java.nio.file.Files to facilitate read/write operations.
 */
public class Filer {

    private static Charset charset = Charset.forName("UTF-8");

    private File file;
    private Path filePath;

    public Filer(String path) {
        this.file = new File(path);
        this.filePath = file.toPath();
    }

    /**
     * Writes the given string to the file, overwriting any existing content.
     * @param content - Content to be written.
     */
    public void write(String content) {
        try {
            Files.writeString(filePath, content, charset, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't write on file " + filePath, e);
        }
    }

    /**
     * Writes all elements from the given iterable to the file, overwriting any existing content.
     * @param content - Content to be written.
     */
    public void write(Iterable<? extends CharSequence> content) {
        try {
            Files.write(filePath, content, charset, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't write on file " + filePath, e);
        }
    }

    /**
     * Appends the given string to the end of the file.
     * @param content - Contend to be appended.
     */
    public void append(String content) {
        try {
            Files.writeString(filePath, content, charset, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't append on file " + filePath, e);
        }
    }

    /**
     * Appends the given string to the end of the file, adding a line break at the end.
     * @param content - Content to be appended.
     */
    public void appendLn(String content) {
        append(content + System.lineSeparator());
    }

    /**
     * Appends all elements from the given iterable to the file.
     * @param content - Content to be appended.
     */
    public void append(Iterable<? extends CharSequence> content) {
        try {
            Files.write(filePath, content, charset, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't append on file " + filePath, e);
        }
    }

    /**
     * Reads the whole content of the file as a single string.
     * @return String
     */
    public String read() {
        try {
            return Files.readString(filePath, charset);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read from file " + filePath, e);
        }
    }

    /**
     * Reads the file as a list containing each line as a string.
     * @return List&lt;String&gt;
     */
    public List<String> readAsList() {
        try {
            return Files.readAllLines(filePath, charset);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read from file " + filePath, e);
        }
    }

    /**
     * Deletes the file phisically on the system.
     */
    public void delete() {
        if (!file.delete())
            throw new IllegalStateException("Couldn't delete file " + filePath);
    }

    /**
     * Checks if the file exists.
     * @return boolean - True if the file exists.
     */
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * Checks if a file exists on the given path.
     * @param path - File's location path.
     * @return boolean - True if the file exists.
     */
    public static boolean exists(String path) {
        return new File(path).exists();
    }

    /**
     * Determines if it's possible to write on the file, wether creating it or modifying it.
     * @return true if the file exists and is writable or if the parent directory is writable, false otherwise
     */
    public boolean canCreateOrModify() {
        if (this.file.exists())
            return this.file.canWrite();
        else {
            File parentDirectory = this.file.getParentFile();
            if (parentDirectory == null)
                parentDirectory = new File(System.getProperty("user.dir"));
            return parentDirectory.canWrite();
        }
    }

    /**
     * Determines if it's possible to write on a file on the given path, wether creating it or modifying it.
     * @param  path	the path to the file to check
     * @return true if the file exists and is writable or if the parent directory is writable, false otherwise
     */
    public static boolean canCreateOrModify(String path) {
        return new Filer(path).canCreateOrModify();
    }

    /**
     * Checks if it is possible to write on the existing file.
     * @return boolean - True if the file both exists and is writable.
     */
    public boolean canModify() {
        return this.file.canWrite();
    }

    /**
     * Checks if it is possible to write on an existing file on the given path.
     * @param path - File's location path.
     * @return boolean - True if the file both exists and is writable.
     */
    public static boolean canModify(String path) {
        return new File(path).canWrite();
    }

    /**
     * Deletes the file on the given path, if it exists.
     * @param path - File's location path.
     */
    public static void deleteIfExists(String path) {
        File file = new File(path);
        if (file.exists())
            file.delete();
    }

    /**
     * Opens a file on the given path or throws an exception if the file doesn't exist.
     * @param path - File's location path.
     * @return Filer - Instance of Filer encapsulating the file.
     * @throws IllegalArgumentException if the file doesn't exist.
     */
    public static Filer get(String path) {
        Filer f = new Filer(path);
        if (!f.file.exists())
            throw new IllegalArgumentException("File " + path + " doesn't exist!");
        return f;
    }

    /**
     * Creates the file on the given path or throws an exception if the file already exists.
     * @throws IllegalArgumentException if the file already exists.
     */
    public void create() {
        if (this.file.exists())
            throw new IllegalArgumentException("File " + this.filePath + " already exists!");
        try {
            this.file.createNewFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't create a file with the given path " + this.filePath, e);
        }
    }

    /**
     * Creates a file on the given path or throws an exception if the file already exists.
     * @param path - File's location path.
     * @return Filer - Instance of Filer encapsulating the file.
     * @throws IllegalArgumentException if the file already exists.
     */
    public static Filer create(String path) {
        Filer f = new Filer(path);
        if (f.file.exists())
            throw new IllegalArgumentException("File " + path + " already exists!");
        try {
            f.file.createNewFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't create a file with the given path " + path, e);
        }
        return f;
    }

    /**
     * Opens a file on the given path, creating it if it doesn't exist.
     * @param path - File's location path.
     * @return Filer - Instance of Filer encapsulating the file.
     */
    public static Filer getForWriting(String path) {
        Filer f = new Filer(path);
        if (!f.file.exists()) {
            try {
                if (f.file.createNewFile())
                    return f;
                else
                    throw new IllegalArgumentException("Couldn't create a file with the given path " + path);
            } catch (IOException e) {
                throw new IllegalArgumentException("Couldn't create a file with the given path " + path, e);
            }
        }
        else if (!f.file.canWrite()) {
            throw new IllegalArgumentException("File exists on the given path, but it's not writable: " + path);
        }
        return f;
    }

    public File getFile() {
        return file;
    }

    public Path getFilePath() {
        return filePath;
    }
}
