package protocol;

/**
 * MakeMove message is sent by client to server to make a move at row,col position 
 */
public class MakeMove extends Message{
    public final int row,col;

    public MakeMove(int r,int c) {
        super("MAKE_MOVE");
        row=r;
        col=c;
    }
}
