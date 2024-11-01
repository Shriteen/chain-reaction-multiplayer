package protocol;

import com.google.gson.Gson;

/**
 * Message abstract class represents common information shared by all messages
 */
public class Message {
    public String messageType;

    Message(String type) {
        this.messageType=type;
    }

    //return JSON representation of message
    public String toString() {
        return new Gson().toJson(this).toString();
    }
}
