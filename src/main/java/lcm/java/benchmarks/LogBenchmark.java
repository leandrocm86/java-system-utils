package lcm.java.benchmarks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lcm.java.system.F;
import lcm.java.system.Log;

class LogBenchmark extends VoidBenchmark<List<String>> {
    LogBenchmark(int size) {
        super("LogBenchmark", Arrays.stream(Benchmark.generateSequence(size)).map(l -> l.toString()).toList());
    }

    static String BASE_PATH = "C:\\Users\\leand\\Desktop\\";

    void run() throws Exception {
        String appendFile = BASE_PATH + "logAppend.txt";
        String bufferFile = BASE_PATH + "logBuffer.txt";
        F.getOrCreate(appendFile).delete();
        F.getOrCreate(bufferFile).delete();

        runVoidFunction(input -> {
            Log.initFile(appendFile);
            input.forEach(l -> Log.i(l));
        }, "Appending each log message to file");

        Log.initFile(null);

        runVoidFunction(input -> {
            Log.initBuffer();
            input.forEach(l -> Log.i(l));
            Log.flushMessages(bufferFile);
        }, "Flushing buffer to file");

        F.getOrCreate(appendFile).delete();
        F.getOrCreate(bufferFile).delete();

        runVoidFunction(input -> {
            Log.initBuffer();
            input.forEach(l -> Log.i(l));
            Log.flushMessages(bufferFile);
        }, "Flushing buffer to file");

        runVoidFunction(input -> {
            Log.initFile(appendFile);
            input.forEach(l -> Log.i(l));
        }, "Appending each log message to file");

        F file1 = F.get(appendFile);
        F file2 = F.get(bufferFile);

        if (file1.readAsList().size() != super.predefinedInput.size()
            || file2.readAsList().size() != super.predefinedInput.size())
            throw new RuntimeException("Inputs are different from logs");

        if (file1.read().length() != file2.read().length())
            throw new RuntimeException("Files are not equal");
        
        System.out.println("ALL RIGHT!");
    }
}
    