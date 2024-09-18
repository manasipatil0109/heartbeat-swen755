import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SmartDoorbellClient {
    private static final int PORT = 9090;
    private static final String HOST = "localhost";
    private static final int CHECK_INTERVAL = 2; // seconds

    private static final Map<String, Boolean> featureStatus = new HashMap<>();

    public static void main(String[] args) {
        featureStatus.put("video recording", false);
        featureStatus.put("motion detection", false);
        featureStatus.put("lock control", false);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Task to check if all features are active
        Runnable checkFeatures = () -> {
            for (Map.Entry<String, Boolean> entry : featureStatus.entrySet()) {
                if (!entry.getValue()) {
                    System.out.println("Warning: " + entry.getKey() + " is not active!");
                }
                featureStatus.put(entry.getKey(), false); // Reset for the next check
            }
        };

        // Schedule the feature check task to run every CHECK_INTERVAL seconds
        scheduler.scheduleAtFixedRate(checkFeatures, CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                String feature = message.split(" ")[0];
                featureStatus.put(feature, true); // Mark the feature as active
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
