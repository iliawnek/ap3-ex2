import java.io.*;

/**
 * A simple class to print out a directory tree, depth first
 */
public class DirectoryTree {
    /**
     * Works on a single file system entry and
     * calls itself recursively if it turns out
     * to be a directory.
     *
     * @param name The name of a directory to visit
     */
    public void processDirectory(String name) {
        try {
            File file = new File(name);    // create a File object
            if (file.isDirectory()) {    // a directory - could be symlink
                String entries[] = file.list();
                if (entries != null) {    // not a symlink
                    System.out.println(name);// print out the name
                    for (String entry : entries) {
                        if (entry.compareTo(".") == 0)
                            continue;
                        if (entry.compareTo("..") == 0)
                            continue;
                        processDirectory(name + "/" + entry);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing " + name + ": " + e);
        }
    }

    /**
     * The program starts here.
     *
     * @param args The arguments from the command line
     */
    public static void main(String args[]) {
        // Create an object of this class
        DirectoryTree dt = new DirectoryTree();

        if (args.length == 0) {
            // If there are no arguments, traverse the current directory
            dt.processDirectory(".");
        } else {
            // Else process every argument sequentially
            for (String arg : args) {
                dt.processDirectory(arg);
            }
        }
    }
}
