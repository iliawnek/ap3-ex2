package com.ap3.ex2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Worker implements Runnable {
    private LinkedBlockingQueue<String> workQueue;
    private List<String> anotherStructure;
    private Pattern pattern;

    Worker(LinkedBlockingQueue<String> workQueue, List<String> anotherStructure, Pattern pattern) {
        this.workQueue = workQueue;
        this.anotherStructure = anotherStructure;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        String currentDirectory;
        File currentFile;
        File innerFile;
        String[] entries;
        Matcher matcher;
        try {
            while (true) {
                currentDirectory = workQueue.take();
                currentFile = new File(currentDirectory);
                entries = currentFile.list();
                if (entries != null) {
                    for (String entry : entries) {
                        if (entry.equals(".") || entry.equals("..")) continue;
                        innerFile = new File(currentDirectory + "/" + entry);
                        if (innerFile.isDirectory()) {
                            workQueue.put(currentDirectory + "/" + entry);
                        } else {
                            matcher = pattern.matcher(entry);
                            if (matcher.matches()) {
                                anotherStructure.add(entry);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
}

public class MultipleWorkerFileCrawler {

    public static void main(String[] args) throws InterruptedException {
        // process arguments
        String bashPattern = args[0];
        String regexPattern = convertPattern(bashPattern);
        Pattern pattern = Pattern.compile(regexPattern);
        int crawlerThreads = 2;
        String directory;
        if (args.length > 1) {
            directory = args[1];
        } else {
            directory = ".";
        }
        for (String env : args) {
            String value = System.getenv(env);
            if (value != null) {
                crawlerThreads = Integer.parseInt(value);
            }
        }

        // populate work queue
        LinkedBlockingQueue<String> workQueue = new LinkedBlockingQueue<>();
        List<String> anotherStructure = Collections.synchronizedList(new ArrayList<>());
        Thread[] workers = new Thread[crawlerThreads];
        workQueue.put(directory);
        for (int i = 0; i < crawlerThreads; i++) {
            workers[i] = new Thread(new Worker(workQueue, anotherStructure, pattern));
            workers[i].start();
        }
        while (!workQueue.isEmpty() || !allThreadsWaiting(workers)) Thread.sleep(10);
        for (Thread worker : workers) worker.interrupt();

        // harvest the data in the Another Structure, printing out the results
        Collections.sort(anotherStructure);
        for (String match : anotherStructure) {
            System.out.println(match);
        }
    }

    private static boolean allThreadsWaiting(Thread[] threads) {
        for (Thread thread : threads) {
            if (thread.getState() != Thread.State.WAITING) {
                return false;
            }
        }
        return true;
    }

    private static String convertPattern(String str) {
        StringBuilder pat = new StringBuilder();
        int start, length;

        pat.append('^');
        if (str.charAt(0) == '\'') {    // double quoting on Windows
            start = 1;
            length = str.length() - 1;
        } else {
            start = 0;
            length = str.length();
        }
        for (int i = start; i < length; i++) {
            switch (str.charAt(i)) {
                case '*':
                    pat.append('.');
                    pat.append('*');
                    break;
                case '.':
                    pat.append('\\');
                    pat.append('.');
                    break;
                case '?':
                    pat.append('.');
                    break;
                default:
                    pat.append(str.charAt(i));
                    break;
            }
        }
        pat.append('$');
        return new String(pat);
    }
}
