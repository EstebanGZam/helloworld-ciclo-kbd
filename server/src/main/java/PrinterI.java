import Demo.Response;
import com.zeroc.Ice.Current;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class PrinterI implements Demo.Printer {

	// Formateador de decimales para mostrar los resultados con dos decimales
	private final DecimalFormat df = new DecimalFormat("#.00");

	// Método principal que recibe un mensaje (s), lo procesa y devuelve una respuesta de tipo Response
	public Response printString(String s, Current current) {
		long processTime; // Variable para almacenar el tiempo de procesamiento
		long start = System.currentTimeMillis(); // Registra el tiempo de inicio del procesamiento

		// Incrementa el contador de solicitudes totales en el servidor
		Server.setTotalRequests(Server.getTotalRequests() + 1);

		// Imprime un separador para cada nueva solicitud
		System.out.println("====================================================================================");
		System.out.println("Message: " + s); // Imprime el mensaje recibido

		// Divide el mensaje en dos partes separadas por "=>"
		String[] msgArray = s.split("=>");

		// La segunda parte del mensaje es la que se procesa
		String message = msgArray[1];
		String serverResponse; // Respuesta del servidor

		try {
			// Intenta convertir el mensaje a un número y verifica si es un número natural
			serverResponse = checkIfNaturalNumber(Integer.parseInt(message));
		} catch (NumberFormatException e) {
			// Si el mensaje no es un número, maneja la entrada no numérica
			serverResponse = handleNonNumericInput(message);
		}

		System.out.println(serverResponse); // Imprime la respuesta del servidor

		// Calcula el tiempo total de procesamiento
		processTime = System.currentTimeMillis() - start;

		// Incrementa el contador de solicitudes resueltas en el servidor
		Server.setResolvedRequests(Server.getResolvedRequests() + 1);

		// Acumula el tiempo total de procesamiento en el servidor
		Server.setProcessTime(Server.getProcessTime() + processTime);

		// Devuelve la respuesta final con el tiempo de procesamiento, throughput y tasa de solicitudes no procesadas
		return new Response(processTime, calculateThroughput(), calculateUnprocessRate(), serverResponse);
	}

	// Método para calcular la tasa de solicitudes no procesadas
	private double calculateUnprocessRate() {
		// Calcula la tasa de solicitudes no procesadas como porcentaje
		double unprocessRate = (Server.getTotalRequests() - Server.getResolvedRequests()) / (double) Server.getTotalRequests() * 100;
		System.out.println("Unprocess Rate: " + df.format(unprocessRate) + " %");
		return unprocessRate;
	}

	// Método para calcular el throughput (número de solicitudes por segundo)
	private double calculateThroughput() {
		// Si el tiempo de procesamiento total es cero, el throughput es NaN, de lo contrario, se calcula el throughput
		double throughput = Server.getProcessTime() == 0 ? Double.NaN : Server.getTotalRequests() / ((double) Server.getProcessTime() / 1000.0);
		System.out.println("Throughput: " + df.format(throughput) + " request/s");
		return throughput;
	}

	// Método para verificar si un número es natural y generar una secuencia de Fibonacci y factores primos
	private static String checkIfNaturalNumber(int n) {
		if (n > 0) { // Verifica si el número es mayor que cero
			StringBuilder response = new StringBuilder(); // Builder para generar la respuesta

			// Genera una secuencia de Fibonacci
			BigInteger[] fibonacciArray = new BigInteger[n];
			response.append("\nFibonacci (").append(n).append("): ");
			for (int i = 0; i < n; i++) {
				fibonacciArray[i] = fibonacci(i, new BigInteger[n]); // Llama al método recursivo de Fibonacci
				response.append(fibonacciArray[i]).append(" "); // Agrega el número de Fibonacci a la respuesta
			}

			// Calcula los factores primos del número
			response.append("\nPrime factors (").append(n).append("): ");
			response.append(calculatePrimeFactors(n)); // Agrega los factores primos a la respuesta

			return response.toString(); // Devuelve la respuesta completa
		} else {
			return String.valueOf(n); // Si el número es cero o negativo, devuelve el número como está
		}
	}

	// Método recursivo para calcular el número de Fibonacci
	public static BigInteger fibonacci(int num, BigInteger[] memo) {
		if (num == 0 || num == 1) { // Casos base: Fibonacci de 0 es 0, y Fibonacci de 1 es 1
			return BigInteger.valueOf(num);
		}
		if (memo[num] == null) { // Si no se ha calculado antes, lo calcula
			memo[num] = fibonacci(num - 1, memo).add(fibonacci(num - 2, memo)); // Guarda el resultado en el arreglo
		}
		return memo[num]; // Devuelve el valor de Fibonacci
	}

	// Método para calcular los factores primos de un número
	public static String calculatePrimeFactors(int n) {
		StringBuilder factors = new StringBuilder(); // Builder para construir la lista de factores
		for (int i = 2; i <= n / i; i++) { // Itera desde 2 hasta la raíz cuadrada de n
			while (n % i == 0) { // Si el número es divisible por i
				factors.append(i).append(" "); // Agrega i como factor
				n /= i; // Divide n entre i
			}
		}
		if (n > 1) { // Si el número restante es mayor que 1, también es un factor primo
			factors.append(n);
		}
		return factors.toString(); // Devuelve la lista de factores primos
	}

	// Método para manejar entradas no numéricas
	private String handleNonNumericInput(String message) {
		String output; // Variable para almacenar el resultado
		try {
			if (message.startsWith("listifs")) { // Comando "listifs" para listar interfaces de red
				String os = System.getProperty("os.name").toLowerCase(); // Obtiene el nombre del sistema operativo
				output = printCommand(new String[]{os.contains("win") ? "ipconfig" : "ifconfig"}); // Ejecuta el comando correspondiente según el sistema operativo
			} else if (message.startsWith("listports")) { // Comando "listports" para listar puertos
				output = handleListPortsCommand(message); // Llama al método para manejar los puertos
			} else if (message.startsWith("!")) { // Comando de shell, indicado por "!"
				output = printCommand(message.substring(1).split("\\s+")); // Ejecuta el comando shell
			} else {
				throw new Exception("Invalid command: " + message);
			}

		} catch (Exception errorConsole) {
			// Si hay un error, se devuelve una excepción que indica que la solicitud no se procesó correctamente
			throw new RuntimeException(errorConsole.getMessage());
		}
		return output; // Devuelve el resultado
	}

	// Método para ejecutar comandos de shell
	private static String printCommand(String[] command) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder(command); // Crea un nuevo proceso con el comando
		processBuilder.redirectErrorStream(true); // Redirige los errores a la salida estándar

		Process process = processBuilder.start(); // Inicia el proceso
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); // Lee la salida del proceso

		String line; // Variable para almacenar las líneas de salida
		StringBuilder series = new StringBuilder(); // Builder para construir la salida completa
		while ((line = reader.readLine()) != null) { // Lee cada línea de salida
			series.append(line).append("\n"); // Agrega cada línea a la salida
		}
		process.waitFor(); // Espera a que el proceso termine
		return series.toString(); // Devuelve la salida completa del comando
	}

	// Método para manejar el comando "listports", que utiliza nmap para escanear puertos
	private String handleListPortsCommand(String message) throws Exception {
		int pos = message.indexOf("listports"); // Encuentra la posición del comando "listports"
		if (pos != -1) {
			String ip = message.substring(pos + "listports".length()).trim(); // Extrae la dirección IP del mensaje
			System.out.println(ip);
			return printCommand(new String[]{"nmap", ip}); // Ejecuta el comando nmap con la IP
		} else {
			return message; // Si no se encuentra una IP, devuelve el mensaje tal cual
		}
	}

}
