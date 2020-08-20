package server;

import common.ChatterboxProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Base Server Runnable. Opens a server socket on the 'ChatterboxProtocol' specified
 * network port, and creates a new 'ServerBase' to coordinate between the threads
 */
public class ChatterboxServer {

    /**
     * Run this to start the server
     *
     * Creates an outwards facing 'ServerSocket' on the port specified in 'ChatterboxProtocol', and then creates
     * a new serverBase
     *
     * @param args Do nothing
     * @throws IOException Thrown if unable to connect on the ChatterboxProtocol port
     */
    public static void main (String[] args) throws IOException {
        ServerSocket server = new ServerSocket (ChatterboxProtocol.PORT);
        ServerBase serverBase = new ServerBase ();

        while (true) {
            try {
                Socket client = server.accept ();
                System.out.println("New User connected");
                ClientConnection connection = new ClientConnection (client, serverBase);
                System.out.println ("New Thread Created");
                new Thread (connection).start ();
                System.out.println ("Thread started\n");

            } catch (IOException e){
                // Triggered if there is an error on opening connection to a client
                System.out.println("ChatterboxServer: Error in opening a connection, dropped connection and " +
                        "carrying on regardless");
            } catch (Exception e){
                e.printStackTrace ();
            }
        }

    }

}
