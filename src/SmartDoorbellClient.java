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
                checkFeatureStatus(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkFeatureStatus(String message) {
        if (message.contains("video recording is stopped")) {
            System.out.println("Alert: Video recording feature has stopped.");
        }
        if (message.contains("motion detection is stopped")) {
            System.out.println("Alert: Motion detection feature has stopped.");
        }
        if (message.contains("lock control is stopped")) {
            System.out.println("Alert: Lock control feature has stopped.");
        }
    }
}
