package server;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;

import protocol.*;

/**
 * SClient represents a client on the server side
 */
public class SClient extends Thread{

    // The socket connecting to client
    private Socket socket;
    // Output stream to write to socket
    private PrintWriter writer;

    // Reference to game server
    private GameServer server;

    // Represents whether client is actively connected 
    private boolean active;

    // Client info
    private final int clientId;
    private String username;
    
    // initialise socket and start communication in a new thread
    SClient(Socket socket, GameServer server, int id) throws IOException{
        this.socket = socket;
        this.server = server;
        active = true;
        clientId=id;
        username = String.valueOf(id);
        
        writer = new PrintWriter(socket.getOutputStream(), true);
        start();
    }

    public void run() {
        //TODO: Actual implementation
        System.out.println("connected to "+socket+ " Client ID:"+clientId+ " Username:"+username);

        try {
            //Send ping as test for communication
            send(new Ping());
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

        
    }

    public void send(Message message) throws IOException {
        writer.println(message.toString());
    }

    public boolean isActive() {
        return active;
    }

    public int getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    // set active to false i.e. do not consider client to be useful anymore. Like a close()
    public void deactivate() {
        active = false;
    }
}
