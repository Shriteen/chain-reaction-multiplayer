package app;

import java.net.URL;
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import server.GameServer;
import client.GameClient;
import protocol.Exit;

public class App extends Application{

    // Server and client references are made available throughout application
    static public GameServer server;
    static public GameClient client;

    static private Stage stage;
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException{
        this.stage=stage;
        
        // Stop server if available on closing window
        stage.setOnCloseRequest(event -> {
                shutdown();
            });
        
        
        URL homeFxml=getClass().getResource("/fxml/home.fxml");
        Parent root= FXMLLoader.load(homeFxml);
        
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();
        
    }

    // Closes client and server if active
    static public void shutdown() {
        if(client != null){
            try {
                client.send(new Exit());
            }
            catch (Throwable e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
            client.deactivate();
            client=null;
        }
                
        if(server != null){
            server.stopServer();
            server=null;
        }
    }
    
    static public void showDisconnectedScreen() {
        Platform.runLater(()->{
                try {        
                    URL disconnectedFxml=App.class.getResource("/fxml/disconnected.fxml");
                    Parent root= FXMLLoader.load(disconnectedFxml);
                    stage.setScene(new Scene(root,640, 480));
                }
                catch (Throwable e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }   
            });
    }
}
