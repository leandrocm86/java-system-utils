package lcm.java.system;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;

/**
 * Class with general system utilities.
 */
public class Sys {

    /**
     * Retrieves the current system path.
     */
    public static String getSystemPath() {
		try {
			CodeSource source = Sys.class.getProtectionDomain().getCodeSource();
			File jarFile = new File(source.getLocation().toURI().getPath());
			String path = jarFile.getParentFile().getPath() + "/";
			return URLDecoder.decode(path, "UTF-8");
		} catch (Throwable t) {
			throw new IllegalStateException("Error while trying to retrieve the current system path.", t);
		}
	}

    /**
     * Detects wether the program is running on a Windows system.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String[] prepareCommand(String command) {
        return isWindows() ? new String[]{"CMD", "/C", command} : new String[]{"/bin/sh", "-c", command};
    }

    /**
     * Executes a command on system and returns its output.
     * If no output is expected, consider using {@link #exec(String...)}
     * @param command - command to be executed on the system.
     * @return output of the command's execution.
     */
    public static String read(String command) {
        ProcessBuilder builder = new ProcessBuilder(prepareCommand(command));
        builder.redirectErrorStream(true);
        Process p;
        try {
            p = builder.start();
        }
        catch(IOException e) {
            throw new IllegalStateException("Failed to execute commands on system.", e);
        }
        try {
            return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        catch(IOException e) {
            throw new IllegalStateException("Failed to read command output from system.", e);
        }
    }

    /**
     * Executes command on system and ensures there is no output.
     * @param command - command to be executed on the system.
     * @throws IllegalStateException if the command generate any output.
     */
    public static void exec(String command) {
        String output = read(command);
        if (output.length() > 0)
            throw new IllegalStateException("Unexpected output given for command executed on system: " + output);
    }

    /**
     * Executes command on system and ignores any output.
     * @param command - command to be executed on the system.
     */
    public static void execAndIgnore(String command) {
        try {
            new ProcessBuilder(prepareCommand(command)).start();
        }
        catch(IOException e) {
            throw new IllegalStateException("Failed to execute command on system.", e);
        }
    }

    /**
     * Executes (opens) file on system.
     * @param filePath - path of the file to be executed on the system.
     */
    public static void openFile(String filePath) {
		try {
			java.awt.Desktop.getDesktop().open(new File(filePath));
		} catch (IOException e) {
			throw new IllegalArgumentException("Couldn't execute/open file " + filePath, e);
		}
	}

    /**
     * Get current size of heap in bytes.
     */
    public static long getHeapBytes() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Get maximum size of heap in bytes. The heap cannot grow beyond this size.
     * Any attempt will result in an OutOfMemoryException.
     */
    public static long getMaxHeapBytes() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Get amount of free memory within the heap in bytes.
     * This size will increase after garbage collection and decrease as new objects are created.
     */
    public static long getFreeHeapBytes() {
        return Runtime.getRuntime().freeMemory();
    }

}
