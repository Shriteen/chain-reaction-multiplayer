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

        //refresh game state
        client.refreshStateRequest();
    }    
}
