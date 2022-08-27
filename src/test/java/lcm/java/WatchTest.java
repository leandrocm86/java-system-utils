package lcm.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.chrono.ThaiBuddhistEra;

import org.junit.jupiter.api.Test;

import lcm.java.system.Watch;

public class WatchTest {

    @Test
    void testWatches() throws InterruptedException {
        Watch.get("Total");
        Thread.sleep(50);
        for (int i = 0; i < 10; i++) {
            Watch.get("Loops").restartCheck();
            Thread.sleep(10);
            if (i % 2 == 0) {
                Watch.get("Pairs").restartCheck();
                Thread.sleep(20);
                Watch.get("Pairs").check();
            }
            Watch.get("Loops").check();
        }
        Watch.get("Total").check();

        long total = Watch.get("Total").getTotalTime();
        long loops = Watch.get("Loops").getTotalTime();
        long pairs = Watch.get("Pairs").getTotalTime();
        assertTrue(total >= 250);
        assertTrue(loops >= 200 && loops < total);
        assertTrue(pairs >= 100 && pairs < loops);
    }
    
}
