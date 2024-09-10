import Demo.PrinterPrx;
import Demo.Response;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static final Scanner scanner = new Scanner(System.in);
    private static List<Long> latencies = new ArrayList<>();
    private static List<Long> processingTimes = new ArrayList<>();
    private static List<Long> networkPerformance = new ArrayList<>();
    private static List<Long> jitters = new ArrayList<>();
    private static List<Double> missingRates = new ArrayList<>();
    private static List<String> sentMessages = new ArrayList<>();
    private static List<Double> unprocessRates = new ArrayList<>();
    private static List<Double> throughput = new ArrayList<>();

    private static int successfulRequests = 0;
    private static int totalRequests = 0;
    private static PrinterPrx service;

    public static void main(String[] args) {
        List<String> extraArgs = new ArrayList<>();
        try (Communicator communicator = Util.initialize(args, "client.cfg", extraArgs)) {
            service = PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));
            if (service == null) {
                throw new Error("Invalid proxy");
            }

            displayMenu(communicator);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayMenu(Communicator communicator) throws UnknownHostException {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n====================================================================================");
            System.out.println("----- MAIN MENU -------");
            System.out.println("1. Send a message to the server");
            System.out.println("2. Generate performance report");
            System.out.println("3. Exit");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    sendMessageToServer(communicator);
                    break;
                case "2":
                    generateReport();
                    break;
                case "3":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void sendMessageToServer(Communicator communicator) throws UnknownHostException {
        String username = System.getProperty("user.name").replace(" ", "").trim();
        String hostname = Inet4Address.getLocalHost().getHostName().trim();

        boolean exit = false;
        while (!exit) {
            String prefix = username + ":" + hostname + "=>";
            System.out.println("====================================================================================");
            System.out.print(prefix);
            String input;
            do {

                input = scanner.nextLine();
            } while (input.equals(""));

            exit = input.equalsIgnoreCase("exit");
            if (exit) {
                System.out.println("Thank you for using our services. See you soon!");
            } else {
                sentMessages.add(input);

                long start = System.currentTimeMillis();
                try {
                    Response response = service.printString(prefix + input);
                    long latency = System.currentTimeMillis() - start;
                    latencies.add(latency);

                    long processingTime = response.responseTime;
                    processingTimes.add(processingTime);

                    long netPerformance = latency - processingTime;
                    networkPerformance.add(netPerformance);

                    System.out.println("Server response: " + response.value);
                    System.out.println("Processing time: " + processingTime + " ms");
                    System.out.println("Latency: " + latency + " ms");
                    System.out.println("Network Performance: " + netPerformance + " ms");

                    if (latencies.size() > 1) {
                        calculateJitter();
                    }
                    successfulRequests++;
                    totalRequests++;

                    calculateMissingRate();


                } catch (RuntimeException e) {
                    totalRequests++;
                    System.err.println("Request could not be processed");
                    calculateMissingRate();
                }
            }
        }
    }

    private static void calculateJitter() {
        long jitter = 0;
        for (int i = 1; i < latencies.size(); i++) {
            jitter += Math.abs(latencies.get(i) - latencies.get(i - 1));
        }
        jitter /= (latencies.size() - 1);
        jitters.add(jitter);  // Store jitter value
        System.out.println("Jitter: " + jitter + " ms");
    }

    private static void calculateMissingRate() {
        double missingRate = (double) (totalRequests - successfulRequests) / totalRequests * 100;
        missingRates.add(missingRate);  // Store missing rate value
        System.out.println("Missing Rate: " + missingRate + " %");
    }

    private static void generateReport() {
        System.out.println("\n--- Performance Report ---");
        System.out.println("| Sent Message                                      | Latency (ms) | Processing Time (ms) | Net Performance (ms) | Jitter (ms) | Missing Rate (%) |");
        System.out.println("|---------------------------------------------------|--------------|----------------------|----------------------|-------------|------------------|");

        for (int i = 0; i < latencies.size(); i++) {
            System.out.printf("| %-51s | %12d | %22d | %22d | %11d | %16.2f |\n",
                    sentMessages.get(i), latencies.get(i), processingTimes.get(i), networkPerformance.get(i),
                    (i == 0 ? 0 : jitters.get(i - 1)), missingRates.get(i));
        }
    }
}
