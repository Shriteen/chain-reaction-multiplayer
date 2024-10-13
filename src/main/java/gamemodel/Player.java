package gamemodel;

/**
 * Player: Represents a player in the game model
   id should be non-zero
   color should be a hex string pf length 6
 */
public record Player(int id, String name, String color) {
    public Player
    {
        //check id not 0
        if(id==0)
            throw new IllegalArgumentException("Player Id cannot be 0");            
        //check for length
        if(color.length()!=6)
            throw new IllegalArgumentException("The color should be hexadecimal string of six characters");
        //check for hex
        try {
            Integer.parseInt(color, 16);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The color should be hexadecimal string of six characters");            
        }
    }
}
