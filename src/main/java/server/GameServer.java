package server;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import gamemodel.GameModel;
import gamemodel.Player;
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

    // Thread which runs game loop
    GameLoop gameLoop;
    
    public GameServer() throws IOException{
        super(45678);
        clientList= new ArrayList<SClient>();


        // Thread to clean inactive clients
        new CleanDaemon();
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

    //Changing maxPlayers is allowed only in LISTENING state
    public void setMaxPlayers(int count) throws IllegalStateException{
        if(state==Server.State.LISTENING)
            maxPlayers= count;
        else
            throw new IllegalStateException("Changing max players is allowed only during server is in LISTENING state!");
    }

    public GameModel getGameState() {
        return model;
    }
    
    // Returns a random integer which is not already allotted to any client
    private int getUniqueRandomId() {
        int randomValue = RNG.nextInt(1000);

        // 0 is used as magic number and is invalid id
        if(randomValue==0 || clientList.stream().anyMatch(sc -> sc.getClientId() == randomValue))
            return getUniqueRandomId();                // if not valid just try again

        return randomValue;
    }

    // Start the game
    public void startGame(int rows, int cols) {

        ArrayList<Player> players=new ArrayList<Player>();

        synchronized(clientList){
            for (int i=0; i<clientList.size() && i<maxPlayers; i++) {
                if(clientList.get(i).isActive()){
                    players.add(new Player(
                                    clientList.get(i).getClientId(),
                                    clientList.get(i).getUsername() ,
                                    getColorForIndex(i)
                                    ) );
                }
            }
        }
        
        model = new GameModel(rows, cols, players );

        //start loop thread
        gameLoop=new GameLoop();
    }
    
    // Daemon thread which removes inactive clients from clientList
    private class CleanDaemon extends Thread {
        CleanDaemon() {
            this.start();
        }
        
        public void run() {
            // Loop every 1 seconds
            while(state!= Server.State.STOPPED){
                try {
                    Thread.sleep(1000);
                    
                    synchronized(clientList)
                    {
                        // If game has started, also need to remove from game
                        if(state == Server.State.STARTED) {
                            for(SClient sc : clientList) {
                                if(!sc.isActive()){
                                    model.removePlayer(sc.getClientId());
                                }
                            }
                        }
                        
                        clientList.removeIf(sc-> !sc.isActive());
                    }

                    // Remove players if exceeding latest max players limit even after removing inactive
                    if(clientList.size() > maxPlayers){
                        synchronized(clientList){
                            for(int i=maxPlayers; i<clientList.size(); i++) {                                
                                try {
                                    clientList.get(i).send(
                                        new ErrorMessage(ErrorMessage.Code.CONN_REFUSED,
                                                         "Max player limit changed" ) );
                                }
                                catch (IOException e) {
                                    System.out.println("Error " + e.getMessage());
                                    e.printStackTrace();
                                }
                                
                                clientList.get(i).deactivate();
                            }
                            clientList.subList(maxPlayers,clientList.size()).clear();
                        }
                    }
                    
                }
                catch (InterruptedException e) {
                    System.out.println("CleanDaemon interrupted");
                }
            }
        }   
    }

    // Thread for loop in which game is played
    class GameLoop extends Thread {
        GameLoop() {
            state= Server.State.STARTED;
            this.start();
        }
        
        public void run() {
            // TODO: game loop implementation
            System.out.println("game is "+state.name());
            model.print();
        }   
    }
    
    // Utility method to returns a hex color prioritizing basic colors
    static private String getColorForIndex(int i) {
        final String[] colors= { "FFFF00", "00FFFF", "FF00FF",
                                 "FF0000", "00FF00", "0000FF",
                                 "FFFFFF", "FFA500" };
        if (i<colors.length) {
            return colors[i];
        }else{
            //Generate random color
            int red,green,blue;
            while(true){
                red = RNG.nextInt(255);
                green= RNG.nextInt(255);
                blue= RNG.nextInt(255);

                //should be bright
                if((red+green+blue)/3 > 127)
                    break;
            }

            return Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
        }
    }

    // Sends game model to all clients
    private void sendStateToAll() {
        synchronized(clientList){
            for(var client: clientList){
                try {
                    if(client.isActive())
                        client.send(new GameState(model));
                }
                catch (IOException e) {
                    System.out.println("Failed to send game state to "+ client.getClientId());
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // Sends your turn message to the player having current turn
    private void sendYourTurnMessage() {
        int currentPlayerId = model.currentTurnOfPlayer().id();
        SClient currentPlayerClient = clientList.stream()
                                                .filter(sc -> sc.getClientId()==currentPlayerId)
                                                .findFirst()
                                                .get();
        try {
            currentPlayerClient.send(new YourTurn());
        }
        catch (IOException e) {
            System.out.println("Failed to send your turn message to "+ currentPlayerClient.getClientId());
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

    }
    
}
