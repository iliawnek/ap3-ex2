package com.ap3.ex2;

import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequentialFileCrawler {

    public static void main(String[] args) {
        // process arguments
        String bashPattern = args[0];
        String regexPattern = convertPattern(bashPattern);
        Pattern pattern = Pattern.compile(regexPattern);
        String directory;
        if (args.length > 1) {
            directory = args[1];
        } else {
            directory = ".";
        }

        // populate work queue
        LinkedList<String> workQueue = new LinkedList<>();
        processDirectory(directory, workQueue);

        // process the Work Queue, placing the processed data in the Another Structure
        LinkedList<String> anotherStructure = new LinkedList<>();
        String currentDirectory;
        File currentFile;
        String[] entries;
        Matcher matcher;
        while (!workQueue.isEmpty()) {
            currentDirectory = workQueue.remove();
            currentFile = new File(currentDirectory);
            entries = currentFile.list();
            if (entries != null) {
                for (String entry : entries) {
                    matcher = pattern.matcher(entry);
                    if (matcher.matches()) {
                        anotherStructure.addLast(entry);
                    }
                }
            }
        }

        // harvest the data in the Another Structure, printing out the results
        String harvest;
        while (!anotherStructure.isEmpty()) {
            harvest = anotherStructure.remove();
            System.out.println(harvest);
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

    private static void processDirectory(String name, LinkedList<String> list) {
        try {
            File file = new File(name); // create a File object
            if (file.isDirectory()) { // a directory - could be symlink
                String entries[] = file.list();
                if (entries != null) { // not a symlink
                    list.addLast(name);
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
}
