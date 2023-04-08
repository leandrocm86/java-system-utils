package lcm.java;

import lcm.java.system.Filer;
import lcm.java.system.logging.OLog;
import lcm.java.system.logging.TLog;

import java.util.ArrayList;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        testTLogParallelWriting(100, 1000);
    }
    
    private static void testOLogUnbufferedMessages() {
        OLog.bufferMessages(true);
        OLog.setPrintStream(System.out);
        OLog.setCustomHeader("[OLog unbuffered test] ");
        OLog.info("Message test 1");
        OLog.info("Message test 2");
    }

    private static void testTLogParallelWriting(int numberOfThreads, int numberOfMessages) throws InterruptedException {
        ArrayList<String> messages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            messages.add("Message number " + i);
        }

        String filePath = "/home/lcm/temp/tlog_tests.txt";
        Filer.deleteIfExists(filePath);
        TLog.setGlobalDefaultFilePath(filePath);

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread(() -> {
                TLog.setCustomHeader("[Thread " + Thread.currentThread().getId() + "] ");
                for (String message: messages)
                    TLog.info(message);
                // TLog.clean();
                System.out.println("Thread finished!");
            }));
        }

        for (Thread t : threads)
            t.start();
        
        System.out.println("All threads started. Waiting to finish...");
        
        while (!threads.isEmpty()) {
            for (Iterator<Thread> it = threads.iterator(); it.hasNext();)
                if (!it.next().isAlive())
                    it.remove();
            System.gc();
            Thread.sleep(2000);
        }
        
        System.out.println("All threads finished. Counted " + Filer.get(filePath).readAsList().size() + " lines.");
    }
}