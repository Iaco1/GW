package mnkgame;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Testing class for GW methods
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
    public static void scenario(Board b, LinkedList<Position> p1Moves, LinkedList<Position> p2Moves){
        if(Math.abs(p1Moves.size() - p2Moves.size()) > 1) return;
        Iterator<Position> p1i = p1Moves.iterator(), p2i = p2Moves.iterator();
        while(true){
            if(!p1i.hasNext() && !p2i.hasNext()) return;
            if(p1i.hasNext()){
                Position move = (Position) p1i.next();
                b.markCell(move.i, move.j);
                b.updateThreats(b.getCellAt(move.i, move.j));
                b.updateBoardVisualisation();
            }
            if(p2i.hasNext()){
                Position move = (Position) p2i.next();
                b.markCell(move.i, move.j);
                b.updateThreats(b.getCellAt(move.i, move.j));
                b.updateBoardVisualisation();
            }
        }
    }

    public static void playback(Board b){
        for(int i = b.MC.size()-1; i >= 0; i--){
            MNKCell pivot = b.MC.get(i);
            pivot = new MNKCell(pivot.i, pivot.j, MNKCellState.FREE);
            b.unmarkCell();
            b.updateThreats(pivot);

        }
    }
    
    public static void scenario(Board b, LinkedList<MNKCell> MC){
        for(MNKCell c : MC){
            b.markCell(c.i, c.j);
        }
    }

    public static void openThreatsTest(){
        GW gw = new GW();

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
        playback(gw.board);
    }

    public static void extremitiesUpdaterTest(){
        GW gw = new GW();
        gw.initPlayer(7, 7, 4, true, 10);
        LinkedList<Position> p1Moves = new LinkedList<>();
        p1Moves.add(new Position(1,1));
        p1Moves.add(new Position(1,2));
        p1Moves.add(new Position(1,3));
        p1Moves.add(new Position(3,1));
        p1Moves.add(new Position(3,2));
        p1Moves.add(new Position(3,4));
        p1Moves.add(new Position(0,6));
        p1Moves.add(new Position(2,6));

        LinkedList<Position> p2Moves = new LinkedList<>();
        p2Moves.add(new Position(6,0));
        p2Moves.add(new Position(6,2));
        p2Moves.add(new Position(6,4));
        p2Moves.add(new Position(6,6));
        p2Moves.add(new Position(1,0));
        p2Moves.add(new Position(1,4));
        p2Moves.add(new Position(3,0));
        p2Moves.add(new Position(3,5));

        scenario(gw.board, p1Moves, p2Moves);
        playback(gw.board);
    }

    public static void halfOpenThreatsTest(){
        GW gw = new GW();
        gw.initPlayer(7, 7, 4, true, 10);
        LinkedList<Position> p1Moves = new LinkedList<>();
        p1Moves.add(new Position(0,1));
        p1Moves.add(new Position(0,2));
        p1Moves.add(new Position(0,3));
        p1Moves.add(new Position(1,0));
        p1Moves.add(new Position(1,1));
        p1Moves.add(new Position(1,2));
        p1Moves.add(new Position(3,1));
        p1Moves.add(new Position(3,2));
        p1Moves.add(new Position(3,4));
        p1Moves.add(new Position(4,2));
        p1Moves.add(new Position(6,2));
        p1Moves.add(new Position(6,4));

        LinkedList<Position> p2Moves = new LinkedList<>();
        p2Moves.add(new Position(1,3));
        p2Moves.add(new Position(1,4));
        p2Moves.add(new Position(1,5));
        p2Moves.add(new Position(2,0));
        p2Moves.add(new Position(2,2));
        p2Moves.add(new Position(2,3));
        p2Moves.add(new Position(2,5));
        p2Moves.add(new Position(2,6));
        p2Moves.add(new Position(3,6));
        p2Moves.add(new Position(5,4));
        p2Moves.add(new Position(5,6));
        p2Moves.add(new Position(6,3));

        scenario(gw.board, p1Moves, p2Moves);
        playback(gw.board);
    }

    public static void simpleThreeOpenThreats(){
        GW gw = new GW();
        gw.initPlayer(7, 7, 4, true, 10);
        LinkedList<Position> p1Moves = new LinkedList<>();
        p1Moves.add(new Position(1,1));
        p1Moves.add(new Position(1,2));
        p1Moves.add(new Position(2,2));
        p1Moves.add(new Position(3,2));
        p1Moves.add(new Position(4,1));

        LinkedList<Position> p2Moves = new LinkedList<>();
        p2Moves.add(new Position(0,6));
        p2Moves.add(new Position(2,6));
        p2Moves.add(new Position(4,6));
        p2Moves.add(new Position(6,6));

        scenario(gw.board, p1Moves, p2Moves);

    }

    @Deprecated
    public static void getAllPossibleThreatsTest(){
        GW gw = new GW();
        gw.initPlayer(3, 3, 3, true, 10);
        gw.board.markCell(1, 1);
        LinkedList<Threat> apt = new LinkedList<>();//gw.board.getAllPossibleThreats(gw.board.getCellAt(1, 1));
        apt.removeLast();
        apt.removeFirst();
    }

    public static void main(String[] args) {
       halfOpenThreatsTest();
    }
}
