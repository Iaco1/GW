package mnkgame;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Testing class for GW methods
 * @author Leonie Brockmann 
 */
public class Test {

        /**
     * Plays the game with the list of moves given for each player
     * will not work if the moves lists' length have a difference > 1  
     * assumes the positions are in the bounds of the board 
     * @param b
     * @param p1Moves
     * @param p2Moves
     */
    public static void scenario(MNKBoard b, LinkedList<Position> p1Moves, LinkedList<Position> p2Moves){
        if(Math.abs(p1Moves.size() - p2Moves.size()) > 1) return;
        Iterator<Position> p1i = p1Moves.iterator(), p2i = p2Moves.iterator();
        while(true){
            if(!p1i.hasNext() && !p2i.hasNext()) return;
            if(p1i.hasNext()){
                Position move = (Position) p1i.next();
                b.markCell(move.i, move.j);
            }
            if(p2i.hasNext()){
                Position move = (Position) p2i.next();
                b.markCell(move.i, move.j);
            }
        }
    }

    public static void horizontalOpenThreatsTest(){
        GW gw = new GW();
        gw.initPlayer(3, 3, 3, true, 10);
        gw.board.markCell(0, 1);
        gw.board.markCell(1, 1);
        gw.board.markCell(2, 1);
        gw.getOpenThreats(gw.board, MNKCellState.P1, gw.board.K-2);

        gw.initPlayer(7, 7, 4, true, 10);
        LinkedList<Position> p1Moves = new LinkedList<>();
        LinkedList<Position> p2Moves = new LinkedList<>();
        p1Moves.add(new Position(1, 1));
        p1Moves.add(new Position(1, 2));
        p1Moves.add(new Position(1, 4));
        p1Moves.add(new Position(2, 0));
        p1Moves.add(new Position(2, 1));
        p1Moves.add(new Position(2, 2));
        p1Moves.add(new Position(3, 1));
        p1Moves.add(new Position(3, 2));
        p1Moves.add(new Position(3, 3));
        p1Moves.add(new Position(5, 2));
        p1Moves.add(new Position(5, 3));

        p2Moves.add(new Position(0,1));
        p2Moves.add(new Position(0,2));
        p2Moves.add(new Position(0,3));
        p2Moves.add(new Position(0,5));
        p2Moves.add(new Position(1,5));
        p2Moves.add(new Position(2,3));
        p2Moves.add(new Position(2,4));
        p2Moves.add(new Position(2,5));
        p2Moves.add(new Position(4,0));
        p2Moves.add(new Position(4,5));

        scenario(gw.board, p1Moves, p2Moves);
        

        gw.getOpenThreats(gw.board, MNKCellState.P1, 3);
        gw.getOpenThreats(gw.board, MNKCellState.P1, 2);
    }

    public static void main(String[] args) {
       horizontalOpenThreatsTest();
    }
}
