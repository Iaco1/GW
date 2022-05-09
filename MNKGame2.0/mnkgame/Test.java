package mnkgame;

import java.util.HashSet;

public class Test {
    public static void main(String[] args) {
        MNKBoard b = new MNKBoard(3, 3, 3);
        b.markCell(0, 0);
        b.markCell(1, 1);
        b.markCell(2, 2);
        b.markCell(1, 2);
        

        HashSet<HashSet<MNKCell>> allWinningAlignments = new HashSet<>();
        HashSet<MNKCell> alignment = new HashSet<>();

        alignment.add(b.getMarkedCells()[0]);
        alignment.add(new MNKCell(1, 0, MNKCellState.FREE));
        alignment.add(new MNKCell(2, 0, MNKCellState.FREE));

        allWinningAlignments.add(alignment);


        alignment = new HashSet<>();
        alignment.add(b.getMarkedCells()[0]);
        alignment.add(new MNKCell(0, 1, MNKCellState.FREE));
        alignment.add(new MNKCell(0, 2, MNKCellState.FREE));
        allWinningAlignments.add(alignment);


        alignment = new HashSet<>();
        alignment.add(b.getMarkedCells()[2]);
        alignment.add(new MNKCell(2, 1, MNKCellState.FREE));
        alignment.add(new MNKCell(2, 0, MNKCellState.FREE));
        allWinningAlignments.add(alignment);

        GW gw = new GW();
        for(HashSet<MNKCell> currentAligment : allWinningAlignments){
            for(MNKCell cell : currentAligment){
                Double value = gw.computeValue(cell, currentAligment, allWinningAlignments);
                System.out.print("(" + cell.i + ", " + cell.j + ", " + value + "), ");
            }
            System.out.println();
        }
        System.out.println("just don't stop yet");

    }
}
