import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SimpleServerProgram {

    static ArrayList<String> serverNames = new ArrayList<>();
    static ArrayList<String> words = new ArrayList<>();
    
    public static void main(String args[]) {

        ServerSocket listener = null;
        String line;
        BufferedReader is;
        PrintWriter os;
        Socket socketOfServer = null;

        // Try to open a server socket on port 9999
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)

    
        try {
            listener = new ServerSocket(9990);
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
            os = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream())), true);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("results.txt")), true);
     
            serverNames = new ArrayList(Arrays.asList(is.readLine().split(" ")));


            while (true) {
                // Read data to the server (sent from client).
                line = is.readLine();
                // If users send QUIT (To end conversation).
                if (line.equals("QUIT MAPPING")) {
                    break;
                }
                line = line.replaceAll("\\p{Punct}", "").toLowerCase();
            
                // split the line into words and update the word counts
                String[] tmp = line.split("\\s+");
                for (String word : tmp) {
                    words.add(word);
                    writer.println(word);
                }
                
            }

            os.println("DONE MAPPING");

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("Sever stopped!");
    }
}