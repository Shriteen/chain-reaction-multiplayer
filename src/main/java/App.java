package app;

import java.net.URL;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import server.GameServer;
import client.GameClient;

public class App extends Application{

    // Server and client references are made available throughout application
    static public GameServer server;
    static public GameClient client;
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException{

        // Stop server if available on closing window
        stage.setOnCloseRequest(event -> {
                if(server != null){
                    server.setStopped();
                }
            });
        
        
        URL homeFxml=getClass().getResource("/fxml/home.fxml");
        Parent root= FXMLLoader.load(homeFxml);
        
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();
        
    }

    
}
