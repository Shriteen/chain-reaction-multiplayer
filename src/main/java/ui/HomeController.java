package ui;

import java.util.ResourceBundle;
import java.net.URL;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import com.maximeroussy.invitrode.WordGenerator;

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
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Initialize default username to a random pronounceable string
        String defaultName= new WordGenerator().newWord(7).toLowerCase();
        defaultName= defaultName.substring(0,1).toUpperCase() + defaultName.substring(1);
        username.setText(defaultName);
    }

    @FXML
    private void hostGame() {
        System.out.println("placeholder method: host game clicked");
    }
    
    @FXML
    private void joinGame() {
        System.out.println("placeholder method: join clicked");
    }

    
}
