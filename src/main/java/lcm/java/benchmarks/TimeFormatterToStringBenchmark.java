package lcm.java.benchmarks;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lcm.java.system.TimeFormatter;

public class TimeFormatterToStringBenchmark extends FunctionBenchmark<List<Long>, List<String>> {
    
    public TimeFormatterToStringBenchmark(int n) {
        super("TimeFormatterTOSTRING", generateDates(n));
    }

    static List<Long> generateDates(int n) {
        var dates = new ArrayList<Long>();
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            timestamp += 1000;
            dates.add(timestamp);
        }
        return dates;
    }

    public void run() {
        runFunction(dates -> {
            var strings = new ArrayList<String>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
            for (var date : dates) {
                strings.add(dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())));
            }
            return strings;
        }, "Native DateTimeFormatter");
        runFunction(dates -> {
            var strings = new ArrayList<String>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
            for (var date : dates) {
                strings.add(dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())));
            }
            return strings;
        }, "Native DateTimeFormatter");
        // runFunction(dates -> {
        //     var strings = new ArrayList<String>();
        //     SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm:ss");
        //     for (var date : dates) {
        //         strings.add(sdf.format(date));
        //     }
        //     return strings;
        // }, "Native SimpleDateFormatter");
        runFunction(dates -> {
            var strings = new ArrayList<String>();
            TimeFormatter tf = new TimeFormatter("dd-MMM HH:mm:ss");
            for (var date : dates) {
                strings.add(tf.millisToString(date));
            }
            return strings;
        }, "TimeFormatter");
        runFunction(dates -> {
            var strings = new ArrayList<String>();
            TimeFormatter tf = new TimeFormatter("dd-MMM HH:mm:ss");
            for (var date : dates) {
                strings.add(tf.millisToString(date));
            }
            return strings;
        }, "TimeFormatter");
        runFunction(dates -> {
            var strings = new ArrayList<String>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
            for (var date : dates) {
                strings.add(dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())));
            }
            return strings;
        }, "Native DateTimeFormatter");
    }
}
