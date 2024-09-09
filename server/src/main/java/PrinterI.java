import Demo.Response;
import com.zeroc.Ice.Current;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PrinterI implements Demo.Printer {

	public Response printString(String s, Current current) {
		long processTime;
		long start = System.currentTimeMillis();
		System.out.println("====================================================================================");
		System.out.println("Message: " + s);

		String[] msgArray = s.split("=>");

		if (msgArray.length == 1) {
			processTime = System.currentTimeMillis() - start;
			return new Response(processTime, "You sent an empty message, please try again");
		}

		String message = msgArray[1];
		String serverResponse;

		try {
			serverResponse = checkIfNaturalNumber(Integer.parseInt(message));
		} catch (NumberFormatException e) {
			serverResponse = handleNonNumericInput(message);
		}

		System.out.println(serverResponse);

		processTime = System.currentTimeMillis() - start;
		System.out.println("Response time: " + processTime + "ms");
		return new Response(processTime, serverResponse);
	}

	private static String checkIfNaturalNumber(int n) {
		if (n > 0) {
			StringBuilder response = new StringBuilder();

			long[] fibonacciArray = new long[n];
			response.append("\nFibonacci (").append(n).append("): ");
			for (int i = 0; i < n; i++) {
				fibonacciArray[i] = fibonacci(i, new long[n]);
				response.append(fibonacciArray[i]).append(" ");
			}

			response.append("\nPrime factors (").append(n).append("): ");
			response.append(calculatePrimeFactors(n));

			return response.toString();
		} else {
			return String.valueOf(n);
		}
	}

	public static long fibonacci(int num, long[] memo) {
		if (num == 0 || num == 1) {
			return num;
		}
		if (memo[num] == 0) {
			memo[num] = fibonacci(num - 1, memo) + fibonacci(num - 2, memo);
		}
		return memo[num];
	}

	public static String calculatePrimeFactors(int n) {
		StringBuilder factors = new StringBuilder();
		for (int i = 2; i <= n / i; i++) {
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

	private String handleNonNumericInput(String message) {
		String output;
		if (message.startsWith("listifs")) {
			String os = System.getProperty("os.name").toLowerCase();
			output = printCommand(new String[]{os.contains("win") ? "ipconfig" : "ifconfig"});
		} else if (message.startsWith("listports")) {
			output = handleListPortsCommand(message);
		} else if (message.startsWith("!")) {
			output = printCommand(message.substring(1).split("\\s+"));
		} else {
			output = message.equalsIgnoreCase("exit") ? "Thank you for using our services. See you soon!" : message;
		}

		return output;
	}

	private static String printCommand(String[] command) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			StringBuilder series = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				series.append(line).append("\n");
			}
			process.waitFor();
			return series.toString();
		} catch (Exception errorConsole) {
			return "The command is incorrect";
		}
	}

	private String handleListPortsCommand(String message) {
		int pos = message.indexOf("listports");
		if (pos != -1) {
			String ip = message.substring(pos + "listports".length()).trim();
			return printCommand(new String[]{"nmap", ip});
		} else {
			return message;
		}
	}

}
