import java.io.*;
import java.net.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class SimpleClient {
    static final ArrayList<String> serverHosts = new ArrayList<>();
    
    static Map<String, Integer> wordCounts = new ConcurrentHashMap<>();

    static Socket socketOfClient = null;
    static BufferedWriter os = null;
    static BufferedReader is = null;
    static ArrayList<Socket> sockets = new ArrayList<>();
    static ArrayList<BufferedReader> readers = new ArrayList<>();
    static ArrayList<PrintWriter> writers = new ArrayList<>();
    static ArrayList<BufferedOutputStream> bwriters = new ArrayList<>();
    static long startTime;
    static int port = 9990;
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Server Host

        try {
            BufferedReader br = new BufferedReader(new FileReader("machines.txt"));
            String machine;
            while ((machine=br.readLine())!=null) {
                serverHosts.add(machine);
            }
            br.close();
            
            String serverNames = String.join(" ", serverHosts);
            for(int i=0; i<serverHosts.size(); ++i) {
                sockets.add(new Socket(serverHosts.get(i), port));
                readers.add(new BufferedReader(new InputStreamReader(sockets.get(i).getInputStream())));
                writers.add(new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets.get(i).getOutputStream())), true));
                bwriters.add(new BufferedOutputStream(sockets.get(i).getOutputStream()));
                writers.get(i).println(serverNames);
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        
        int thread_count = serverHosts.size();
        
        ExecutorService executor = Executors.newFixedThreadPool(thread_count);
        
        try {
            
            BufferedReader fileReader = new BufferedReader(new FileReader("splits.txt"));
            // Create and submit tasks to the thread pool
            for (int i = 0; i < thread_count; i++) {
                ArrayList<String> tmp = new ArrayList<>();
                for(int j=0; j<24/thread_count; ++j) {
                    tmp.add(fileReader.readLine());
                }
                // int linesToRead = linesPerThread + (i == thread_count-1 ? remainingLines : 0);
                executor.submit(new FileReaderTask(tmp, i));
            }
            
            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to complete
            }

            startTime = System.currentTimeMillis();

            System.out.println("all sent");

            for(int i=0; i<serverHosts.size(); ++i) {
                String line = readers.get(i).readLine();
                while (!line.equals("DONE MAPPING")) {
                    line = readers.get(i).readLine();
                }
            }
            System.out.println("Mapping phase is done");

            for(int i=0; i<serverHosts.size(); ++i) {
                writers.get(i).println("SHUFFLE");
            }
            
            ArrayList<Gatherer> gatherers = new ArrayList<>();
            for(int i=0; i<serverHosts.size(); ++i) {
                gatherers.add(new Gatherer(i));
                gatherers.get(i).start();
            }

            for(int i=0; i<gatherers.size(); ++i) {
                gatherers.get(i).join();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Count Complete in " + (endTime - startTime) + " ms");

            Map<String, Integer> sortedWordCounts = new TreeMap<>(
                    Comparator.<String, Integer>comparing(wordCounts::get).reversed()
                            .thenComparing(Comparator.naturalOrder()));

            sortedWordCounts.putAll(wordCounts);
            System.out.println("the 5 most recurring words are:");
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedWordCounts.entrySet()) {
                if (count >= 5) {
                    break;
                }
                System.out.println(entry.getKey() + ": " + entry.getValue());
                count++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class FileReaderTask implements Runnable {
        // private final byte[] fileBytes;
        private final int startLine;
        private final int numLines;
        private int server;
        private ArrayList<String> filenames = new ArrayList<>();

        public FileReaderTask(byte[] fileBytes, int startLine, int numLines, int server) {
            // this.fileBytes = fileBytes;
            this.startLine = startLine;
            this.numLines = numLines;
            this.server = server;
        }

        public FileReaderTask(ArrayList<String> filenames, int server) {
            this.filenames.addAll(filenames);
            this.startLine = 0;
            this.numLines = 0;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                for(String filename : filenames) {
                    File file = new File(filename);

                    try (FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel()) {
                        long fileSize = fileChannel.size();

                        // Memory-map the file for reading
                        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

                        // Create a byte array to hold the file data
                        byte[] fileBytes = new byte[(int) fileSize];

                        // Transfer the data from the buffer to the byte array
                        buffer.get(fileBytes);

                        bwriters.get(server).write(fileBytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bwriters.get(server).flush();
                writers.get(server).close();
                bwriters.get(server).close();
                sockets.set(server, null);
                Thread.sleep(5000);
                while (sockets.get(server) == null) {
                    try {
                        sockets.set(server, new Socket(serverHosts.get(server), port));
                        readers.set(server, new BufferedReader(new InputStreamReader(sockets.get(server).getInputStream())));
                        writers.set(server, new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets.get(server).getOutputStream())), true));
                        // connected = true;
                    }
                    catch (Exception e) {
                        Thread.sleep(1000);
                    }
                    
                }
                System.out.println("sent to " + server);
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {

            }
        }
    }

    private static class Gatherer extends Thread {
        private int id;

        public Gatherer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                String word;
                while ((word = readers.get(id).readLine()) != null) {
                    String[] tmp = word.split(" ");
                    wordCounts.put(tmp[0], Integer.parseInt(tmp[1]));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
