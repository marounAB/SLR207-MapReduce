import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

public class SimpleServerProgram {

    public static ArrayList<String> serverNames = new ArrayList<>();
    public static ArrayList<String> words = new ArrayList<>();
    public static ArrayList<Socket> serverSockets = new ArrayList<>();
    public static ArrayList<PrintWriter> writers = new ArrayList<>();
    public static ArrayList<BufferedReader> readers = new ArrayList<>();
    public static CopyOnWriteArrayList<String> mywords = new CopyOnWriteArrayList<>();
    public static Map<String, Integer> wordCount = new HashMap<>();
    public static ArrayList<Socket> tmpSockets = new ArrayList<>();
    public static ArrayList<BufferedReader> tmpReaders = new ArrayList<>();
    public static int port = 9990;
    
    public static void main(String args[]) {

        ServerSocket listener = null;
        String line;
        BufferedReader is;
        InputStream inputStream;
        PrintWriter os;
        Socket socketOfServer = null;

        // Try to open a server socket on port 9999
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)

    
        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            System.out.println("Server is waiting to accept user...");

            // Accept client connection request
            // Get new Socket at Server.    
            socketOfServer = listener.accept();
            System.out.println("Accept a client!");

            // Open input and output streams
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            inputStream = socketOfServer.getInputStream();
            os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream())), true);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("results.txt")), true);
     
            serverNames = new ArrayList(Arrays.asList(is.readLine().split(" ")));
            for(String name : serverNames) {
                System.out.print(name + " ");
            }
            System.out.println();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int size = Integer.parseInt(is.readLine());
            System.out.println("size = " + size);
            byte[] buffer = new byte[8912];
            int bytesRead;
            int numBytes = 0;
            while (numBytes < size) {
                bytesRead = inputStream.read(buffer);
                numBytes += bytesRead;
                outputStream.write(buffer, 0, bytesRead);
            }
            
            System.out.println(numBytes);

            System.out.println("5alast recieve");
            // Transform the received binary data into a string
            String receivedString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            String[] lines = receivedString.split("\\r?\\n");
            for(String line1: lines) {
                line1 = line1.replaceAll("\\p{Punct}", "").toLowerCase();
            
                // split the line into words and update the word counts
                String[] tmp = line1.split("\\s+");
                for (String word : tmp) {
                    words.add(word);
                    // writer.println(word);
                }
            }

            RecieverFactory recieverFactory = new RecieverFactory(listener);
            recieverFactory.start();
            
            os.println("DONE MAPPING");

            String startShuffle;
            while (!(startShuffle = is.readLine()).equals("SHUFFLE")) {}
            System.out.println(startShuffle + " to start shuffle");

            for(int i=0; i<serverNames.size(); ++i) {
                Socket socket = new Socket(serverNames.get(i), port);
                serverSockets.add(socket);
                writers.add(new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true));
                readers.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            }

            System.out.println("STARTED SENDING");

            int n = serverNames.size();
            long n1 = serverNames.size();
            System.out.println("size=" + n);
            System.out.println("size1=" + n1);
            ArrayList<ArrayList<String>> data = new ArrayList<>();
            for(int i=0; i<n; ++i) {
                data.add(new ArrayList<>());
            } 
            for(int i=0; i<words.size(); ++i) {
                int j = words.get(i).hashCode()%n;
                int tosend = (j>=0)?j:(n+j);
                // writers.get(tosend).println(words.get(i));
                data.get(tosend).add(words.get(i));
            }
            System.out.println("DONE ASSEMBLING");
            for(int i=0; i<n; ++i) {
                writers.get(i).println(String.join(" ", data.get(i)));
            }
            System.out.println("DONE SENDING");

            for(int i=0; i<serverNames.size(); ++i) {
                writers.get(i).println("DONE SHUFFLING");
            }

            recieverFactory.join();

            for(String word : mywords) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                os.println(entry.getKey() + " " + entry.getValue());
            }
            os.println("QUIT");

            // for(int i=0; i<serverSockets.size(); ++i) {
            //     writers.get(i).close();
            //     readers.get(i).close();
            //     serverSockets.get(i).close();
            // }
            // os.close();
            // is.close();
            // listener.close();
            // socketOfServer.close();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("Sever stopped!");
    }

    private static class RecieverFactory extends Thread {
        private ServerSocket serverSocket;

        public RecieverFactory(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            // ExecutorService executor = Executors.newFixedThreadPool(serverNames.size());
            System.out.println("opened server socket");
            ArrayList<Thread> tmp = new ArrayList<>();
            for(int i=0; i<serverNames.size(); ++i) {
                try {
                    tmpSockets.add(serverSocket.accept());
                    tmpReaders.add(new BufferedReader(new InputStreamReader(tmpSockets.get(i).getInputStream())));
                    System.out.println("started socket " + i);
                    // executor.submit(new Reciever(i, socket));
                    tmp.add(new Reciever(i));
                    tmp.get(i).start();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.println("error with socket " +i);
                    // e.printStackTrace();
                }
            }
            try {
                // executor.shutdown();
                // while (!executor.isTerminated()) {
                //     // Wait for all tasks to complete
                // }    
                for(int i=0; i<tmp.size(); ++i) {
                    tmp.get(i).join();
                    System.out.println(i+ "terminated");
                }
            }
            catch (Exception e) {
                System.out.println("error with executor");
            }
        }
    }

    private static class Reciever extends Thread {
        int id;

        public Reciever(int id) {
            this.id = id;
        }


        @Override
        public void run() {
            try {
                System.out.println("before opening reader");
                boolean done = false;
                long c = 0;
                while (!done) {
                    String word;
                    word = tmpReaders.get(id).readLine();
                    // if (c%100 == 0) {
                    //     System.out.println("100 recieved");
                    // }
                    if (word.equals("DONE SHUFFLING")) {
                        done = true;
                    }
                    else {
                        mywords.addAll(new ArrayList(Arrays.asList(word.split(" "))));
                    }
                }
                System.out.println("done getting my words");
                // reader.close();
                // socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // System.out.println("error while reading my words");
            }
        }
    }
}