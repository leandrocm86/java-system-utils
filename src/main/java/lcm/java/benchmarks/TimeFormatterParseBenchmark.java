package lcm.java.benchmarks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lcm.java.system.TimeFormatter;

public class TimeFormatterParseBenchmark extends FunctionBenchmark<List<String>, List<Long>> {
    
    public TimeFormatterParseBenchmark(int n) {
        super("TimeFormatterPARSE", generateDateStrings(n));
    }

    static final String PATTERN = "dd-MMM-yyyy HH:mm:ss.SSS";

    static List<Long> generateDates(int n) {
        var dates = new ArrayList<Long>();
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            timestamp += 1000;
            dates.add(timestamp);
        }
        return dates;
    }

    private static List<String> generateDateStrings(int n) {
        var strings = new ArrayList<String>();
        var formatter = DateTimeFormatter.ofPattern(PATTERN);
        var dates = generateDates(n);
        for (var millis : dates) {
            strings.add(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())));
        }
        return strings;
    }

    public void run() {
        runFunction(strings -> {
            var millis = new ArrayList<Long>();
            TimeFormatter tf = new TimeFormatter(PATTERN);
            for (var string : strings) {
                millis.add(tf.stringToMillis(string));
            }
            return millis;
        }, "TimeFormatter");
        // runFunction(strings -> {
        //     var millis = new ArrayList<Long>();
        //     SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        //     for (var string : strings) {
        //         try {
        //             millis.add(sdf.parse(string).getTime());
        //         } catch (ParseException e) {
        //             throw new IllegalStateException(e);
        //         }
        //     }
        //     return millis;
        // }, "Native SimpleDateFormat");
        runFunction(strings -> {
            var millis = new ArrayList<Long>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(PATTERN);
            for (var string : strings) {
                millis.add(dtf.parse(string, LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
            return millis;
        }, "Native DateTimeFormatter");
        runFunction(strings -> {
            var millis = new ArrayList<Long>();
            TimeFormatter tf = new TimeFormatter(PATTERN);
            for (var string : strings) {
                millis.add(tf.stringToMillis(string));
            }
            return millis;
        }, "TimeFormatter");
        runFunction(strings -> {
            var millis = new ArrayList<Long>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(PATTERN);
            for (var string : strings) {
                millis.add(dtf.parse(string, LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
            return millis;
        }, "Native DateTimeFormatter");
    }
}
