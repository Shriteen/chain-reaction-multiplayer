package server;

import java.net.Socket;
import java.io.*;
import com.google.gson.Gson;

import client.Client;
import protocol.*;

/**
 * SClient represents a client on the server side
 */
public class SClient extends Client{
    
    // Reference to game server
    private GameServer server;

    // Client info
    private final int clientId;
    private String username;
    
    // initialise socket and start communication in a new thread
    SClient(Socket socket, GameServer server, int id) throws IOException{
        super(socket, true);    // server should quickly update about lost connections

        this.server = server;
        
        clientId=id;
        username = String.valueOf(id);
        
        start();
    }

    protected void preLoopHook() throws Throwable {
        System.out.println("connected to "+socket+ " Client ID:"+clientId+ " Username:"+username);
        
        //send hello to client to convey its id
        send(new ServerHello(clientId));
    }

    protected void postLoopHook() throws Throwable {
        // Close socket after use
        socket.close();
        System.out.println("Closed socket; clientId: "+clientId);
    }    

    // handle game related messages 
    protected void handleMessages(String messageType, String jsonMessage){
        
        switch(messageType){
            
        case "CLIENT_HELLO":
            ClientHello message= new Gson().fromJson(jsonMessage, ClientHello.class);
            username= message.userName;
            System.out.println("Client "+clientId+" updated their username to "+username);
            break;
        default:
            System.out.println("Received unknown message type: "+ messageType);
        }
    }
    
    public int getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }


}
