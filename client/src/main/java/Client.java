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
	public static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		List<String> extraArgs = new ArrayList<>();

		try (Communicator communicator = Util.initialize(args, "client.cfg", extraArgs)) {
			Response response;
			PrinterPrx service = PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));

			if (service == null) {
				throw new Error("Invalid proxy");
			}
			String username = System.getProperty("user.name").replace(" ", "").trim();
			String hostname = Inet4Address.getLocalHost().getHostName().trim();
			boolean exit = false;
			String input;

			while (!exit) {
				String prefix = username + ":" + hostname + "=>";
				System.out.println("====================================================================================");
				System.out.print(prefix);
				input = scanner.nextLine();
				long start = System.currentTimeMillis();
				response = service.printString(prefix + input);
				System.out.println("Server response: " + response.value);
				long latency = System.currentTimeMillis() - start;
				if (!input.equalsIgnoreCase("exit")) {
					System.out.println("Processing time: " + response.responseTime + " ms");
					System.out.println("Latency: " + latency + " ms");
					System.out.println("Network Performance: " + (latency - response.responseTime) + " ms");
				}
				exit = input.equalsIgnoreCase("exit");
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}