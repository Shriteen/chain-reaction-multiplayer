package ui;

import java.util.ResourceBundle;
import java.net.URL;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import client.GameClient;

import static app.App.server;
import static app.App.client;

/**
 * ClientLobby: The screen which waits till server starts game
 */
public class ClientLobbyController implements Initializable {
    
    @FXML
    private VBox container;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        client.onGameStateReceived(model->{
                Platform.runLater(()->{
                        try {
                            
                            URL boardFxml=getClass().getResource("/fxml/board.fxml");
                            Parent root= FXMLLoader.load(boardFxml);
                            ((Stage)container.getScene().getWindow()).setScene(
                                new Scene(root,640, 480)
                                );
                        }
                        catch (Throwable e) {
                            System.out.println("Error " + e.getMessage());
                            e.printStackTrace();
                        }   
                    });
            });

        client.onSocketClosed(state->{
                if(state == GameClient.State.CONNECTED || state == GameClient.State.STARTED){
                    System.out.println("Disconnected from Server");
                    app.App.showDisconnectedScreen();
                }
            });

        //handle case when socket is closed before onclosed event handler is set
        if(client.isSocketClosed()){
            System.out.println("Disconnected from Server");
            // no condition as at this point state should be CONNECTED or STARTED
            app.App.showDisconnectedScreen();
        }

        //refresh game state
        client.refreshStateRequest();
    }
}
