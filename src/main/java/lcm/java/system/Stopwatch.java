package lcm.java.system;

import java.util.HashMap;
import java.util.Map;

/** 
 * Utility class to measure time passed between two moments.
 * It makes it easier to make benchmarks in different application parts simultaneously.
 */
public class Stopwatch {

    private long lastTimestamp;
    private long totalTime;
    private String name;
    private int checkCount = 0;

    private static HashMap<String, Stopwatch> watchesByName = new HashMap<>();
    
    /** 
     * Instantiates a new watch and initializes it with the current time.
     * If you wish to share this watch across different methods or classes,
     * consider using the static {@link #get(String)} method instead of directly creating a new one.
     */
    public Stopwatch() {
        lastTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Instantiates a clock with a name, but don't associate it with the global static map.
     * This is for other utility classes on the same package and would be dangerous to be used publicly,
     * because it would be possible to instantiate 2 clocks with the same name and make inconsistencies.
     * 
     * @param name - Name of the clock. This should be a unique identifier.
     */
    protected Stopwatch(String name) {
    	this();
		this.name = name;
    }

    /** 
     * Retrieves an instance of Watch associated with the given name.
     * This instance is created when it's called for the first time, and the same is returned at each call.
     * @param watchName - Name of the clock to retrieve.
     */
    public static Stopwatch get(String watchName) {
        return watchesByName.computeIfAbsent(watchName, w -> new Stopwatch(watchName));
    }

    /** 
     * Retrieves all watches associated with a name (watches created through the {@link #get(String)} method).
     * @return HashMap&lt;String, Watch&gt; - Map containing each clock by its name.
     */
    public static Map<String, Stopwatch> getAllWatches() {
        return watchesByName;
    }

    /** 
     * Counts the time passed since the previous check (or the clock's creation, if this is the first check).
     * This time will be added to the total time counter.
     * @return long - difference between this check and the last one.
     */
    public long check() {
    	checkCount++;
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
     * Returns the number of times the check method was called.
     */
    public int getCheckCount() {
		return checkCount;
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
    
    public String getName() {
    	return name;
    }
    
    @Override
    public String toString() {
    	return name + ": " + totalTime + "ms (calls: " + checkCount + ")";
    }

}
