import Demo.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PrinterI implements Demo.Printer {

    private long time = 0;

    public Response printString(String s, com.zeroc.Ice.Current current) {
        long start = System.currentTimeMillis();
        System.out.println("Executing the command: " + s);

        String[] msgArray = s.split(" ");

        if (msgArray.length == 1) {
            return new Response(0L, "You sent an empty message, please try again");
        }

        String message = msgArray[1];
        String serverResponse;

        try {
            serverResponse = checkIfNaturalNumber(s, Long.parseLong(message));
        } catch (NumberFormatException e) {
            serverResponse = handleNonNumericInput(s, message);
        }

        System.out.println(serverResponse);

        time = time + (System.currentTimeMillis() - start);
        System.out.println("Response time: " + time + "ms\n");
        return new Response(time, serverResponse);
    }

    private static String checkIfNaturalNumber(String s, Long n) {
        if (n > 0) {
            StringBuilder response = new StringBuilder();

            Long[] fibonacciArray = new Long[n.intValue()];
            for (int i = 0; i < n; i++) {
                fibonacciArray[i] = fibonacci((long) i, new Long[n.intValue()]);
                response.append(fibonacciArray[i]).append(" ");
            }

            response.append("\nPrime factors of ").append(n).append(": ");
            response.append(calculatePrimeFactors(n));

            return response.toString();
        } else {
            return String.valueOf(n);
        }
    }

    public static Long fibonacci(Long num, Long[] memo) {
        if (num == 0 || num == 1) {
            return num;
        }
        if (memo[num.intValue()] == null) {
            memo[num.intValue()] = fibonacci(num - 1, memo) + fibonacci(num - 2, memo);
        }
        return memo[num.intValue()];
    }

    public static String calculatePrimeFactors(Long n) {
        StringBuilder factors = new StringBuilder();
        for (long i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                factors.append(i).append(" ");
                n /= i;
            }
        }
        if (n > 1) {
            factors.append(n);
        }
        return factors.toString();
    }

    private String handleNonNumericInput(String s, String message) {
        String output = "";
        if (message.startsWith("listifs")) {
            output = printCommand(new String[]{"ifconfig"});
        } else if (message.startsWith("listports")) {
            output = handleListPortsCommand(s, message);
        } else if (message.startsWith("!")) {
            output = printCommand(message.substring(1).split("\\s+"));
        } else {
            output = message;
        }

        return output;
    }

    private static String printCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String linea;
            StringBuilder series = new StringBuilder();
            while ((linea = reader.readLine()) != null) {
                series.append(linea).append("\n");
            }

            int result = process.waitFor();
            return series.toString();
        } catch (Exception errorConsole) {
            return "The command is incorrect";
        }
    }

    private String handleListPortsCommand(String s, String message) {
        int pos = message.indexOf("listports");
        if (pos != -1) {
            String ip = message.substring(pos + "listports".length()).trim();
            return printCommand(new String[]{"nmap", ip});
        } else {
            return message;
        }
    }

}
