package com.ap3.ex2;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Worker implements Runnable {
    private LinkedBlockingQueue<String> workQueue;
    private LinkedBlockingQueue<String> anotherStructure;
    private Pattern pattern;

    Worker(LinkedBlockingQueue<String> workQueue, LinkedBlockingQueue<String> anotherStructure, Pattern pattern) {
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
                                anotherStructure.put(entry);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException ignored) {}
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
        LinkedBlockingQueue<String> anotherStructure = new LinkedBlockingQueue<>();
        Thread[] workers = new Thread[crawlerThreads];
        workQueue.put(directory);
        for (int i = 0; i < crawlerThreads; i++) {
            workers[i] = new Thread(new Worker(workQueue, anotherStructure, pattern));
            workers[i].start();
        }
        while (!workQueue.isEmpty() || !allThreadsWaiting(workers)) Thread.sleep(10);
        for (Thread worker : workers) worker.interrupt();

        // harvest the data in the Another Structure, printing out the results
        String harvest;
        while (!anotherStructure.isEmpty()) {
            harvest = anotherStructure.remove();
            System.out.println(harvest);
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

    private static void processDirectory(String name, LinkedBlockingQueue<String> list) {
        try {
            File file = new File(name); // create a File object
            if (file.isDirectory()) { // a directory - could be symlink
                String entries[] = file.list();
                if (entries != null) { // not a symlink
//                    System.out.format("putting %s\n", name);
                    list.put(name);
                    for (String entry : entries) {
                        if (entry.compareTo(".") == 0)
                            continue;
                        if (entry.compareTo("..") == 0)
                            continue;
                        processDirectory(name + "/" + entry, list);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing " + name + ": " + e);
        }
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
