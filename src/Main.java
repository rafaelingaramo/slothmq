import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Waiting new connections");
        //Thread every new connection ?

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            Socket socketClient = serverSocket.accept();

            InputStream inputStream = socketClient.getInputStream();
            //lets collect the message on a string for now, to be improved later
            StringBuilder builder = new StringBuilder();
            byte[] buffer = new byte[1024];
            while (true) {
                int read = inputStream.read(buffer);
                if (read == -1) { //EOF
                    //nothing to read, exit
                    break;
                }
                if (read == 0) {
                    //no bytes to read this time
                    continue;
                }
                builder.append(new String(buffer), 0, read);
                if (builder.toString().contains("--EOM")) {
                    System.out.println("posting message: " + builder);
                    System.out.println("waiting for more");
                    builder = new StringBuilder();
                }

                if (builder.toString().contains("--EOS")) {
                    System.out.println("End of stream");
                    break;
                }
            }
        }
    }
}