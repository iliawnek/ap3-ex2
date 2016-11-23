import java.io.File;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SingleWorker implements Runnable {
    private LinkedBlockingQueue<String> workQueue;
    private ConcurrentSkipListSet<String> anotherStructure;
    private Pattern pattern;

    SingleWorker(LinkedBlockingQueue<String> workQueue, ConcurrentSkipListSet<String> anotherStructure, Pattern pattern) {
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
                if (currentDirectory.equals("!!!")) {
                    return;
                }
                currentFile = new File(currentDirectory);
                entries = currentFile.list();
                if (entries != null) {
                    for (String entry : entries) {
                        innerFile = new File(currentDirectory + "/" + entry);
                        if (!innerFile.isDirectory()) {
                            matcher = pattern.matcher(entry);
                            if (matcher.matches()) {
                                anotherStructure.add(currentDirectory + "/" + entry);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class SingleWorkerFileCrawler {

    public static void main(String[] args) throws InterruptedException {
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
        LinkedBlockingQueue<String> workQueue = new LinkedBlockingQueue<>();
        ConcurrentSkipListSet<String> anotherStructure = new ConcurrentSkipListSet<>();
        Thread worker = new Thread(new SingleWorker(workQueue, anotherStructure, pattern));
        worker.start();
        processDirectory(directory, workQueue);
        workQueue.put("!!!"); // poison
        worker.join();

        // harvest the data in the Another Structure, printing out the results
        for (String match : anotherStructure) {
            System.out.println(match);
        }
    }

    private static void processDirectory(String name, LinkedBlockingQueue<String> list) {
        try {
            File file = new File(name); // create a File object
            if (file.isDirectory()) { // a directory - could be symlink
                String entries[] = file.list();
                if (entries != null) { // not a symlink
                    list.put(name);
                    for (String entry : entries) {
                        if (entry.compareTo(".") == 0)
                            continue;
                        if (entry.compareTo("") == 0)
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
