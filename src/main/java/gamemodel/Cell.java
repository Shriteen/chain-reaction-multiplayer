package gamemodel;

/**
 * Cell: Represents a cell in the grid
 */
public class Cell {

    //Max number of orbs the cell can contain
    final int maxCount;

    //count of orbs present in cell
    int count=0;

    //id of player who owns the cell, 0 if cell is empty
    int ownerId=0;

    Cell(int maxCount)
    {
        this.maxCount=maxCount;
    }

    // resets the cell
    void clear()
    {
        count=0;
        ownerId=0;
    }

    /* Activation of the cell by a player.
       id is the id of player who will own the cell after the operation.
       Note that this does not check for ownership and simply overrides.

       If the cell explodes due to maxCount crossed then returns true, false otherwise.
     */
    boolean activate(int id)
    {
        if(count>=maxCount){            
            clear();
            return true;
        }
        else{
            count++;
            ownerId=id;
            return false;
        }
    }
    
    public String toString()
    {
        return ("Cell:- maxCount:"+ maxCount +" "+
                          "count:"+ count    +" "+
                        "ownerId:"+ ownerId );
    }

    public int getCount() {
        return count;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
