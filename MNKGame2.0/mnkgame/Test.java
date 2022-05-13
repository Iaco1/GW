package mnkgame;
import java.util.HashSet;

import org.junit.Assert;

/**
 * Testing class for GW methods
 * @author Leonie Brockmann 
 */
public class Test {
    //uses getAllWinningAlignments
    /**
     * Tests whether this 3,3,3 game board has cells valued according to our current alignment heuristic:
     * x    0.6     0.6
     * 0.6  o       o
     * 0.8  0.6     x
     * @author Davide Iacomino
     */
    public static void computeValueTest() {
        GW gw = new GW();

        gw.board =  new MNKBoard(3, 3, 3);
        gw.board.markCell(0, 0);
        gw.board.markCell(1, 1);
        gw.board.markCell(2, 2);
        gw.board.markCell(1, 2);
        
        gw.createAllWinningAlignments(gw.intToMNKCellState(gw.player));

        for(HashSet<MNKCell> currentAligment : gw.allWinningAlignments){
            for(MNKCell cell : currentAligment){
                Double value = gw.computeValue(cell, currentAligment, gw.allWinningAlignments);
                System.out.print("(" + cell.i + ", " + cell.j + ", " + value + "), ");
            }
            System.out.println();
        }
        System.out.println("just don't stop yet");

    }

    public static void computeValueTest774(){
        GW gw = new GW();
        gw.board = new MNKBoard(7, 7, 4);
        gw.board.markCell(2, 2);
        gw.board.markCell(1, 2);
        gw.board.markCell(2, 3);
        gw.board.markCell(2, 1);
        gw.board.markCell(3, 2);

        gw.createAllWinningAlignments(gw.getOpponent(gw.intToMNKCellState(gw.player)));

        gw.evBoard = new EvaluationBoard(gw.board.M, gw.board.N);
        for(MNKCell cell : gw.board.getMarkedCells()){
            gw.evBoard.assignMark(cell);
        }

        gw.evBoard.printBoard(5);

        for(HashSet<MNKCell> currentAligment : gw.allWinningAlignments){
            for(MNKCell cell : currentAligment){
                Double value = gw.computeValue(cell, currentAligment, gw.allWinningAlignments);
                gw.evBoard.assignValue(value, cell);
            }
        }

        gw.evBoard.printBoard(5);

        System.out.println("just don't stop yet");

    }
    /**
     * Tests whether you get the expected 12 alignments in the set
     * @author Leonie Brockmann
     */
    public static void createAllWinningAlignmentsTest() {

        MNKBoard b = new MNKBoard(3,3,3);

        GW player = new GW();
        b.markCell(1,1);
        b.markCell(1,2);
        player.setBoard(b);
        
        //HashSet<HashSet<MNKCell>> alignments = new HashSet<>();


        //alignments.add(new HashSet<>()); 


        player.createAllWinningAlignments(player.intToMNKCellState(player.player));
        Assert.assertEquals(player.allWinningAlignments.size(), 4);

    }

    public static void main(String[] args) {
        computeValueTest774();
    }
}
