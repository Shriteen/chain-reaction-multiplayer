package ui;

import java.util.ResourceBundle;
import java.net.URL;
import java.net.ConnectException;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import com.maximeroussy.invitrode.WordGenerator;

import server.GameServer;
import client.GameClient;

import static app.App.server;
import static app.App.client;

/**
 * HomeController: The screen shown on application launch
 */
public class HomeController implements Initializable {

    @FXML
    private TextField username; 
    
    @FXML
    private TextField ipAddr; 

    @FXML
    private Button hostGameBtn;
    @FXML
    private Button joinBtn;
    @FXML
    private VBox loadingSpinnerBox;
    @FXML
    private VBox connectionControlsBox;

    @FXML
    private Text errorMessage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Initialize default username to a random pronounceable string
        String defaultName= new WordGenerator().newWord(7).toLowerCase();
        defaultName= defaultName.substring(0,1).toUpperCase() + defaultName.substring(1);
        username.setText(defaultName);

        hostGameBtn.disableProperty().bind(
            Bindings.isEmpty(username.textProperty())
            );

        joinBtn.disableProperty().bind(
            Bindings.isEmpty(username.textProperty())
            .or(Bindings.isEmpty(ipAddr.textProperty()))
            );

        loadingSpinnerBox.setVisible(false);
    }

    @FXML
    @SuppressWarnings("unchecked")
    private void hostGame() {
        System.out.println("Host game clicked");

        Task startServerTask = getStartServerTask();
        startServerTask.setOnSucceeded( (e)->{
                System.out.println("Server started");

                try {
                    // Connection to itself
                    client= new GameClient("127.0.0.1",username.getText());

                    // Transition UI
                    URL serverLobbyFxml=getClass().getResource("/fxml/serverLobby.fxml");
                    Parent root= FXMLLoader.load(serverLobbyFxml);
                    
                    ((Stage)hostGameBtn.getScene().getWindow()).setScene(new Scene(root,640, 480));
                }
                catch (Throwable err) {
                    /* No special error handling considering that
                       connecting to same machine won't have issues
                       and connection won't get refused due to it
                       being among first if not first to connect.
                    */
                    System.out.println("Error " + err.getMessage());
                    err.printStackTrace();
                }
                
            });
        
        new Thread(startServerTask).start();        
    }
    
    @FXML
    @SuppressWarnings("unchecked")
    private void joinGame() {
        System.out.println("Joining server");

        Task connectToServerTask = getConnectToServerTask();
        connectToServerTask.setOnSucceeded( (e)->{
                System.out.println("Connected to server");

                try {
                    // Transition UI
                    URL clientLobbyFxml=getClass().getResource("/fxml/clientLobby.fxml");
                    Parent root= FXMLLoader.load(clientLobbyFxml);
                    
                    ((Stage)joinBtn.getScene().getWindow()).setScene(new Scene(root,640, 480));
                }
                catch (Throwable ex) {
                    System.out.println("Error " + ex.getMessage());
                    ex.printStackTrace();
                }
                
            });
        
        new Thread(connectToServerTask).start();
    }

    // creates and returns a task that connects to server, also binds with UI updates
    private Task getConnectToServerTask() {
        Task connectToServerTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    for(int i=1;i<=5;i++){ // try upto 5 times if connection refused
                        try {
                            client= new GameClient(ipAddr.getText(),username.getText());
                            
                            /* set error handler ASAP;
                               possible chance of race condition where error comes before this is set,
                               but not showing error message in UI shouldn't be a deal breaker 🙂 as
                               fallback to report error in console already exist
                            */
                            client.onErrorMessageReceived(app.App::showErrorMessagePopup);
                            
                            return null;
                        }
                        catch (ConnectException e){
                            /* Java gives ConnectException for both Connection refused and Connection Timeout.
                               Thus to distinguish between them, this method is used */
                            if(e.getMessage().equals("Connection refused")) {
                                System.out.println("Connection refused: Attempt #"+i);
                                Thread.sleep(3000); // wait for 3 seconds before attempting again
                            }else{
                                System.out.println(e.getMessage());
                                Platform.runLater(()->{
                                        errorMessage.setText(e.getMessage());
                                    });
                                throw e;
                            }
                        }
                        catch (Throwable e) {
                            System.out.println("Error " + e.getMessage());
                            e.printStackTrace();
                            Platform.runLater(()->{
                                    errorMessage.setText(e.getMessage());
                                });
                            throw e;
                        }
                    }
                    Platform.runLater(()->{
                            errorMessage.setText("Can't connect to server. Try Again ");
                        });
                    throw new Exception();
                }
            };

        // show spinner when trying to connect and disable the inputs
        loadingSpinnerBox.visibleProperty().bind(connectToServerTask.runningProperty());
        connectionControlsBox.disableProperty().bind(connectToServerTask.runningProperty());

        return connectToServerTask;
    }

    // creates and returns a task that starts server, also binds with UI updates
    private Task getStartServerTask() {
        Task startServerTask = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        server= new GameServer();
                        server.start();
                        return null;
                    }
                    catch (Throwable e) {
                        System.out.println("Error " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(()->{
                                errorMessage.setText("Unable to start server: "+e.getMessage());
                            });
                        throw e;
                    }
                }
            };

        // show spinner and disable the inputs
        loadingSpinnerBox.visibleProperty().bind(startServerTask.runningProperty());
        connectionControlsBox.disableProperty().bind(startServerTask.runningProperty());

        return startServerTask;
    }

    
}
