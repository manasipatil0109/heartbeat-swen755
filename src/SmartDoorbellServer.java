import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Scanner;

public class SmartDoorbellServer {
    private static final int PORT = 9090;
    private static final String HOST = "localhost";
    private static final int THREAD_COUNT = 3;
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Starting video recording function
        System.out.println("Starting Video Recording");
        Future<?> videoFuture = executorService.submit(new HeartbeatTask("video recording"));

        // Starting motion detection function
        System.out.println("Starting Motion Detection");
        Future<?> motionFuture = executorService.submit(new HeartbeatTask("motion detection"));

        // Starting the lock-unlock function
        System.out.println("Starting Lock Control");
        Future<?> lockFuture = executorService.submit(new HeartbeatTask("lock control"));

        // User input to stop threads to simulate crashing the features
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'stop video', 'stop motion', 'stop lock', or 'exit':");
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "stop video":
                    videoFuture.cancel(true);
                    System.out.println("Video recording stopped.");
                    break;
                case "stop motion":
                    motionFuture.cancel(true);
                    System.out.println("Motion detection stopped.");
                    break;
                case "stop lock":
                    lockFuture.cancel(true);
                    System.out.println("Lock control stopped.");
                    break;
                case "exit":
                    isRunning = false;
                    executorService.shutdownNow();
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid input. Please try again.");
            }
        }
    }

    static class HeartbeatTask implements Runnable {
        private final String feature;

        public HeartbeatTask(String feature) {
            this.feature = feature;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(HOST, PORT);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    out.println(feature + " is alive");
                    Thread.sleep(1000); // Send message every second
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    System.out.println(feature + " function interrupted.");
                } else {
                    e.printStackTrace();
                }
            }
        }
    }
}
