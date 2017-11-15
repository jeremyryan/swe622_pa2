package edu.gmu.swe622.pa2;

import java.rmi.Naming;
import java.rmi.Remote;
import java.util.stream.Stream;

/**
 * Main executable class for starting server or client programs.
 */
public class Main {

    /**
     * Prints the command line options that the program supports and exits with a return status of 0.
     */
    private static void printUsage() {
        Stream.of(
                "Usage:",
                "server start <portnumber>",
                "client upload <path_on_client> </path/filename/on/server>",
                "client download </path/existing_filename/on/server> <path_on_client>",
                "client dir <path/existing_directory/on/server>",
                "client mkdir </path/new_directory/on/server>",
                "client rmdir <path/existing_directory/on/server>",
                "client rm </path/existing_filename/on/server>",
                "client shutdown"
        ).forEach(System.out::println);
        System.exit(0);
    }

    /**
     * Main method to start server and client from the command line.
     * Verifies that the arguments passed are correct, in which case it hands control to the appropriate
     * class. Otherwise prints usage information and exists. If an exception is thrown while processing the request,
     * an error message is printed and the process exits with a return status of 1.
     * @param args  command line arguments
     */
    public static void main(String[] args) {
        if (args.length <= 1) {
            printUsage();
        }

        if ("server".equalsIgnoreCase(args[0])) {
            if (! "start".equalsIgnoreCase(args[1]) || args.length < 3) {
                printUsage();
            }

            String portParam = args[2];
            Integer port = null;
            try {
                port = Integer.valueOf(portParam);
            } catch (NumberFormatException exp) {
                printUsage();
            }
            String registeredName = "rmi://localhost:" + port + "/fss";
            try  {
                //System.setSecurityManager(new RMISecurityManager());
                Naming.rebind(registeredName, (Remote) new FSSServer());
            } catch (Exception exp) {
                System.out.println("FSS server encountered an error");
                exp.printStackTrace();
                System.exit(1);
            }

        } else if ("client".equalsIgnoreCase(args[0])) {
            try {
                String serverVar = System.getenv("PA2_SERVER");
                if (serverVar == null) {
                    throw new IllegalStateException("environment variable PA2_SERVER must be set");
                }
                String[] serverVarItems = serverVar.split(":");
                if (serverVarItems.length != 2) {
                    throw new IllegalStateException("make sure PA2_SERVER environment variable is set: hostname:port");
                }
                String hostName = serverVarItems[0];
                String portParam = serverVarItems[1];
                if (hostName == null) {
                    throw new IllegalStateException("no hostname could be found; make sure PA2_SERVER is set: hostname:port");
                }
                if (portParam == null) {
                    throw new IllegalStateException("no port could be found; make sure PA2_SERVER is set: hostname:port");
                }
                Integer port = Integer.valueOf(portParam);
                String actionName = args[1];
                Action action = Action.findByName(actionName);

                if (action == null || args.length-2 < action.getNumArgs()) {
                    printUsage();
                }

                String[] commandArgs = new String[action.getNumArgs()];
                for (int i = 0; i < action.getNumArgs(); i++) {
                    commandArgs[i] = args[i+2];
                }
                new FSSClient(hostName, port).doAction(action, commandArgs);
            } catch (Exception exp) {
                System.err.println(exp.getMessage());
                //exp.printStackTrace();
                System.exit(1);
            }
        } else {
            printUsage();
        }
    }
}
