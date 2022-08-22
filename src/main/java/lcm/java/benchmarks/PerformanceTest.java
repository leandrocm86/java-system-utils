package lcm.java.benchmarks;

public final class PerformanceTest {
    private PerformanceTest() {}

    public static void main(String[] args) throws Exception {
        new LogBenchmark(10000).run();
    }
}
