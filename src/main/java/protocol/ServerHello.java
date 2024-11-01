package protocol;

/**
 * ServerHello message is sent by the server to client on connection
 */
public class ServerHello extends Message{    
    public final int id;
    
    public ServerHello(int id) {
        super("SERVER_HELLO");
        this.id = id;
    }
}
