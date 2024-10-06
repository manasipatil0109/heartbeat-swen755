import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SmartDoorbellClient {
    private static final int PORT = 9090;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                checkSystemStatus(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkSystemStatus(String message) {
        if (message.contains("System is stopped")) {
            System.out.println("Alert: The system has stopped.");
        }
    }
}
