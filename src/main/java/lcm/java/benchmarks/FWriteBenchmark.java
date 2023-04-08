package lcm.java.benchmarks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lcm.java.system.Filer;

class FWriteBenchmark extends VoidBenchmark<List<String>> {
    FWriteBenchmark(int size) {
        super("FBenchmark", Arrays.stream(Benchmark.generateNanos(size)).map(l -> l.toString()).toList());
    }

    static String BASE_PATH = "C:\\Users\\leand\\Desktop\\";
    static String LN = System.lineSeparator();

    void run() throws Exception {
        File listFile = new File(BASE_PATH + "list.txt");
        Filer flist = Filer.getOrCreate(BASE_PATH + "flist.txt");
        File stringFile = new File(BASE_PATH + "string.txt");
        Filer fstring = Filer.getOrCreate(BASE_PATH + "fstring.txt");
        File openAndAppendFile = new File(BASE_PATH + "append.txt");
        Filer fappend = Filer.getOrCreate(BASE_PATH + "fappend.txt");
        File appendFile = new File(BASE_PATH + "append.txt");

        if (listFile.exists())
            listFile.delete();

        if (stringFile.exists())
            stringFile.delete();
        
        if (openAndAppendFile.exists())
            openAndAppendFile.delete();

        if (appendFile.exists())
            appendFile.delete();

        flist.delete(); fstring.delete(); fappend.delete();
        
        runVoidFunction(input -> {
            Filer.create(BASE_PATH + "fstring.txt").write(input.stream().collect(Collectors.joining(LN)));
        }, "Writing string with F");

        runVoidFunction(input -> {
            Files.writeString(stringFile.toPath(), input.stream().collect(Collectors.joining(LN)), StandardOpenOption.CREATE);
        }, "Writing string");

        runVoidFunction(input -> {
            Filer f = Filer.create(BASE_PATH + "flist.txt");
            f.write(input);
        }, "Writing list with F");

        runVoidFunction(input -> {
            Files.write(listFile.toPath(), input, StandardOpenOption.CREATE);
        }, "Writing list");

        runVoidFunction(input -> {
            Filer f = Filer.getOrCreate(BASE_PATH + "fappend.txt");
            for (String l : input)
                f.appendLn(l);
        }, "Appending strings with appendLn");

        runVoidFunction(input -> {
            openAndAppendFile.createNewFile();
            for (String l : input) {
                Files.writeString(openAndAppendFile.toPath(), l + LN, StandardOpenOption.APPEND);
            }
        }, "Manually appending strings after creating file");

        String file1 = Files.readString(listFile.toPath()).trim();
        String file2 = Files.readString(stringFile.toPath()).trim();
        String file3 = Files.readString(openAndAppendFile.toPath()).trim();
        String file4 = Files.readString(appendFile.toPath()).trim();
        String file5 = flist.read().trim();
        String file6 = fstring.read().trim();
        String file7 = fappend.read().trim();

        if (!file1.equals(file2) || !file1.equals(file3) || !file1.equals(file4) || !file1.equals(file5) || !file1.equals(file6) || !file1.equals(file7))
            throw new RuntimeException("Files are not equal");
        
        System.out.println("ALL RIGHT!");
    }
}
    