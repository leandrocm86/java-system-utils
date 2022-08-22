package lcm.java.benchmarks;

public final class PerformanceTest {
    private PerformanceTest() {}

    public static void main(String[] args) throws Exception {
        new FWriteBenchmark(10000).run();
    }
}
