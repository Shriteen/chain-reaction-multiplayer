package protocol;

import gamemodel.GameModel;

/**
 * GameState: Message containing game model, sent by server to clients to sync state
 */
public class GameState extends Message{
    public final GameModel state;
    
    public GameState(GameModel state){
        super("GAME_STATE");
        this.state=state;
    }
}
