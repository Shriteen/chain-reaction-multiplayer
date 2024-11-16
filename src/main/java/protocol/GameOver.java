package protocol;

import gamemodel.Player;

/**
 * GameOver represents game is over and conveys the winning player
 */
public class GameOver extends Message{    
    public final Player winner;
    
    public GameOver(Player player) {
        super("GAME_OVER");
        winner=player;
    }
}
