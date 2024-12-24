package protocol;

/**
 * RequestGameState is used by client to request latest game state
 */
public class RequestGameState  extends Message{
	public RequestGameState() {
        super("REQUEST_STATE");
    }
}
