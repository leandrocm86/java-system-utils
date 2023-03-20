package lcm.java.system;

import java.util.HashMap;
import java.util.stream.Collectors;


/**
 * Helper class to manage a properties/configuration file.
 * Loads each property from a given file and makes their values statically available globally.
 * The file must follow a typical propeties file syntax, with keys and values separated with '=' in each line.
 * The file's syntax is abstracted from the application, and Props saves each property accordingly.
 * However, it's important to note that Props must be initialized before used, even if the file is empty.
 * To initialize Props, the {@link #load(String)} method must be called, tipically when starting the application.
 */
public class Props {
    private static HashMap<String, String> properties;
    private static boolean autoSave = false;
    private static Filer file;

    /**
     * Sets the autosave behavior that writes to file at each changed property.
     * By default, autosave is false, and all modifications are persisted by calling {@link #save()}. 
     */
    public static void setAutoSave(boolean autoSaveValue) {
        autoSave = autoSaveValue;
    }
    
    /**
     * Loads all properties from a file and stores them in memory to be used through the setters and getters.
     * This method should be the first one called from Props, tipically when starting the application.
     * @param filePath - Path of the properties file.
     */
    public static void load(String filePath) {
        file = Filer.get(filePath);
        var lines = file.readAsList();
        properties = new HashMap<String, String>();
        for (var line : lines) {
            if (!line.contains("=")) // Ignores lines without the property delimiter
                continue;
            var pair = line.split("=");
            properties.put(pair[0].trim(), pair[1].trim());
        }
    }

    /**
     * Saves all properties to the file, keeping the proper syntax (key=value). 
     */
    public static void save() {
        if (file == null)
            throw new IllegalStateException("Properties were not loaded!");
        var lines = properties.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
        file.write(lines);
    }
    
    /** 
     * Retrieves a String value for the property with the given key.
     * If no property is found, a given default value is returned.
     * @param key - Key of the property to be retrieved.
     * @param defaultValue - Value to be returned if no property is found with the given key.
     * @return String - Value of the property, or the given default value.
     */
    public static String getString(String key, String defaultValue) {
        if (properties == null)
            throw new IllegalStateException("Properties were not loaded!");
        String value = properties.get(key);
        return value != null ? value : defaultValue;
    }

    /** 
     * Retrieves a String value for the property with the given key.
     * If no property is found, an exception is raised.
     * @param key - Key of the property to be retrieved.
     * @throws IllegalStateException if no property is found with the given key.
     * @return String - Value of the property.
     */
    public static String getString(String key) {
        String value = getString(key, null);
        if (value == null)
            throw new IllegalArgumentException("No property found with key " + key);
        return value;
    }
    
    /**
     * Sets the value of a property.
     * The new value will only be persisted in file if autosave is set to true.
     * If autosave is false (default), {@link #save()} must be explicit called to save the changes in file.
     * @param key - key that identifies the property to be set.
     * @param value - value to be set.
     */
    public static void setString(String key, String value) {
        if (properties == null)
            throw new IllegalStateException("Properties were not loaded!");
        properties.put(key, value);
        if (autoSave)
            save();
    }

    /** 
     * Retrieves an Integer value for the property with the given key.
     * If no property is found, a given default value is returned.
     * @param key - Key of the property to be retrieved.
     * @param defaultValue - Value to be returned if no property is found with the given key.
     * @return Integer - Value of the property, or the given default value.
     */
    public static Integer getInt(String key, Integer defaultValue) {
        String value = getString(key, null);
        return value != null ? Integer.valueOf(value) : defaultValue;
    }

    /** 
     * Retrieves an int value for the property with the given key.
     * If no property is found, an exception is raised.
     * @param key - Key of the property to be retrieved.
     * @throws IllegalStateException if no property is found with the given key.
     * @return String - Value of the property.
     */
    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }
    
    /**
     * Sets the value of an int property.
     * The new value will only be persisted in file if autosave is set to true.
     * If autosave is false (default), {@link #save()} must be explicit called to save the changes in file.
     * @param key - key that identifies the int property to be set.
     * @param value - value to be set.
     */
    public static void setInt(String key, int value) {
        setString(key, String.valueOf(value));
    }

    /** 
     * Retrieves a Double value for the property with the given key.
     * If no property is found, a given default value is returned.
     * @param key - Key of the property to be retrieved.
     * @param defaultValue - Value to be returned if no property is found with the given key.
     * @return Double - Value of the property, or the given default value.
     */
    public static Double getDouble(String key, Double defaultValue) {
        String value = getString(key, null);
        return value != null ? Double.valueOf(value) : defaultValue;
    }

    /** 
     * Retrieves a double value for the property with the given key.
     * If no property is found, an exception is raised.
     * @param key - Key of the property to be retrieved.
     * @throws IllegalStateException if no property is found with the given key.
     * @return double - Value of the property.
     */
    public static double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }
    
    /**
     * Sets the value of a double property.
     * The new value will only be persisted in file if autosave is set to true.
     * If autosave is false (default), {@link #save()} must be explicit called to save the changes in file.
     * @param key - key that identifies the double property to be set.
     * @param value - value to be set.
     */
    public static void setDouble(String key, double value) {
        setString(key, String.valueOf(value));
    }

    /** 
     * Retrieves a Boolean value for the property with the given key.
     * If no property is found, a given default value is returned.
     * @param key - Key of the property to be retrieved.
     * @param defaultValue - Value to be returned if no property is found with the given key.
     * @return Boolean - Value of the property, or the given default value.
     */
    public static Boolean getBoolean(String key, Boolean defaultValue) {
        String value = getString(key, null);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    /** 
     * Retrieves a boolean value for the property with the given key.
     * If no property is found, an exception is raised.
     * @param key - Key of the property to be retrieved.
     * @throws IllegalStateException if no property is found with the given key.
     * @return boolean - Value of the property.
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }
    
    /**
     * Sets the value of a boolean property.
     * The new value will only be persisted in file if autosave is set to true.
     * If autosave is false (default), {@link #save()} must be explicit called to save the changes in file.
     * @param key - key that identifies the boolean property to be set.
     * @param value - value to be set.
     */
    public static void setBoolean(String key, boolean value) {
        setString(key, String.valueOf(value));
    }

}
