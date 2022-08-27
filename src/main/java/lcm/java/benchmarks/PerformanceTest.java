package lcm.java.benchmarks;

public final class PerformanceTest {
    private PerformanceTest() {}

    public static void main(String[] args) throws Exception {
        new TimeFormatterParseBenchmark(5000000).run();     
    }

}
