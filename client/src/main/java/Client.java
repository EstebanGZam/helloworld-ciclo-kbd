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

	// Scanner para leer la entrada del usuario desde la consola
	private static final Scanner scanner = new Scanner(System.in);

	// Contador para el número de solicitudes exitosas
	private static int successfulRequests = 0;

	// Contador para el número total de solicitudes
	private static int totalRequests = 0;

	public static void main(String[] args) {

		// Lista de argumentos extra que pueden pasarse durante la inicialización
		List<String> extraArgs = new ArrayList<>();
		// Lista para almacenar las latencias
		List<Long> latencies = new ArrayList<>();

		// Bloque try-with-resources para garantizar que el comunicador se cierre correctamente
		try (Communicator communicator = Util.initialize(args, "client.cfg", extraArgs)) {
			Response response; // Variable para almacenar la respuesta del servidor
			// Crea una instancia del servicio proxy del servidor usando el nombre de proxy configurado
			PrinterPrx service = PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy"));

			// Verifica si el proxy es válido, si no, lanza un error
			if (service == null) {
				throw new Error("Invalid proxy");
			}

			// Obtiene el nombre de usuario del sistema, elimina espacios y lo recorta
			String username = System.getProperty("user.name").replace(" ", "").trim();
			// Obtiene el hostname de la máquina cliente
			String hostname = Inet4Address.getLocalHost().getHostName().trim();

			boolean exit = false; // Variable de control para el bucle principal
			String input; // Variable para almacenar la entrada del usuario

			// Bucle principal que sigue ejecutándose hasta que el usuario ingrese "exit"
			while (!exit) {
				// Crea el prefijo que incluye el nombre de usuario y el hostname
				String prefix = username + ":" + hostname + "=>";
				System.out.println("====================================================================================");
				System.out.print(prefix); // Imprime el prefijo en la consola
				input = scanner.nextLine(); // Lee la entrada del usuario

				long start = System.currentTimeMillis(); // Registra el tiempo de inicio para calcular la latencia
				try {
					// Envía el mensaje al servidor concatenado con el prefijo y recibe la respuesta
					response = service.printString(prefix + input);

					// Imprime la respuesta del servidor en la consola
					System.out.println("Server response: " + response.value);

					long latency = System.currentTimeMillis() - start; // Calcula la latencia total de la operación
					latencies.add(latency); // Almacena la latencia en la lista

					// Si el usuario no ingresó "exit", muestra el tiempo de procesamiento y la latencia
					if (!input.equalsIgnoreCase("exit")) {
						System.out.println("Processing time: " + response.responseTime + " ms"); // Tiempo de procesamiento del servidor
						System.out.println("Latency: " + latency + " ms"); // Latencia total
						// Calcula el rendimiento de la red restando el tiempo de procesamiento del tiempo total
						System.out.println("Network Performance: " + (latency - response.responseTime) + " ms");
						// Calcula el jitter si hay al menos dos latencias
						if (latencies.size() > 1) {
							long jitter = 0;
							for (int i = 1; i < latencies.size(); i++) {
								jitter += Math.abs(latencies.get(i) - latencies.get(i - 1));
							}
							jitter /= (latencies.size() - 1); // Promedio del jitter
							System.out.println("Jitter: " + jitter + " ms");
						}
						successfulRequests++;
						totalRequests++;
						showFailureStatistics();
					}
				} catch (RuntimeException e) {
					totalRequests++;
					System.err.println("Request could not be processed");
					showFailureStatistics();
				}

				// Si el usuario ingresó "exit", finaliza el bucle
				exit = input.equalsIgnoreCase("exit");
			}

			// Captura y lanza una excepción en caso de que ocurra un error al obtener el hostname
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private static void showFailureStatistics() {
		// Calcular la Tasa de Pérdida (Missing Rate)
		double missingRate = (double) (totalRequests - successfulRequests) / totalRequests * 100;
		System.out.println("Missing Rate: " + missingRate + " %");
	}
}
