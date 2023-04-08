package lcm.java.benchmarks;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import lcm.java.system.Filer;
import lcm.java.system.logging.OLog;

class LogBenchmark extends VoidBenchmark<List<String>> {
    LogBenchmark(int size) {
        super("LogBenchmark", Arrays.stream(Benchmark.generateSequence(size)).map(l -> l.toString()).toList());
    }

    static String BASE_PATH = "/home/lcm/temp/";

    void run() throws Exception {
        OLog.setDateTimeFormat(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String appendFile = BASE_PATH + "logAppend.txt";
        String bufferFile = BASE_PATH + "logBuffer.txt";
        Filer.deleteIfExists(appendFile);
        Filer.deleteIfExists(bufferFile);

        runVoidFunction(input -> {
            OLog.bufferMessages(false);
            OLog.setFilePath(appendFile);
            input.forEach(l -> OLog.info(l));
        }, "Appending each log message to file");

        OLog.setFilePath(null);

        runVoidFunction(input -> {
            OLog.bufferMessages(true);
            OLog.setFilePath(bufferFile);
            input.forEach(l -> OLog.info(l));
            OLog.flushBufferedMessages();
        }, "Flushing buffer to file");

        Filer.deleteIfExists(appendFile);
        Filer.deleteIfExists(bufferFile);

        runVoidFunction(input -> {
            OLog.bufferMessages(true);
            OLog.setFilePath(bufferFile);
            input.forEach(l -> OLog.info(l));
            OLog.flushBufferedMessages();
        }, "Flushing buffer to file");

        runVoidFunction(input -> {
            OLog.bufferMessages(false);
            OLog.setFilePath(appendFile);
            input.forEach(l -> OLog.info(l));
        }, "Appending each log message to file");

        Filer file1 = Filer.get(appendFile);
        Filer file2 = Filer.get(bufferFile);

        if (file1.readAsList().size() != super.predefinedInput.size()
            || file2.readAsList().size() != super.predefinedInput.size())
            throw new RuntimeException("Inputs are different from logs");

        if (!file1.read().equals(file2.read()))
            throw new RuntimeException("Files are not equal");
        
        System.out.println("ALL RIGHT!");
    }
}
    