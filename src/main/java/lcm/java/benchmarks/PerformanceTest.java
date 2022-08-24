package lcm.java.benchmarks;

import lcm.java.system.Sys;

public final class PerformanceTest {
    private PerformanceTest() {}

    public static void main(String[] args) throws Exception {
        new SysBenchmark(100, Sys.isWindows() ? "dir": "ls").run();
    }
}
