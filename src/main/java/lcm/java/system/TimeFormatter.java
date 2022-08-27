package lcm.java.system;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Helper class for making conversion easier between LocalDateTime, String and Epoch.
 * Always assumes the system's timezone.
 * Conversions between LocalDateTime and Epoch can be made statically,
 * whereas conversions from/to String require instantiation with a date pattern.
 */
public class TimeFormatter {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final DateTimeFormatter formatter;

    public TimeFormatter(String dateTimePattern) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    /** 
     * Retrieves a LocalDateTime (at the system's timezone) for the given date in String.
     * The parsing is made with the pattern supplied for the constructor.
     */
    public LocalDateTime stringToLocal(String date) {
        return formatter.parse(date, LocalDateTime::from);
    }

    /** 
     * Retrieves an Epoch timestamp in millis for the given date in String (at the system's timezone).
     * The parsing is made with the pattern supplied for the constructor.
     */
    public long stringToMillis(String date) {
        return localToMillis(stringToLocal(date));
    }
    
    /** 
     * Retrieves an Epoch timestamp in seconds for the given date in String (at the system's timezone).
     * The parsing is made with the pattern supplied for the constructor.
     */
    public long stringToSeconds(String date) {
        return localToSeconds(stringToLocal(date));
    }

    /** 
     * Retrieves a date in String for the given LocalDateTime (at the system's timezone).
     * Formatting is made with the pattern supplied for the constructor.
     */
    public String localToString(LocalDateTime date) {
        return formatter.format(date);
    }

    /** 
     * Retrieves a date in String (at the system's timezone) for the given Epoch timestamp in millis.
     * Formatting is made with the pattern supplied for the constructor.
     */
    public String millisToString(long millis) {
        return localToString(millisToLocal(millis));
    }
    
    /** 
     * Retrieves a date in String (at the system's timezone) for the given Epoch timestamp in seconds.
     * Formatting is made with the pattern supplied for the constructor.
     */
    public String secondsToString(long seconds) {
        return localToString(secondsToLocal(seconds));
    }
    
    /** 
     * Retrieves a LocalDateTime (at the system's timezone) for the given Epoch timestamp in millis.
     */
    public static LocalDateTime millisToLocal(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZONE);
    }
    
    /** 
     * Retrieves a LocalDateTime (at the system's timezone) for the given Epoch timestamp in seconds.
     */
    public static LocalDateTime secondsToLocal(long seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZONE);
    }

    /** 
     * Retrieves the Epoch timestamp in millis for the given LocalDateTime (at the system's timezone).
     */
    public static long localToMillis(LocalDateTime date) {
        return date.atZone(ZONE).toInstant().toEpochMilli();
    }
    
    /** 
     * Retrieves the Epoch timestamp in seconds for the given LocalDateTime (at the system's timezone).
     */
    public static long localToSeconds(LocalDateTime date) {
        return date.atZone(ZONE).toEpochSecond();
    }

}
