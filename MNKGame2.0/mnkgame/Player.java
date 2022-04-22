package mnkgame;

import mnkgame.*;
import java.util.LinkedList;

/**
 * Draft implementation of an mnk-player
 */
public class Player implements MNKPlayer {
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
     * placeholder, returns 0.5 if it's my turn and 0.25 if it's the other player's turn
     */
    public double evaluate(MNKBoard b){
        switch (b.gameState()){
            case WINP1 -> { return 1; }
            case WINP2 -> { return 0; }
            case DRAW -> { return 0.5; }
            case OPEN -> { return 0.1; }
            default -> { return -0.1; }
        }
    }

    /**
     * Alpha-Beta pruning implementation
     * @param b The board to search
     * @param myTurn Whose turn it currently is
     * @param depth The depth up to which to search the tree
     * @param alpha The likeliness the player will win
     * @param beta The likeliness the adversary will win
     * @return How conducive to victory the current move will be
     */
    public double alphaBeta(MNKBoard b, boolean myTurn, int depth, double alpha, double beta){
        double eval;
        if(depth == 0 || b.getFreeCells().length == 0) return evaluate(b);
        else if(myTurn){
            eval = Double.MAX_VALUE;
            for(MNKCell freeCell : b.getFreeCells()){
                MNKBoard childBoard = b;
                childBoard.markCell(freeCell.i, freeCell.j);
                eval = Double.min(eval, alphaBeta(childBoard, false, depth-1, alpha, beta));
                beta = Double.min(eval, beta);
                if(beta <= alpha) break;
            }
            return eval;
        }else{
            eval = Double.MIN_VALUE;
            for(MNKCell freeCell : b.getFreeCells()){
                MNKBoard childBoard = b;
                childBoard.markCell(freeCell.i, freeCell.j);
                eval = Double.max(eval, alphaBeta(childBoard, true, depth-1, alpha, beta));
                alpha = Double.max(eval, alpha);
                if(beta <= alpha) break;
            }
            return eval;
        }
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        board = new MNKBoard(M, N, K);
        timeout = timeout_in_secs;

        player = (first) ? 0 : 1;
    }

    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        double optimalCellEval = 0.0;
        MNKCell optimalCell = FC[0];

        for(MNKCell freeCell : FC) {
            MNKBoard childBoard = board;
            double eval = alphaBeta(childBoard, childBoard.currentPlayer() == player, Integer.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE);
            if(eval > optimalCellEval){
                optimalCell = freeCell;
                optimalCellEval = eval;
            }
        }
        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }

    public static void main(String[] args) {

    }
}