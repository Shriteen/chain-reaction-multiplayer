package client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import com.google.gson.Gson;

import gamemodel.GameModel;
import protocol.*;


/**
 * GameClient is used to connect and communicate to server
 */
public class GameClient extends Client{
    
    // state of client 
    enum State{ CREATED , CONNECTED, STARTED };
    private State state=State.CREATED;
    
    // Client info
    private int ID;
    private final String username;

    // State of game on client side
    private GameModel model;
    
    // Constructor accepts the port on which to listen
    public GameClient(String ipAddress, String username) throws IOException{
        super( new Socket(InetAddress.getByName(ipAddress),45678) , false); // client might wait for some time before discovering its disconnected
        state = State.CONNECTED;
        this.username= username;

        start();
    }

    protected void preLoopHook() throws Throwable {
        //send hello to server to convey its username
        send(new ClientHello(username));
    }

    protected void postLoopHook() throws Throwable {
        // Close socket after use
        socket.close();
        System.out.println("Socket closed");
    }
    
    protected void handleMessages(String messageType, String jsonMessage) {
        switch(messageType){
            
        case "SERVER_HELLO":
            ServerHello message= new Gson().fromJson(jsonMessage, ServerHello.class);
            ID = message.id;
            System.out.println("Received id: "+ID);
            break;
        default:
            System.out.println("Received unknown message type: "+ messageType);
        }
    }
    
    public int getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public GameModel getGameState() {
        return model;
    }
    
    //Change state to Started
    public void setStarted() {
        state = State.STARTED;
    }   
}

