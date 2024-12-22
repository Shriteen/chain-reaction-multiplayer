package ui;

import java.util.ResourceBundle;
import java.net.URL;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.control.TextFormatter;
import javafx.beans.value.ChangeListener;

import server.GameServer;
import client.GameClient;

import static app.App.server;
import static app.App.client;


import ui.util.NumberFormatterFactory;

/**
 * ServerLobby: The screen which allows host to choose configuration for a game
 */
public class ServerLobbyController implements Initializable {

    @FXML
    private TextField maxPlayers; 
    
    @FXML
    private TextField rowsCount; 

    @FXML
    private TextField colsCount;    
    
    @FXML
    private Button startGameBtn;

    @FXML
    private Text errorMessage;
    
    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL url, ResourceBundle rb) {
        // Setup validation and events
        maxPlayers.setTextFormatter(NumberFormatterFactory.wholeNumberFormatter(8));
        rowsCount.setTextFormatter(NumberFormatterFactory.wholeNumberFormatter(10));
        colsCount.setTextFormatter(NumberFormatterFactory.wholeNumberFormatter(10));

        startGameBtn.disableProperty().bind(
            Bindings.isEmpty(maxPlayers.textProperty())
            .or(Bindings.isEmpty(rowsCount.textProperty()))
            .or(Bindings.isEmpty(colsCount.textProperty()))
            );

        // max players listener
        maxPlayers.textProperty().addListener((observable, oldValue, newValue) -> {
                if(maxPlayerValidate()){
                    Platform.runLater(()->{ errorMessage.setText(""); });
                    // Set max players on server
                    server.setMaxPlayers(Integer.parseInt(maxPlayers.getText())); 
                }
            });

        // grid size listeners
        ChangeListener listener= (observable, oldValue, newValue) -> {
            if(gridSizeValidate()){
                Platform.runLater(()->{ errorMessage.setText(""); });
            }
        };
        rowsCount.textProperty().addListener(listener);
        colsCount.textProperty().addListener(listener);
        
    }

    @FXML
    private void startGame() {
                
        if(maxPlayerValidate() && gridSizeValidate()){
            System.out.println("Starting game");
            
            //TODO: start logic (Count actual connected players are 2+ and UI transition)
            int r=Integer.parseInt(rowsCount.getText());
            int c=Integer.parseInt(colsCount.getText());
            
            server.startGame(r,c);
            
        }
        
        
    }

    //checks and returns true if valid; also shows error message
    private boolean maxPlayerValidate(){
        int mp=Integer.parseInt(maxPlayers.getText());
        String errorString;

        if(mp<2){
            errorString ="Minimum number of players is 2";
        }else if(mp>20){
            errorString ="Only upto 20 players are supported";
        }else{                  //Valid
            return true;
        }

        Platform.runLater(()->{
                errorMessage.setText(errorString);
            });
        return false;
    }

    //checks and returns true if valid; also shows error message
    private boolean gridSizeValidate(){
        int r=Integer.parseInt(rowsCount.getText());
        int c=Integer.parseInt(colsCount.getText());
        String errorString;

        if(r<4 || c<4){
            errorString ="Minimum size of grid supported is 4*4";
        }else if(r>30 || c>30){
            errorString ="Maximum size of grid supported is 30*30";
        }else{                  //Valid
            return true;
        }

        Platform.runLater(()->{
                errorMessage.setText(errorString);
            });
        return false;
    }
    
}
