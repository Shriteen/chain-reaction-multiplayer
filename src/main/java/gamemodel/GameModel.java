package gamemodel;

import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.AbstractMap.SimpleImmutableEntry;
import com.google.gson.Gson;

/**
 * GameModel: The model which defines the game state and moves on the state

   initPlayers is LinkedHashMap of Player. It is assumed that key will
   always be the same id as that of the player object used as value
 */
public class GameModel {
    private int rows;
    private int cols;
    private Cell grid[][];
    
    //list of active players
    private ArrayList<Player> players;

    //To keep track of whose turn it is
    private int turnOfPlayerIndex;

    //Required for internal game logic
    private boolean isRound1=true;

    
    public GameModel(int rows,int cols,ArrayList<Player> initPlayers) {

        if(rows<=1 || cols<=1)
            throw new IllegalArgumentException("There should be minimum 2 rows and columns");
        if(initPlayers.size() < 2)
            throw new IllegalArgumentException("There should be at-least 2 players");
        if(initPlayers.size() > (rows*cols))
            throw new IllegalArgumentException("Number of players cannot exceed cells");
        
        
        this.rows=rows;
        this.cols=cols;
        this.players= initPlayers;

        turnOfPlayerIndex= 0;        
            
        grid= new Cell[rows][cols];
        for(int i=0; i<rows;i++){
            for(int j=0; j<cols;j++){
                // if at edge, max value decreases
                int potentialMaxValue=3;
                if(i==0 || i==rows-1 )
                    potentialMaxValue--;
                if(j==0 || j==cols-1 )
                    potentialMaxValue--;
                grid[i][j]= new Cell(potentialMaxValue);
            }
        }
    }

    //TODO: Remove the method used for debugging
    public void print() {
        System.out.println("================================================================================");
        System.out.println(players);
        System.out.println("Turn of: "+ players.get(turnOfPlayerIndex).name());
        System.out.println("================================================================================");       
        for(int i=0; i<rows;i++){
            for(int j=0; j<cols;j++){
                System.out.print(grid[i][j]+"  ,  ");
            }
            System.out.println();
        }
        System.out.println("================================================================================");
    }

    //Utility function to get the player object with the given Id
    private Player getPlayerWithId(int id) {
        return players.stream().filter(p -> p.id()==id).findFirst().get();
    } 

    //get the player having current turn
    public Player currentTurnOfPlayer() {
        return players.get(turnOfPlayerIndex);
    }
    
    //accepts row and column and returns whether it is within bounds of grid
    private boolean isValidAccess(int row,int col) {
        return ((row >= 0) && (row < rows) && (col >= 0) && (col < cols));
    }

    //accepts id, row and column and returns whether it is valid move for player
    private boolean isValidAccess(int id, int row,int col) {
        return isValidAccess(row,col) && ( grid[row][col].ownerId==0 || grid[row][col].ownerId==id );
    }

    //checks if index needs to be wrapped around and wraps it if required
    private void wrapAroundIndexIfNeeded() {
        if(turnOfPlayerIndex>= players.size()){
            turnOfPlayerIndex=0;
            isRound1=false;
        }
    }
    
    //increment turn variable with wraparound if needed
    private void nextTurn() {
        turnOfPlayerIndex++;
        wrapAroundIndexIfNeeded();
    }

    //player with given id makes a move on row,col position
    public void play(int playerId, int row, int col) throws Exception{
        
        //We do not use the player object,
        //we have called this to ensure that playerId is a valid player in game
        //The method will throw NoSuchElementException if player id is invalid for the game state
        getPlayerWithId(playerId);

        if(currentTurnOfPlayer().id()==playerId && isValidAccess(playerId,row,col)){
            ArrayDeque<SimpleImmutableEntry<Integer,Integer>> queue= new ArrayDeque<SimpleImmutableEntry<Integer,Integer>>();
            queue.addLast(new SimpleImmutableEntry<Integer,Integer>(row,col));
            
            int infiniteLoopBreakCounter = 0; // counter to bypass infinite looping issue
            
            while(!queue.isEmpty()){
                SimpleImmutableEntry<Integer,Integer> tup= queue.pollFirst();
                int r=tup.getKey();
                int c=tup.getValue();

                //To reach infinite chain reaction case thousands of explosions must have happened,
                //which is enough to assume that the player has eliminated everyone else 😅
                //and reaction can be stopped without affecting game results
                if( infiniteLoopBreakCounter<10000 && grid[r][c].activate(playerId) ){
                    if(isValidAccess(r-1,c))
                        queue.addLast(new SimpleImmutableEntry<Integer,Integer>(r-1,c));
                    if(isValidAccess(r,c+1))
                        queue.addLast(new SimpleImmutableEntry<Integer,Integer>(r,c+1));
                    if(isValidAccess(r+1,c))
                        queue.addLast(new SimpleImmutableEntry<Integer,Integer>(r+1,c));
                    if(isValidAccess(r,c-1))
                        queue.addLast(new SimpleImmutableEntry<Integer,Integer>(r,c-1));
                }
                
                infiniteLoopBreakCounter++;
            }

            // There should be at-least one round before elimination begins
            if(!isRound1)
                removeDead();
            
            nextTurn();
        }
        else
            throw new Exception(String.format("Invalid Player Access Exception: playerId:%d row:%d col:%d", playerId, row, col));        
    }

    //clear all the cells belonging to player with given id
    private void clearCellsOfPlayer(int id) {
        for(int i=0; i<rows;i++)
            for(int j=0; j<cols;j++)                
                if(grid[i][j].ownerId == id)
                    grid[i][j].clear();
    }

    //removes a player from game
    public void removePlayer(int id) {
        clearCellsOfPlayer(id);
        Player currentPlayer= currentTurnOfPlayer();
        synchronized(players) {
            players.removeIf(p->p.id()==id);
        }
        if(currentPlayer.id()==id){
            //index would be already set to next element, just ensure wrap
            wrapAroundIndexIfNeeded();
        }else{
            //If current player is not removed, we want currentPlayer to retain turn
            //irrespective of removed player comes before or after
            turnOfPlayerIndex= players.indexOf(currentPlayer);
        }
    }

    //Remove all players without any active cell
    private void removeDead() {
        //create shallow copy of list. i.e. The references get copied - they refer to same elements
        ArrayList<Player> playersCopy = new ArrayList<Player>(players);

        // remove players which have a cell from copy such that we end up with players having no cell
        for(int i=0; i<rows;i++){
            for(int j=0; j<cols;j++){
                if(grid[i][j].ownerId != 0){
                    final int ownerId=grid[i][j].ownerId;
                    playersCopy.removeIf(p -> p.id()==ownerId);
                }
            }
        }

        //remove the players having no cell
        for (Player p: playersCopy) {
            removePlayer(p.id());
        }
    }

    // return true if game is over
    public boolean isGameOver() {
        return players.size()==1;
    }

    //return winner player, throws exception if game is not over
    public Player getWinner() throws Exception{
        if(isGameOver())
            return players.getFirst();
        else
            throw new Exception("GameModel.getWinner called when game was not over yet!");
    }

    //return JSON representation of game state
    public String toJSON() {
        return new Gson().toJson(this).toString();
    }

    //factory method to create GameModel object from json string
    public static GameModel fromJSON(String jsonRepresentationOfGameState) {
        return new Gson().fromJson(jsonRepresentationOfGameState, GameModel.class);
    }
}
