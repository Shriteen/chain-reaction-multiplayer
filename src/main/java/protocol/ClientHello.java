package protocol;

/**
 * ClientHello message is sent by the client to server on connection
 */
public class ClientHello extends Message{    
    public final String userName;
    
    public ClientHello(String username) {
        super("CLIENT_HELLO");
        this.userName= username;
    }
}
