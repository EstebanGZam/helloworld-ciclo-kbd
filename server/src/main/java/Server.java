import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Server {

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();
        try (Communicator communicator = Util.initialize(args, "server.cfg", extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("too many arguments");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }
            ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            adapter.add(new PrinterI(), Util.stringToIdentity("SimplePrinter"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }

}