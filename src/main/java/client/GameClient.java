package client;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.function.Consumer;
import com.google.gson.Gson;

import gamemodel.GameModel;
import gamemodel.Player;
import protocol.*;


/**
 * GameClient is used to connect and communicate to server
 */
public class GameClient extends Client{
    
    // state of client 
    enum State{ CREATED , CONNECTED, STARTED, OVER };
    private State state=State.CREATED;
    
    // Client info
    private int ID;
    private final String username;

    // State of game on client side
    private GameModel model;

    // Event function which is fired when new state is received
    // Accepts GameModel as argument
    private Consumer<GameModel> gameStateReceivedHandler = gameModel -> {
        System.out.println("Game State Received");      // Default implementation is to log to console
    };

    // Event function which is fired when your turn message is received
    private Runnable yourTurnHandler = () -> {
        System.out.println("Your Turn");
    };

    // Event function which is fired when game over is received
    // Accepts winning Player as argument
    private Consumer<Player> gameOverReceivedHandler = winner -> {
        System.out.println("Game over!");      // Default implementation is to log to console
        System.out.println("Winner is "+winner.name()+" having id:"+winner.id());
    };
    
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
            ServerHello helloMessage= new Gson().fromJson(jsonMessage, ServerHello.class);
            ID = helloMessage.id;
            System.out.println("Received id: "+ID);
            break;
        case "GAME_STATE":
            model = new Gson().fromJson(jsonMessage, GameState.class).state;
            gameStateReceivedHandler.accept(model);
            setStarted();
            break;
        case "YOUR_TURN":
            yourTurnHandler.run();
            break;
        case "GAME_OVER":
            gameOverReceivedHandler.accept(
                new Gson().fromJson(jsonMessage, GameOver.class).winner
                );
            state= State.OVER;
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

    // To add event handler for game state received
    public void onGameStateReceived(Consumer<GameModel> handler) {
        gameStateReceivedHandler= handler;
    }

    // To add event handler for your turn message
    public void onYourTurnReceived(Runnable handler) {
        yourTurnHandler = handler;
    }

    // To add event handler for game over received
    public void onGameOverReceived(Consumer<Player> handler) {
        gameOverReceivedHandler= handler;
    }
    
    // Send make move message; row and col are position of move
    public void makeMove(int row, int col) {
        if(state==State.STARTED){
            if(model.currentTurnOfPlayer().id()== ID){
                try {
                    send(new MakeMove(row,col));
                }
                catch (IOException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }

            }else
                System.out.println("Error: Attempt to play when it's not my turn!");
        }else
            System.out.println("Error: Made move when game is not started!");
    }
    
}

