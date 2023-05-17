import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleClient {
    public static void main(String[] args) {
        // Server Host
        final String serverHost = "localhost";
        final ArrayList<String> serverHosts = new ArrayList<>(Arrays.asList("localhost", "tp-1a201-10.enst.fr", "tp-1a201-11.enst.fr", "tp-1a201-12.enst.fr"));

        Socket socketOfClient = null;
        BufferedWriter os = null;
        BufferedReader is = null;
        ArrayList<Socket> sockets = new ArrayList<>();
        ArrayList<BufferedReader> readers = new ArrayList<>();
        ArrayList<PrintWriter> writers = new ArrayList<>();
        int port = 9990;

        try {
            for(int i=0; i<serverHosts.size(); ++i) {
                sockets.add(new Socket(serverHosts.get(i), port));
                readers.add(new BufferedReader(new InputStreamReader(sockets.get(i).getInputStream())));
                writers.add(new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets.get(i).getOutputStream())), true));
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + serverHost);
            return;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + serverHost);
            return;
        }

        try {
            BufferedReader fileReader = new BufferedReader(new FileReader("CC-MAIN-20220116093137-20220116123137-00001.warc.wet"));
            String line;
            int j = 0;
            while ((line = fileReader.readLine()) != null) {
                if (j<serverHosts.size()) {
                    writers.get(j).println("START SPLIT");
                }
                j++;
                writers.get(j%serverHosts.size()).println(line);
            }
            for(int i=0; i<serverHosts.size(); ++i) {
                writers.get(i).println("QUIT");
            }
            
            for(int i=0; i<serverHosts.size(); ++i) {
                sockets.get(i).close();
                readers.get(i).close();
                writers.get(i).close();
            }
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }

}