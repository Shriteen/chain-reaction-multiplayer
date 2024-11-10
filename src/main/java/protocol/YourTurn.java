package protocol;

/**
 * YourTurn is a message which informs the client that they should play
 */
public class YourTurn extends Message{
    public YourTurn() {
        super("YOUR_TURN");
    }	
}
