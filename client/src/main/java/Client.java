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
			//com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
			Response response;
			PrinterPrx service = PrinterPrx
					.checkedCast(communicator.propertyToProxy("Printer.Proxy"));

			if (service == null) {
				throw new Error("Invalid proxy");
			}
			String username = System.getProperty("user.name");
			String hostname = Inet4Address.getLocalHost().getHostName();
			boolean exit = false;
			String input;

			while (!exit) {
				String prefix = username + ":" + hostname + " ";
				System.out.print(prefix);
				input = scanner.nextLine();
				response = service.printString(prefix + input);
				exit = input.equalsIgnoreCase("exit");
				System.out.println("Server response: " + response.value);
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
}