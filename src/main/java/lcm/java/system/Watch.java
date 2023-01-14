package lcm.java.system;

import java.util.HashMap;

/** 
 * Utility class to measure time passed between two moments.
 * It makes it easier to make benchmarks in different application parts simultaneously.
 */
public class Watch {

    private long lastTimestamp;
    private long totalTime;

    private static HashMap<String, Watch> watchesByName;
    
    /** 
     * Instantiates a new watch and initializes it with the current time.
     * If you wish to share this watch across different methods or classes,
     * consider using the static {@link #get(String)} method instead of directly creating a new one.
     */
    public Watch() {
        lastTimestamp = System.currentTimeMillis();
    }

    /** 
     * Retrieves an instance of Watch associated with the given name.
     * This instance is created when it's called for the first time, and the same is returned at each call.
     * @param watchName - Name of the clock to retrieve.
     */
    public static Watch get(String watchName) {
        if (watchesByName == null) {
            watchesByName = new HashMap<String, Watch>();
        }
        Watch c = watchesByName.get(watchName);
        if (c == null) {
            c = new Watch();
            watchesByName.put(watchName, c);
        }
        return c;
    }

    /** 
     * Retrieves all watches associated with a name (watches created through the {@link #get(String)} method).
     * @return HashMap&lt;String, Watch&gt; - Map containing each clock by its name.
     */
    public static HashMap<String, Watch> getAllWatches() {
        return watchesByName;
    }

    /** 
     * Counts the time passed since the previous check (or the clock's creation, if this is the first check).
     * This time will be added to the total time counter.
     * @return long - difference between this check and the last one.
     */
    public long check() {
        long previousTimestamp = lastTimestamp;
        lastTimestamp = System.currentTimeMillis();
        totalTime += lastTimestamp - previousTimestamp;
        return lastTimestamp - previousTimestamp;
    }
    
    /** 
     * Returns the total time accumulated between each check.
     */
    public long getTotalTime() {
        return totalTime;
    }

    /** 
     * Restarts the counting for the next check.
     * This doesn't reset the total (accumulated) time already counted (for that, use {@link #reset()}).
     */
    public void restartCheck() {
        lastTimestamp = System.currentTimeMillis();
    }

    /** 
     * Resets the last timestamp and the total time accumulated.
     * For restarting only the last timestamp, use {@link #restartCheck()}).
     */
    public void reset() {
        lastTimestamp = System.currentTimeMillis();
        totalTime = 0;
    }

}
