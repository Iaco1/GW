package mnkgame;
import org.junit.Assert;
import java.util.HashSet;

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
        
        HashSet<HashSet<MNKCell>> allWinningAlignments = gw.getAllWinningAlignments(gw.board);

        gw.filter(allWinningAlignments, 0);
        
        for(HashSet<MNKCell> currentAligment : allWinningAlignments){
            for(MNKCell cell : currentAligment){
                Double value = gw.computeValue(cell, currentAligment, allWinningAlignments);
                System.out.print("(" + cell.i + ", " + cell.j + ", " + value + "), ");
            }
            System.out.println();
        }
        System.out.println("just don't stop yet");

    }

    /**
     * Tests whether you get the expected 12 alignments in the set
     * @author Leonie Brockmann
     */
    public static void getAllWinningAlimentsTest() {

        MNKBoard b = new MNKBoard(3,3,3);
        GW player = new GW();
        b.markCell(1,1);
        b.markCell(1,2);
        
        //HashSet<HashSet<MNKCell>> alignments = new HashSet<>();


        //alignments.add(new HashSet<>()); 


        HashSet<HashSet<MNKCell>> awa = player.getAllWinningAlignments(b);
        Assert.assertEquals(awa.size(), 12);

    }

    public static void main(String[] args) {
        getAllWinningAlimentsTest();
    }
}
