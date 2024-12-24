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
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints; 
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Background;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

import gamemodel.GameModel;

import static app.App.server;
import static app.App.client;

/**
 * Board: The screen where actual game is played
 */
public class BoardController implements Initializable {

    @FXML
    private Text turnMessage;

    @FXML
    private GridPane grid;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        client.onGameStateReceived(this::renderToGrid);
        //refresh game state
        client.refreshStateRequest();

    }

    private void renderToGrid(GameModel model) {

        Platform.runLater(()->{
                grid.getChildren().clear();
            });
        

        grid.getRowConstraints().clear();
        for (int i=0;i<model.getRows();i++) {
            RowConstraints row = new RowConstraints();
            //row.setVgrow(Priority.SOMETIMES);
            /* Let first and last row grow maximum,
               within these, first is aligned bottom and last is aligned top,
               rest take minimum space.
               This leads to grid being centered vertically
            */
            if(i==0){
                row.setValignment(VPos.BOTTOM);
                row.setVgrow(Priority.ALWAYS);
            }else if(i==model.getRows()-1){
                row.setValignment(VPos.TOP);
                row.setVgrow(Priority.ALWAYS);
            }
            grid.getRowConstraints().add(row);            
        }

        grid.getColumnConstraints().clear();
        for (int i=0;i<model.getCols();i++) {
            ColumnConstraints col = new ColumnConstraints();
            //col.setHgrow(Priority.NEVER);
            /* Let first and last column grow maximum,
               within these, first is aligned right and last is aligned left,
               rest take minimum space.
               This leads to grid being centered horizontally
            */
            if(i==0){
                col.setHalignment(HPos.RIGHT);
                col.setHgrow(Priority.ALWAYS);
            }else if(i==model.getCols()-1){
                col.setHalignment(HPos.LEFT);
                col.setHgrow(Priority.ALWAYS);
            }
            grid.getColumnConstraints().add(col);            
        }

        Platform.runLater(()->{
                for(int i=0;i<model.getRows();i++){
                    for(int j=0;j<model.getCols();j++){
                        Button cell= new Button(String.valueOf(model.getGrid()[i][j].getCount()));
                        GridPane.setConstraints(cell, j, i);

                        String cellColor;
                        int cellOwner= model.getGrid()[i][j].getOwnerId();
                        if(cellOwner==0){ // neutral
                            cellColor="#ffffff";
                        }else{
                            cellColor="#"+model.getPlayerWithId(cellOwner).color();
                        }
                        
                        cell.setBackground(new Background(
                                               new BackgroundFill(Color.web(cellColor),
                                                                  CornerRadii.EMPTY,
                                                                  Insets.EMPTY)
                                               ));
                
                        grid.getChildren().add(cell);
                    }
                }
                
                // set turn of message
                if(client.getID() == model.currentTurnOfPlayer().id())
                    turnMessage.setText("Your Turn");
                else
                    turnMessage.setText("Turn of "+ model.currentTurnOfPlayer().name());
            });
        
    }
    
    @FXML
    private void exitGame() {
        System.out.println("Exiting game");

        app.App.shutdown();

        Platform.runLater(()->{
                try {            
                    URL homeFxml=getClass().getResource("/fxml/home.fxml");
                    Parent root= FXMLLoader.load(homeFxml);
                    ((Stage)grid.getScene().getWindow()).setScene(
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
