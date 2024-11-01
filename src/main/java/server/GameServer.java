package server;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import gamemodel.GameModel;
import protocol.*;

/**
 * The server of the game
 */
public class GameServer extends Server {

    private static final Random RNG= new Random();

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
        try {
            // If count exceeds maxCount then reject new clients 
            if(clientList.size() < maxPlayers)
                clientList.add(new SClient(sock, this, getUniqueRandomId()));
            else{
                System.out.println("Refusing connection to "+ sock +" as count exceeds "+maxPlayers);

                //send refused message
                (new PrintWriter(sock.getOutputStream(), true)).println(
                    new ErrorMessage(ErrorMessage.Code.CONN_REFUSED,
                                     "Max player limit reached!"
                        ).toString()
                    );
                
                sock.close();
            }
        }
        catch (IOException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMaxPlayers(int count) {
        maxPlayers= count;
    }

    // Returns a random integer which is not already allotted to any client
    private int getUniqueRandomId() {
        int randomValue = RNG.nextInt(1000);

        // 0 is used as magic number and is invalid id
        if(randomValue==0 || clientList.stream().anyMatch(sc -> sc.getClientId() == randomValue))
            return getUniqueRandomId();                // if not valid just try again

        return randomValue;
    }
}
