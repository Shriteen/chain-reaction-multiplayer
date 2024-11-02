package client;

import java.net.Socket;
import java.io.*;
import com.google.gson.Gson;

import protocol.*;

/**
 * Client is a generic client used for common fields
 
   The constructor does not start the listening thread,
   it has to be explicitly called by subclass or external code
 */
public abstract class Client extends Thread{

    // The socket connecting to client
    protected Socket socket;
    // Input Output streams to read/write from/to socket
    protected BufferedReader reader;
    protected PrintWriter writer;    

    // Represents whether client is actively connected 
    private boolean active;

    /* Aggressive ping every receive loop if true,
       Daemon mode ping when false
       
       The flag is required so that two clients with aggressive ping
       by default do not go in indirect recursion by constantly
       pinging each other in a feedback loop */
    private boolean aggressivePing;
    
    // initialise socket and start communication in a new thread
    protected Client(Socket socket, boolean aggressivePing) throws IOException{
        this.socket = socket;
        active = true;

        this.aggressivePing = aggressivePing;
        if(!aggressivePing)
            new PingDaemon();
        
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
        
    public void send(Message message) throws IOException {
        writer.println(message.toString());
    }

    // The method is called before starting the receiving loop
    // Default implementation is empty
    // Designed such that subclasses will override if required
    protected void preLoopHook() throws Throwable { }

    // The method is called after receiving loop when client is no longer active
    // Default implementation is empty
    // Designed such that subclasses will override if required
    protected void postLoopHook() throws Throwable { }

    // abstract method to handle messages
    // The subclass should implement this to handle messages other than PING, ERROR_MESSAGE and EXIT
    protected abstract void handleMessages(String messageType, String jsonMessage);

    // The method is called when error message is received
    // Default implementation just prints error message to console
    // Designed such that subclasses will override to handle errors
    protected void handleErrorMessage(ErrorMessage err) {
        System.out.println(err.getErrorDisplayString());
    }
    
    // The receiver loop thread, calls the hooks and handleMessages method;
    // Handles PING, ERROR_MESSAGE and EXIT messages
    public void run() {
        try {
            preLoopHook();

            while (active) {
                try {
                    if(aggressivePing) {
                        //Send ping as test for communication,
                        //will give error in case other side is closed
                        send(new Ping());
                        if(writer.checkError()){
                            active=false;
                            continue;                    
                        }
                    }
                    
                    String jsonMessage=reader.readLine();
                    if(jsonMessage==null)
                        continue;

                    Message message=new Gson().fromJson(jsonMessage, Message.class);

                    // The heart of thread which handles incoming messages
                    // Handle generic connection related messages
                    switch(message.messageType){
                        
                    case "PING":
                        System.out.println("Received ping from "+socket);
                        break;
                    case "ERROR_MESSAGE":
                        ErrorMessage errorMessage= new Gson().fromJson(jsonMessage, ErrorMessage.class);
                        handleErrorMessage(errorMessage);
                        break;
                    case "EXIT":
                        active=false;
                        break;
                        
                    // The other messages are passed to handler method
                    default:
                        handleMessages(message.messageType, jsonMessage);
                        break;
                    }
                    
                }
                catch (IOException e) {
                    active=false;
                }
            }
        }
        catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        } finally {

            try {
                postLoopHook();
            }
            catch (Throwable e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
            
        }
    }
    
    public boolean isActive() {
        return active;
    }

    // set active to false i.e. do not consider client to be useful anymore. Like a close()
    public void deactivate() {
        active = false;
    }

    //Thread which runs in background to check if connected by pinging every 20 seconds
    private class PingDaemon extends Thread {
        PingDaemon() {
            this.start();
        }
        
        public void run() {
            // Ping every 10 seconds
            while(active){
                try {
                    Thread.sleep(10000);
                    //Send ping as test for communication,
                    //will give error in case other side is closed
                    send(new Ping());
                    if(writer.checkError())
                        active=false;
                }
                catch (IOException | InterruptedException e) {
                    active=false;
                }
            }
        }
        
    }
    
}
