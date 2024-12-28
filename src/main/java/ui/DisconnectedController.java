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
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;


import static app.App.server;
import static app.App.client;

/**
 * Disconnected: The screen shown if connection is lost
 */
public class DisconnectedController implements Initializable {

    @FXML
    private Text message;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void exitGame() {
        app.App.shutdown();

        Platform.runLater(()->{
                try {            
                    URL homeFxml=getClass().getResource("/fxml/home.fxml");
                    Parent root= FXMLLoader.load(homeFxml);
                    ((Stage)message.getScene().getWindow()).setScene(
                        new Scene(root,640, 480)
                        );
                    
                }
                catch (Throwable e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            });
    }
}
