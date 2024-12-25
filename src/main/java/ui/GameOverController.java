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

import gamemodel.Player;

import static app.App.server;
import static app.App.client;

/**
 * GameOver: The screen which shows winner
 */
public class GameOverController implements Initializable {
    
    @FXML
    private Text winnerMessage;

    private Player winner;
    
    public GameOverController(Player winner) {
        this.winner= winner;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(client.getID() == winner.id()){
            winnerMessage.setText("You won!");
        }
        else{
            winnerMessage.setText(winner.name()+" won the Game!");
        }
    }

    @FXML
    private void exitGame() {
        app.App.shutdown();

        Platform.runLater(()->{
                try {            
                    URL homeFxml=getClass().getResource("/fxml/home.fxml");
                    Parent root= FXMLLoader.load(homeFxml);
                    ((Stage)winnerMessage.getScene().getWindow()).setScene(
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
