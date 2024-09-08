import java.io.*;


public class Server {
	public static void main(String[] args) {
		java.util.List<String> extraArgs = new java.util.ArrayList<>();

		try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "server.cfg", extraArgs)) {
			if (!extraArgs.isEmpty()) {
				System.err.println("too many arguments");
				for (String v : extraArgs) {
					System.out.println(v);
				}
			}
			com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
			com.zeroc.Ice.Object object = new PrinterI();
			adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
			adapter.activate();
			communicator.waitForShutdown();
		}
	}

	public static void f(String m) {
		String str;
		StringBuilder output = new StringBuilder();

		try {
			Process p = Runtime.getRuntime().exec(m);

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((str = br.readLine()) != null)
				output.append(str).append(System.lineSeparator());
			br.close();
		} catch (Exception ignored) {
		}
	}

}