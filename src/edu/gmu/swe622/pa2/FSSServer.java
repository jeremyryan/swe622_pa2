package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Server for the File Sharing System.
 */
public class FSSServer {

    private ServerSocket serverSocket;
    private boolean running = true;

    /**
     * Starts the server listening on port.
     * @param port the port that the server will listen on
     */
    public void serve(Integer port) {
        try {
            this.serverSocket = new ServerSocket(port);
            while (this.running) {
                Socket clientSocket = this.serverSocket.accept();
                new RequestHandler(clientSocket, this).start();
            }
        } catch (SocketException exp) {
            // thrown when socket is closed by shutdown()
        } catch (IOException exp) {
            exp.printStackTrace();
        } finally {
            if (! (this.serverSocket == null || this.serverSocket.isClosed())) {
                try {
                    this.serverSocket.close();
                } catch (IOException exp) {
                    System.out.println("Could not close socket");
                    exp.printStackTrace();
                }
            }
        }
    }

    /**
     * Shuts down the server. Called by RequestHandler instances when a client sends a shutdown command.
     * @throws IOException
     */
    public void shutdown() throws IOException {
        System.out.println("Server shutting down");
        this.running = false;
        this.serverSocket.close();
    }
}
