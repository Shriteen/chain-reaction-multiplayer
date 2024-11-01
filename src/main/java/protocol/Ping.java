package protocol;

/**
 * Ping is the simplest message used to verify connection is working
 */
public class Ping extends Message {
    public Ping() {
        super("PING");
    }
}
