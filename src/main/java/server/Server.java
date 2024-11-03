package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Abstract class which represents design of a simple Server
 */
public abstract class Server extends Thread
{
    // The server socket which listens to incoming requests
    protected ServerSocket serverSocket;

    // state of the server to determine listen to more connections or not
    enum State{ CREATED ,LISTENING, STARTED, STOPPED };
    protected State state;
    
    // Constructor accepts the port on which to listen
    Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        state = State.CREATED;
        
        //set timeout to 20 seconds so that it does not listen indefinitely
        serverSocket.setSoTimeout(20000);
    }

    // Abstract method to be overridden by implementtations;
    // The method is called by the listening loop with the socket handling the request as argument
    abstract void clientHandler(Socket sock);
    
    // Start listening loop on a separate thread 
    public void run(){
        state= State.LISTENING;
        System.out.println("Server listening on port " + serverSocket.getLocalPort());

        try{
            while (state == State.LISTENING) {
                try {
                    Socket clientRequest=serverSocket.accept();
                    clientHandler(clientRequest);
                }
                catch(SocketTimeoutException e){
                    // This exception is thrown when accept waits for time set using setSoTimeout
                    System.out.print("Server Timeout!");
                    if(state == State.LISTENING)
                        System.out.println(" Listening again.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Change server state to Started
    protected void setStarted() {
        state = State.STARTED;
    } 

    // Change server state to Stopped
    public void setStopped() {
        state = State.STOPPED;
    } 

    
}

