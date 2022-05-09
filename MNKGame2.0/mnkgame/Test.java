package mnkgame;

import org.junit.Assert;

public class Test {

    public static void main(String[] args) {
        getAllWinningAlimentsTest();
    }

    //@Test
    public static void getAllWinningAlimentsTest() {

        MNKBoard b = new MNKBoard(3,3,3);
        GW player = new GW();
        b.markCell(1,1);
        
        //HashSet<HashSet<MNKCell>> alignments = new HashSet<>();


        //alignments.add(new HashSet<>()); 



        Assert.assertEquals(player.getAllWinningAliments(b).size(), 12);


    }

    
}
