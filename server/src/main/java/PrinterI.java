import Demo.Response;
import com.zeroc.Ice.Current;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PrinterI implements Demo.Printer {

	// Método principal que recibe un mensaje (s), lo procesa y devuelve una respuesta de tipo Response
	public Response printString(String s, Current current) {
		long processTime; // Variable para almacenar el tiempo de procesamiento
		long start = System.currentTimeMillis(); // Registra el tiempo de inicio del procesamiento

		System.out.println("====================================================================================");
		System.out.println("Message: " + s); // Imprime el mensaje recibido

		// Divide el mensaje en dos partes separadas por "=>"
		String[] msgArray = s.split("=>");

		// Si solo hay una parte, significa que el mensaje está vacío o malformado
		if (msgArray.length == 1) {
			processTime = System.currentTimeMillis() - start; // Calcula el tiempo de procesamiento
			return new Response(processTime, "You sent an empty message, please try again"); // Devuelve un mensaje de error
		}

		String message = msgArray[1]; // La segunda parte del mensaje es la que se procesa
		String serverResponse; // Respuesta del servidor

		try {
			// Intenta convertir el mensaje a un número y calcula si es un número natural
			serverResponse = checkIfNaturalNumber(Integer.parseInt(message));
		} catch (NumberFormatException e) {
			// Si el mensaje no es un número, maneja la entrada no numérica
			serverResponse = handleNonNumericInput(message);
		}

		System.out.println(serverResponse); // Imprime la respuesta del servidor

		processTime = System.currentTimeMillis() - start; // Calcula el tiempo total de procesamiento
		System.out.println("Response time: " + processTime + "ms"); // Imprime el tiempo de respuesta
		return new Response(processTime, serverResponse); // Devuelve la respuesta final con el tiempo de procesamiento
	}

	// Método para verificar si un número es natural y generar una secuencia de Fibonacci y factores primos
	private static String checkIfNaturalNumber(int n) {
		if (n > 0) { // Verifica si el número es mayor que cero
			StringBuilder response = new StringBuilder(); // Builder para generar la respuesta

			// Genera una secuencia de Fibonacci
			long[] fibonacciArray = new long[n];
			response.append("\nFibonacci (").append(n).append("): ");
			for (int i = 0; i < n; i++) {
				fibonacciArray[i] = fibonacci(i, new long[n]); // Llama al método recursivo de Fibonacci
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
	public static long fibonacci(int num, long[] memo) {
		if (num == 0 || num == 1) { // Casos base: Fibonacci de 0 es 0, y Fibonacci de 1 es 1
			return num;
		}
		if (memo[num] == 0) { // Si no se ha calculado antes, lo calcula
			memo[num] = fibonacci(num - 1, memo) + fibonacci(num - 2, memo); // Guarda el resultado en el arreglo
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
		if (message.startsWith("listifs")) { // Comando "listifs" para listar interfaces de red
			String os = System.getProperty("os.name").toLowerCase(); // Obtiene el nombre del sistema operativo
			output = printCommand(new String[]{os.contains("win") ? "ipconfig" : "ifconfig"}); // Ejecuta el comando correspondiente según el sistema operativo
		} else if (message.startsWith("listports")) { // Comando "listports" para listar puertos
			output = handleListPortsCommand(message); // Llama al método para manejar los puertos
		} else if (message.startsWith("!")) { // Comando de shell, indicado por "!"
			output = printCommand(message.substring(1).split("\\s+")); // Ejecuta el comando shell
		} else {
			// Si el mensaje es "exit", devuelve un mensaje de despedida; de lo contrario, devuelve el mensaje tal cual
			output = message.equalsIgnoreCase("exit") ? "Thank you for using our services. See you soon!" : message;
		}

		return output; // Devuelve el resultado
	}

	// Método para ejecutar comandos de shell
	private static String printCommand(String[] command) {
		try {
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
		} catch (Exception errorConsole) {
			// Si hay un error, se devuelve una excepción que indica que la solicitud no se procesó correctamente
			throw new RuntimeException(errorConsole);
		}
	}

	// Método para manejar el comando "listports", que utiliza nmap para escanear puertos
	private String handleListPortsCommand(String message) {
		int pos = message.indexOf("listports"); // Encuentra la posición del comando "listports"
		if (pos != -1) {
			String ip = message.substring(pos + "listports".length()).trim(); // Extrae la dirección IP del mensaje
			return printCommand(new String[]{"nmap", ip}); // Ejecuta el comando nmap con la IP
		} else {
			return message; // Si no se encuentra una IP, devuelve el mensaje tal cual
		}
	}

}
