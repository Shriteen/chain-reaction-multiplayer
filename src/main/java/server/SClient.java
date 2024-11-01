package server;

import java.net.Socket;
import java.io.*;
import com.google.gson.Gson;

import protocol.*;

/**
 * SClient represents a client on the server side
 */
public class SClient extends Thread{

    // The socket connecting to client
    private Socket socket;
    // Input Output streams to read/write from/to socket
    private BufferedReader reader;
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
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        start();
    }

    public void run() {
        System.out.println("connected to "+socket+ " Client ID:"+clientId+ " Username:"+username);

        try {
            //send hello to client to convey its id
            send(new ServerHello(clientId));
            
            while (active) {
                try {
                    //Send ping as test for communication,
                    //will give error in case other side is closed
                    send(new Ping());
                    if(writer.checkError()){
                        active=false;
                        continue;                    
                    }
                    
                    String jsonMessage=reader.readLine();
                    if(jsonMessage==null)
                        continue;

                    Message message=new Gson().fromJson(jsonMessage, Message.class);

                    // The heart of thread which handles incoming messages
                    // Handle generic connection related messages
                    switch(message.messageType){
                        
                    case "PING":
                        System.out.println("Received ping from "+clientId);
                        break;
                    case "ERROR_MESSAGE":
                        ErrorMessage errorMessage= new Gson().fromJson(jsonMessage, ErrorMessage.class);
                        System.out.println(errorMessage.getErrorDisplayString());
                        break;
                    case "EXIT":
                        active=false;
                        break;
                        
                    // The messages specific to game will be passed to handler method
                    // Note we need to list down all the game specific messages here to pass them
                    case "CLIENT_HELLO":
                        handleMessages(message.messageType, jsonMessage);
                        break;
                        
                    default:
                        System.out.println("Received unknown message type: "+message.messageType);
                    }
                    
                }
                catch (IOException e) {
                    active=false;
                }
            }
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        } finally {
            
            try {
                // Close socket after use
                socket.close();
                System.out.println("Closed socket; clientId: "+clientId);
            }
            catch (Throwable e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // handle game related messages 
    private void handleMessages(String messageType, String jsonMessage) {
        // Handle game related messages;
        // Do not forget to update switch-cases in run() so that this method is called!
        switch(messageType){
            
        case "CLIENT_HELLO":
            ClientHello message= new Gson().fromJson(jsonMessage, ClientHello.class);
            username= message.userName;
            System.out.println("Client "+clientId+" updated their username to "+username);
            break;
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
