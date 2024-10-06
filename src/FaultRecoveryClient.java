import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FaultRecoveryClient {
    private static final int PRIMARY_PORT = 9090;
    private static final int SECONDARY_PORT = 9091;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        Thread primaryThread = new Thread(() -> monitorServer(PRIMARY_PORT, "Primary"));
        Thread secondaryThread = new Thread(() -> monitorServer(SECONDARY_PORT, "Secondary"));

        primaryThread.start();
        secondaryThread.start();
    }

    private static void monitorServer(int port, String serverName) {
        try (Socket socket = new Socket(HOST, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + serverName + ": " + message);
                checkSystemStatus(message, serverName);
            }
        } catch (IOException e) {
            System.out.println(serverName + " server connection failed: " + e.getMessage());
        }
    }

    private static void checkSystemStatus(String message, String serverName) {
        if (message.contains("system is stopped")) {
            System.out.println("Alert: " + serverName + " system has stopped.");
        }
    }
}
