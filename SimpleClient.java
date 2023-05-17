import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleClient {
    static final ArrayList<String> serverHosts = new ArrayList<>(Arrays.asList("localhost", "tp-1a201-10.enst.fr", "tp-1a201-11.enst.fr", "tp-1a201-12.enst.fr"));

    static Socket socketOfClient = null;
    static BufferedWriter os = null;
    static BufferedReader is = null;
    static ArrayList<Socket> sockets = new ArrayList<>();
    static ArrayList<BufferedReader> readers = new ArrayList<>();
    static ArrayList<PrintWriter> writers = new ArrayList<>();
    static int port = 9990;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Server Host

        try {
            for(int i=0; i<serverHosts.size(); ++i) {
                sockets.add(new Socket(serverHosts.get(i), port));
                readers.add(new BufferedReader(new InputStreamReader(sockets.get(i).getInputStream())));
                writers.add(new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets.get(i).getOutputStream())), true));
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        int thread_count = serverHosts.size();
        String filename = "CC-MAIN-20220116093137-20220116123137-00001.warc.wet";

        ExecutorService executor = Executors.newFixedThreadPool(thread_count);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Determine the number of lines in the file
            long numLines = reader.lines().count();

            // Calculate the number of lines to be read by each thread
            long linesPerThread = numLines / thread_count;
            long remainingLines = numLines % thread_count;

            // Create a list to hold the results
            List<String> allLines = new ArrayList<>();

            // Create and submit tasks to the thread pool
            for (int i = 0; i < thread_count; i++) {
                long linesToRead = linesPerThread + (i == 0 ? remainingLines : 0);
                executor.submit(new FileReaderTask(filename, i * linesPerThread, linesToRead, allLines, i));
            }

            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to complete
            }

        // try {
        //     BufferedReader fileReader = new BufferedReader(new FileReader("CC-MAIN-20220116093137-20220116123137-00001.warc.wet"));
        //     String line;
        //     int j = 0;
        //     while ((line = fileReader.readLine()) != null) {
        //         if (j<serverHosts.size()) {
        //             writers.get(j).println("START SPLIT");
        //         }
        //         j++;
        //         writers.get(j%serverHosts.size()).println(line);
        //     }
        //     for(int i=0; i<serverHosts.size(); ++i) {
        //         writers.get(i).println("QUIT");
        //     }
            
        //     for(int i=0; i<serverHosts.size(); ++i) {
        //         sockets.get(i).close();
        //         readers.get(i).close();
        //         writers.get(i).close();
        //     }
        // } catch (UnknownHostException e) {
        //     System.err.println("Trying to connect to unknown host: " + e);
        // } catch (IOException e) {
        //     System.err.println("IOException:  " + e);
        // }
        }
    }
    private static class FileReaderTask implements Runnable {
        private final String filename;
        private final long startLine;
        private final long numLines;
        private final List<String> resultLines;
        private int server;

        public FileReaderTask(String filename, long startLine, long numLines, List<String> resultLines, int server) {
            this.filename = filename;
            this.startLine = startLine;
            this.numLines = numLines;
            this.resultLines = resultLines;
            this.server = server;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                // Skip lines until the starting line
                for (long i = 0; i < startLine; i++) {
                    reader.readLine();
                }

                // Read the specified number of lines
                for (long i = 0; i < numLines; i++) {
                    String line = reader.readLine();
                    if (line != null) {
                        writers.get(server).println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}