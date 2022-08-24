package lcm.java.benchmarks;

import lcm.java.system.Sys;

class SysBenchmark extends VoidBenchmark<String> {
    int executions;

    SysBenchmark(int executions, String command) {
        super("SysBenchmark", command);
        this.executions = executions;
    }

    void run() throws Exception {

        runVoidFunction(input -> {
            for (int i = 0; i < executions; i++)
                Sys.read(input);
        }, "Executing command and waiting for output");

        runVoidFunction(input -> {
            for (int i = 0; i < executions; i++)
                Sys.execAndIgnore(input);
        }, "Executing command and ignoring output");

        runVoidFunction(input -> {
            for (int i = 0; i < executions; i++)
                Sys.read(input);
        }, "Executing command and waiting for output");

        runVoidFunction(input -> {
            for (int i = 0; i < executions; i++)
                Sys.execAndIgnore(input);
        }, "Executing command and ignoring output");
    }
}
    