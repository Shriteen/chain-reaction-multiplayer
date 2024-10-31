package server;

import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import gamemodel.GameModel;

/**
 * The server of the game
 */
public class GameServer extends Server {

    // Maximum number of players allowed to connect
    int maxPlayers = 8;
    
    // List of clients connected to server
    ArrayList<SClient> clientList;

    // Model representing game state
    GameModel model;
    
    public GameServer() throws IOException{
        super(45678);
        clientList= new ArrayList<SClient>();
    }

    // methods which handles each incoming connection
    void clientHandler(Socket sock) {
        
        // If count exceeds maxCount then reject new clients 
        if(clientList.size() < maxPlayers)
            clientList.add(new SClient(sock));
        else{
            //TODO: send refuse response to client
            System.out.println("Refusing connection to "+ sock +" as count exceeds "+maxPlayers);
            
            try {
                sock.close();
            }
            catch (IOException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setMaxPlayers(int count) {
        maxPlayers= count;
    }
}
