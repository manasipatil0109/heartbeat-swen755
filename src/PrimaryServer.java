import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrimaryServer {
    private static final int PORT = 9090;
    private static final int THREAD_COUNT = 3;
    private static volatile boolean isRunning = true;
    private static final AtomicBoolean systemRunning = new AtomicBoolean(true);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Starting the consolidated heartbeat task
        System.out.println("Starting Heartbeat Task");
        Future<?> heartbeatFuture = executorService.submit(new HeartbeatTask());

        // User input to stop the system to simulate crashing the feature
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'stop system' or 'exit':");
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "stop system":
                    systemRunning.set(false);
                    System.out.println("System stopped.");
                    break;
                case "exit":
                    isRunning = false;
                    heartbeatFuture.cancel(true);
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
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    try (Socket socket = serverSocket.accept();
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                        int iterationCount = 0;
                        while (isRunning && !Thread.currentThread().isInterrupted()) {
                            String statusMessage = getStatusMessage();
                            out.println(statusMessage);
                            iterationCount++;
                            // Simulate a random crash after a few iterations
                            if (iterationCount > 5 && ThreadLocalRandom.current().nextInt(100) < 5) {
                                throw new RuntimeException("Simulated crash");
                            }
                            // Sleep for 1 second between heartbeats
                            Thread.sleep(1000);
                        }
                    } catch (IOException | RuntimeException | InterruptedException e) {
                        if (e instanceof RuntimeException) {
                            System.out.println("Heartbeat task crashed: " + e.getMessage());
                            break;
                        } else if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getStatusMessage() {
            return "Primary system is " + (systemRunning.get() ? "alive" : "stopped");
        }
    }
}
