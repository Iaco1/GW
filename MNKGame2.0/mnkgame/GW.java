package mnkgame;

import java.util.HashSet;

/**
 * Draft implementation of an mnk-player
 */
public class GW implements MNKPlayer {
    protected MNKBoard board;
    protected int timeout;
    protected int player;
    final Double MIN = -1000000000.0;

    public static String toSymbol(MNKCellState cs){
        switch(cs){
            case P1:{ return "X"; }
            case P2:{ return "O"; }
            case FREE: { return " "; }
            default: return "";
        }
    }



    /**
     * placeholder
     */
    public Double evaluateEndGame(MNKBoard board){
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
     * k == 3 version
     * @param b
     * @return the number of winning alinements the current player has at its disposal
     */
    /*public int winningAlinements(MNKBoard b){
        int alignements = 0;
        HashSet<MNKCell> alignement = new HashSet<>();
        for(MNKCell cell : b.getMarkedCells()){
            //get all possible alignements from this cell into a set
            //discard all alignements that can't lead to a victory in the next move
            //discard alignements that are already \in alignement
            //alignement = alignemnet union this iteration set
        }
        //return size of winning alinements for the current player
    }*/

    //public Double evaluateMidGame(){}
        // if winningAlinements() > 1 return evaluation based on who's the current player and who's the adversary

    /**
     * an implementation of minmax for mnk games
     * @param b The board on which to perform minmax
     * @param max The player that is now moving, max => our player
     * @return the value of the current move
     */
    public Double minmax(MNKBoard b, boolean max){
        Double eval;
        if(b.gameState() != MNKGameState.OPEN) return evaluateEndGame(b);
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
        if(b.gameState != MNKGameState.OPEN) return evaluateEndGame(b);
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

    public Double heuristic(MNKBoard b){
        HashSet<HashSet<MNKCell>> allPossibleAligments = new HashSet<>();
        allPossibleAligments = getAllWinningAliments(b);
        for(HashSet<MNKCell> alignment : allPossibleAligments){
            if(!isInBoard(alignment)) allPossibleAligments.remove(alignment);
            if(containsMark()) allPossibleAligments.remove(alignment);

        }
        Double optimalCellValue = -1000.0;
        for(HashSet<MNKCell> alignment : allPossibleAligments){
            for(MNKCell cell : alignment){
                Double currentCellValue = computeValue(cell);
                if(currentCellValue > optimalCellValue) optimalCellValue = currentCellValue;
            }
        }
        return optimalCellValue;
    }





    /**
     * creates a set of all possible winning alignments without consideration of other marked cells
     * @param board 
     * @return
     */
    public HashSet<HashSet<MNKCell>> getAllWinningAliments(MNKBoard board) {
        HashSet<HashSet<MNKCell>> allAligments = new HashSet<>();
        for (MNKCell markedCell : board.getMarkedCells()) {
            // find all possible alignments that contain markedCell
            for (int x = 0; x < board.K; x++) {
                HashSet<MNKCell> horizontalAlignment = new HashSet<>();
                HashSet<MNKCell> verticalAlignemnt = new HashSet<>();
                HashSet<MNKCell> diagonalAlignment1 = new HashSet<>();
                HashSet<MNKCell> diagonalAlignment2 = new HashSet<>();
                for (int a = x-board.K+1; a < x+board.K-2; a++) {
                    horizontalAlignment.add(new MNKCell(markedCell.i, markedCell.j+a));
                    verticalAlignemnt.add(new MNKCell(markedCell.i+a, markedCell.j));
                    diagonalAlignment1.add(new MNKCell(markedCell.i+a, markedCell.j+a));
                    diagonalAlignment2.add(new MNKCell(markedCell.i+a, markedCell.j-a));
                }
                allAligments.add(horizontalAlignment);
                allAligments.add(verticalAlignemnt);
                allAligments.add(diagonalAlignment1);
                allAligments.add(diagonalAlignment2);
            }
        }

        return allAligments;
    }


    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        //optimal cell intitalization
        Double optimalValue = MIN;
        MNKCell optimalCell = FC[0];

        //debugging helper map
        String[][] valueMap = new String[board.M][board.N];
        for(MNKCell markedCell : MC){
            valueMap[markedCell.i][markedCell.j] = toSymbol(markedCell.state);
        }

        //mark last played cell by the adversary
        if(MC.length > 0) board.markCell(MC[MC.length-1].i, MC[MC.length-1].j);

        Double alpha = -1.0, beta = 1.0;
        //compute the value of each available move
        for(MNKCell freeCell : FC){
            board.markCell(freeCell.i, freeCell.j);
            Double currentCellValue = alphaBeta(board, (board.currentPlayer() == player) ? true : false, alpha, beta);

            if(currentCellValue > optimalValue){
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
            valueMap[freeCell.i][freeCell.j] = currentCellValue.toString();
        }

        //prints out the board of marked cells and estimates of winning (for debugging)
        /*System.out.println("________GW________");
        for(String[] row : valueMap){
            for(String element : row){
                System.out.print(element+"      ");
            }
            System.out.println();
        }
        System.out.println("________GW________");
*/
        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}