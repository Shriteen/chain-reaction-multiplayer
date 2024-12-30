package ui;

import java.util.ResourceBundle;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import javafx.scene.control.TextFormatter;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import server.GameServer;
import server.SClient;
import client.GameClient;
import ui.util.NumberFormatterFactory;

import static app.App.server;
import static app.App.client;

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

    @FXML
    private Text ipHelp;

    @FXML
    private ListView<SClient> clientListWidget;

    @FXML
    private Text clientListMessage;

    private ScheduledService clientListDaemon;

    private ObservableList<SClient> clientList;
    
    
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

        //List to show connected players
        clientList= FXCollections.observableArrayList();
        setupClientListDaemon();
        clientListWidget.setItems(clientList);
        clientListWidget.setCellFactory(item -> {
                return new ListCell<>() {
                    @Override
                    public void updateItem(SClient sc, boolean empty) {
                        super.updateItem(sc, empty);
                        if (empty || sc == null) {
                            setText(null);
                        } else {
                            setText(sc.getUsername());
                        }
                    }
                };
            });

        clientListMessage.textProperty().bind(
            Bindings.concat("Players (",
                            Bindings.convert(Bindings.size(clientList)),
                            ")")
            );
        
        try {
            ipHelp.setText("Players can connect to "+
                           InetAddress.getLocalHost().getHostAddress()
                );
        }
        catch (UnknownHostException e) {
            ipHelp.setText("Players can connect to localhost");
        }
        
    }

    @FXML
    private void startGame() throws IOException {
                
        if(maxPlayerValidate() && gridSizeValidate() && minConnectedPlayersValidate()){
            System.out.println("Starting game");
            
            int r=Integer.parseInt(rowsCount.getText());
            int c=Integer.parseInt(colsCount.getText());
            
            server.startGame(r,c);

            // Transition UI
            URL boardFxml=getClass().getResource("/fxml/board.fxml");
            Parent root= FXMLLoader.load(boardFxml);
            ((Stage)startGameBtn.getScene().getWindow()).setScene(new Scene(root,640, 480));            

            //stop updating the players list in UI
            clientListDaemon.cancel();
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
        }else if(r>20 || c>20){
            errorString ="Maximum size of grid supported is 20*20";
        }else{                  //Valid
            return true;
        }

        Platform.runLater(()->{
                errorMessage.setText(errorString);
            });
        return false;
    }

    //checks and returns true if valid; also shows error message
    private boolean minConnectedPlayersValidate(){
        if(server.getClientList().size() < 2){
            Platform.runLater(()->{
                    errorMessage.setText("Minimum number of players is 2");
                });
            return false;
        }
        
        return true;
    }


    // Setup daemon to track and update connected clients
    private void setupClientListDaemon(){
        clientListDaemon= new ScheduledService() {
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Object call() {
                            Platform.runLater(()->{
                                    clientList.setAll(server.getClientList());
                                });                            
                            return null;
                        }
                    };
                }
            };
        clientListDaemon.setPeriod(Duration.seconds(1));
        clientListDaemon.start();
    }
}

