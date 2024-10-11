import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FaultRecoveryClient {
    private static final int PRIMARY_PORT = 9090;
    private static final int SECONDARY_PORT = 9091;
    private static final String HOST = "localhost";
    private static volatile boolean primaryRunning = true;

    public static void main(String[] args) {
        Thread primaryThread = new Thread(() -> monitorPrimaryServer());
        Thread secondaryThread = new Thread(() -> monitorSecondaryServer());

        primaryThread.start();
        secondaryThread.start();
    }

    private static void monitorPrimaryServer() {
        while (primaryRunning) {
            try (Socket socket = new Socket(HOST, PRIMARY_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null && primaryRunning) {
                    System.out.println("Received from Primary: " + message);
                    checkSystemStatus(message, "Primary");
                }
            } catch (IOException e) {
                System.out.println("Primary server connection failed: " + e.getMessage());
                primaryRunning = false;
                System.out.println("Switching to Secondary server...");
            }
        }
    }

    private static void monitorSecondaryServer() {
        while (true) {
            try (Socket socket = new Socket(HOST, SECONDARY_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    if (!primaryRunning) {
                        System.out.println("Received from Secondary: " + message);
                        checkSystemStatus(message, "Secondary");
                    }
                }
            } catch (IOException e) {
                System.out.println("Secondary server connection failed: " + e.getMessage());
                try {
                    Thread.sleep(2000); // Retry connection after 2 seconds
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static void checkSystemStatus(String message, String serverName) {
        if (message.contains("system is stopped")) {
            System.out.println("Alert: " + serverName + " system has stopped.");
            if (serverName.equals("Primary")) {
                primaryRunning = false;
            }
        } else if (message.contains("with counter")) {
            System.out.println("Counter value from " + serverName + ": " + message);
        }
    }
}
