package mnkgame;

import java.util.LinkedList;

/**
 * Draft implementation of an mnk-player
 */
public class GW implements MNKPlayer {
    private MNKBoard board;
    private int timeout;
    private int player;

/**
     * Finds the position, in the MC array, of all <code>MNKCell</code>s that are adjacent to cell c and of the same cell state
     * @param c The cell about which to search for adjacent elements with the same cell state
     * @param MC the array of all marked cells
     * @return the LinkedList list of all cells that are adjacent to c and of the same MNKCellState
     */
    public static LinkedList<MNKCell> findEqualAdjacent(MNKCell c, MNKCell [] MC){
        LinkedList<MNKCell> eqAdj = new LinkedList<>();
        if(c == null || MC == null) return eqAdj;

        for(MNKCell mc : MC){
            int horizontalDistance = Math.abs(mc.i - c.i), verticalDistance = Math.abs(mc.j - c.j);
            if(( verticalDistance == 1 || horizontalDistance == 1) && mc.state == c.state)  eqAdj.add(mc);   //mc is adjacent to c
        }
        return eqAdj;
    }

    /**
     * Returns the cell, that could be contained in the Marked Cells array, that would contribute to an alignment.
     * The method can return cells that lie outside the boundaries of the board.
     * @param c The first cell
     * @param a The second cell
     * @return The cell that would have to be contained in Marked Cells to contribute to an alignment
     * @throws Exception if the two cells are in the same position, are of a different <code>MNKCellState</code> or are not adjacent
     */
    public static MNKCell expectedNext(MNKCell c, MNKCell a) throws Exception {
        if(c.state != a.state) throw new Exception("The two cells are of a different MNKCellState");
        int rowDistance = Math.abs(c.i - a.i), columnDistance = Math.abs(c.j - a.j);

        if(rowDistance == 0 && columnDistance == 0) throw new Exception("The two cells are in the same position");
        if(columnDistance == 0) return new MNKCell(c.i + 1, c.j);  // cells on the same column
        if(rowDistance == 0) return new MNKCell(c.i, c.j + 1);  // cells on the same row
        if(rowDistance == 1 && columnDistance == 1) return new MNKCell(c.i + 1, c.j + 1);

        throw new Exception("The two cells are not adjacent");
    }


    /**
     * placeholder
     */
    public Double evaluate(MNKBoard board){
        switch(board.gameState()){
            case WINP1:{
                return (player == 0) ? 1.0 : -1.0;
            }
            case WINP2:{
                return (player == 1) ? 1.0 : -1.0;
            }
            case DRAW:
            case OPEN:
            default:{
                return 0.5;
            }
        }
    }

    /**
     * an implementation of minmax for mnk games
     * @param b The board on which to perform minmax
     * @param max The player that is now moving, max => our player
     * @return the 
     */
    public Double minmax(MNKBoard b, boolean max){
        Double eval;
        if(b.getFreeCells().length == 0 || b.gameState() != MNKGameState.OPEN) return evaluate(b);
        else if(max){
            eval = Double.MIN_VALUE;
            for(MNKCell freecell : b.getFreeCells()){
                b.markCell(freecell.i, freecell.j);
                eval = Double.max(eval, minmax(b, false));
                b.unmarkCell();
            }
        }else{
            eval = Double.MAX_VALUE;
            for(MNKCell freecell : b.getFreeCells()){
                b.markCell(freecell.i, freecell.j);
                eval = Double.min(eval, minmax(b, true));
                b.unmarkCell();
            }
        }
        return eval;
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        board = new MNKBoard(M, N, K);
        timeout = timeout_in_secs;

        player = (first) ? 0 : 1;
    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        Double optimalValue = Double.MIN_VALUE;
        MNKCell optimalCell = FC[0];

        if(MC.length > 0) board.markCell(MC[MC.length-1].i, MC[MC.length-1].j); //mark last played cell by the adversary

        for(MNKCell freeCell : FC){
            board.markCell(freeCell.i, freeCell.j);
            Double currentCellValue = minmax(board, (board.currentPlayer() == player) ? true : false );
            
            if(currentCellValue > optimalValue){
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
        }
        
        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}