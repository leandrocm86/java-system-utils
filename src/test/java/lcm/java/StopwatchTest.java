package lcm.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import lcm.java.system.Stopwatch;

public class StopwatchTest {

    @Test
    void testWatches() throws InterruptedException {
        Stopwatch.get("Total");
        Thread.sleep(50);
        for (int i = 0; i < 10; i++) {
            Stopwatch.get("Loops").restartCheck();
            Thread.sleep(10);
            if (i % 2 == 0) {
                Stopwatch.get("Pairs").restartCheck();
                Thread.sleep(20);
                Stopwatch.get("Pairs").check();
            }
            Stopwatch.get("Loops").check();
        }
        Stopwatch.get("Total").check();

        long total = Stopwatch.get("Total").getTotalTime();
        long loops = Stopwatch.get("Loops").getTotalTime();
        long pairs = Stopwatch.get("Pairs").getTotalTime();
        assertTrue(total >= 250);
        assertTrue(loops >= 200 && loops < total);
        assertTrue(pairs >= 100 && pairs < loops);
    }
    
}
