package protocol;

/**
 * ErrorMessage represents some error has occured
 */
public class ErrorMessage extends Message{
    public enum Code{
        CONN_REFUSED,
        OTHER
    };
    
    private String messageString;
    private Code code;
    
    public ErrorMessage(Code code,String message) {
        super("ERROR_MESSAGE");
        messageString= message;
        this.code= code;
    }
}
