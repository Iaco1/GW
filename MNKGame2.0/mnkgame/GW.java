package mnkgame;

import java.util.LinkedList;

/**
 * Draft implementation of an mnk-player
 */
public class GW implements MNKPlayer {
    protected MNKBoard board;
    protected int timeout;
    protected int player;
    final Double MIN = -1000000000.0;

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
                return 0.0;
            }
        }
    }

    /**
     * an implementation of minmax for mnk games
     * @param b The board on which to perform minmax
     * @param max The player that is now moving, max => our player
     * @return the value of the current move
     */
    public Double minmax(MNKBoard b, boolean max){
        Double eval;
        if(b.gameState() != MNKGameState.OPEN) return evaluate(b);
        else if(max){
            eval = MIN;
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


    /**
     * An implementation of the alpha-beta pruning algorithm for an mnk-game.
     * It calculates the likeliness of winning based on the state of the board.
     * @param b The Board to analyze
     * @param max Whether it is the player's turn or not
     * @param alpha The minimum attainable score for the player
     * @param beta The maximum attainable score for the adversary
     * @return The value of the current move
     */
    public Double alphaBeta(MNKBoard b, boolean max, double alpha, double beta){
        Double eval;
        if(b.gameState != MNKGameState.OPEN) return evaluate(b);
        else if(max){
            eval = MIN;
            for(MNKCell freeCell : b.getFreeCells()){
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.max(eval, alphaBeta(b, false, alpha, beta));
                b.unmarkCell();
                alpha = Double.max(eval, alpha);
                if(alpha >= beta) break;
            }
        }else{
            eval = Double.MAX_VALUE;
            for(MNKCell freeCell : b.getFreeCells()){
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.min(eval, alphaBeta(b, true, alpha, beta));
                b.unmarkCell();
                beta = Double.min(eval, beta);
                if(alpha >= beta) break;
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
        //optimal cell intitalization
        Double optimalValue = MIN;
        MNKCell optimalCell = FC[0];

        //debugging helper map
        ValueMap valueMap = new ValueMap(board.M, board.N);
        for(MNKCell markedCell : MC){
            valueMap.addMark(markedCell);
        }

        //mark last played cell by the adversary
        if(MC.length > 0) board.markCell(MC[MC.length-1].i, MC[MC.length-1].j);

        Double alpha = 0.0, beta = 0.0;
        //compute the value of each available move
        for(MNKCell freeCell : FC){
            board.markCell(freeCell.i, freeCell.j);
            Double currentCellValue = alphaBeta(board, (board.currentPlayer() == player) ? true : false, alpha, beta);

            if(currentCellValue > optimalValue){
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();

            valueMap.addValue(freeCell, currentCellValue);
        }

        valueMap.printMap();
        
        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}