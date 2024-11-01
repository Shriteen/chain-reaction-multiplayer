package protocol;

/**
 * Exit is used to inform the server that the client will exit, this is used for graceful exit
 */
public class Exit extends Message {
    public Exit() {
        super("EXIT");
    }
}
