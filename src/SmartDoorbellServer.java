import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmartDoorbellServer {
    private static final int PORT = 9090;
    private static final int THREAD_COUNT = 3;
    private static volatile boolean isRunning = true;
    private static final AtomicBoolean videoRunning = new AtomicBoolean(true);
    private static final AtomicBoolean motionRunning = new AtomicBoolean(true);
    private static final AtomicBoolean lockRunning = new AtomicBoolean(true);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Starting the consolidated heartbeat task
        System.out.println("Starting Heartbeat Task");
        Future<?> heartbeatFuture = executorService.submit(new HeartbeatTask());

        // User input to stop threads to simulate crashing the features
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'stop video', 'stop motion', 'stop lock', or 'exit':");
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "stop video":
                    videoRunning.set(false);
                    System.out.println("Video recording stopped.");
                    break;
                case "stop motion":
                    motionRunning.set(false);
                    System.out.println("Motion detection stopped.");
                    break;
                case "stop lock":
                    lockRunning.set(false);
                    System.out.println("Lock control stopped.");
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
                        while (isRunning && !Thread.currentThread().isInterrupted()) {
                            String statusMessage = getStatusMessage();
                            out.println(statusMessage);
                            Thread.sleep(1000); // Send message every second
                        }
                    } catch (IOException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
                            System.out.println("Heartbeat task interrupted.");
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
            StringBuilder statusMessage = new StringBuilder();
            statusMessage.append("video recording is ").append(videoRunning.get() ? "alive" : "stopped").append(", ");
            statusMessage.append("motion detection is ").append(motionRunning.get() ? "alive" : "stopped").append(", ");
            statusMessage.append("lock control is ").append(lockRunning.get() ? "alive" : "stopped");
            return statusMessage.toString();
        }
    }
}
