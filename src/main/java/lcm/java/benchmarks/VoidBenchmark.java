package lcm.java.benchmarks;

class VoidBenchmark<I> {

    interface VoidFunction<I> {
        void apply(I input) throws Exception;
    }

    String name;
    I predefinedInput;
    I lastInput = null;

    VoidBenchmark(String name) {
        this.name = name;
    }

    VoidBenchmark(String name, I predefinedInput) {
        this.name = name;
        this.predefinedInput = predefinedInput;
    }

    void runVoidFunction(I input, VoidFunction<I> voidFunction, String title) throws Exception {
        Benchmark.printSeparator();
        System.gc();
        long start = System.currentTimeMillis();
        voidFunction.apply(input);
        System.out.println(String.format("[%s] Time for %s: %dms", name, title, System.currentTimeMillis() - start));
        System.out.println("Input: " + printInput(input));
        addInput(input);
    }

    void runVoidFunction(VoidFunction<I> voidFunction, String title) throws Exception {
        runVoidFunction(predefinedInput, voidFunction, title);
    }

    String printInput(I i) {
        return Benchmark.printObject(i);
    }

    void addInput(I input) {
        if (lastInput != null)
            if (!equalInputs(lastInput, input)) {
                System.out.println("===== ERROR: INPUTS WERE DIFFERENT AFTER FUNCTION! =====");
                System.out.println("PREVIOUS: " + printInput(lastInput));
                System.out.println("NOW: " + printInput(input));
                throw new AssertionError("Input didn't end like the previous!");
            }
        lastInput = input;
    }

    boolean equalInputs(I i1, I i2) {
        return Benchmark.equalObjects(i1, i2);
    }

}
