import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

// Clase principal Server, que actúa como servidor en la arquitectura Ice
public class Server {

	// Método main: punto de entrada del servidor
	public static void main(String[] args) {
		// Lista para almacenar argumentos extra que se puedan pasar durante la ejecución del servidor
		java.util.List<String> extraArgs = new java.util.ArrayList<>();

		// Bloque try-with-resources para inicializar el comunicador Ice y garantizar su cierre
		try (Communicator communicator = Util.initialize(args, "server.cfg", extraArgs)) {

			// Si hay argumentos adicionales no reconocidos, imprime un error
			if (!extraArgs.isEmpty()) {
				System.err.println("too many arguments");
				// Muestra los argumentos no reconocidos
				for (String v : extraArgs) {
					System.out.println(v);
				}
			}

			// Crea un adaptador de objetos Ice, usando el nombre "Printer" definido en la configuración
			ObjectAdapter adapter = communicator.createObjectAdapter("Printer");

			// Añade un objeto de tipo PrinterI al adaptador, asociándolo con la identidad "SimplePrinter"
			adapter.add(new PrinterI(), Util.stringToIdentity("SimplePrinter"));

			// Activa el adaptador para empezar a aceptar solicitudes
			adapter.activate();

			// El servidor queda a la espera de que se le envíen solicitudes
			communicator.waitForShutdown();
		}
	}

}
