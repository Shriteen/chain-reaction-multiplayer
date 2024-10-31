package server;

import java.net.Socket;

/**
 * SClient represents a client on the server side
 */
public class SClient extends Thread{

    // The socket connecting to client
    private Socket socket;

    // initialise socket and start communication in a new thread
    SClient(Socket socket) {
        this.socket = socket;

        start();
    }

    public void run() {
        //TODO: Actual implementation
        System.out.println("connected to "+socket);
    }
}
