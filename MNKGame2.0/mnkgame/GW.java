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
/*
    public MNKCellState intToMNKCellState(int player){
        switch(player){
            case 0: return MNKCellState.P1;
            case 1: return MNKCellState.P2;
            default: return MNKCellState.FREE;
        }
    }
    public int MNKCellStateToInt(MNKCellState state){
        switch{
            case MNKCellState.P1:{ return 0;}
            case MNKCellState.P2: {return 1;}
            default: {return 2;}
        }
    }
    */
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

    /**
     * Assigns a value \in [-1, 1] to <code> cell </code>.
     * The formula used is (# marked cell in alignment + 1) / k     +       (# times the cell appears in all the winning alignments / 4*k*k)    
     * @param cell The cell to be evaluated
     * @param currentAligment The alignment (set of cells) the cell belongs to
     * @param winningAlignments The set of all alignments for the board in this state
     * @param k The number of marked cell in a row or column or diagonal that produces a win
     * @return The likeliness that the current player will win
     */
    public Double computeValue(MNKCell cell, HashSet<MNKCell> currentAligment, HashSet<HashSet<MNKCell>> winningAlignments){
        Integer k = board.K;
        
        //compute base value
        double value = 0.0;
        if(cell.state != MNKCellState.FREE) return -1.0;
        //cell must be free

        //count no. cells that are already marked
        Integer marked = 0;
        for(MNKCell currentCell : currentAligment){
            if(currentCell.state != MNKCellState.FREE) marked++;
        }//maybe we can use the built-in search functions if we can find a way to compare to cells based on who's marked it

        marked++; //this counts the parameter: cell
        value += (double)(marked) / (double)(k);

        //compute increment based on how many alignments this cell intersects
        Integer possibleAlignments = 4*k;
        Integer intersectedAlignments = 0;

        for(HashSet<MNKCell> alignment : winningAlignments){
            for(MNKCell currentCell : alignment){
                if(currentCell.equals(cell)) intersectedAlignments++;
            }
        }
        value += ((double)intersectedAlignments / (double)(possibleAlignments * k));
        return value;
    }
    //we could compute the values for the enemy's alignment and compute our values accordingly
    //e.g. if our move has a high win probaiblity for our player but also has a high win probability for the opponent we should consider playing other moves
    //the logic could be better understood with visuals and pen and paper

    /**
     * Discards alignments that are out of bounds and alignments that don't produce a win for the considered player
     * @param allPossibleAligments
     *//*
    public void filter(HashSet<HashSet<MNKCell>> allPossibleAligments){
        for(HashSet<MNKCell> alignment : allPossibleAligments){
            if(!isInBoard(alignment)) allPossibleAligments.remove(alignment);
            if(containsMark( enumfor opponent)) allPossibleAligments.remove(alignment);
        }
    }*/
    
    /**
     * Returns true if the all cells are in the bounds of the board
     * @param alignment The alignment to be considered
     * @return true if the all cells are in the bounds of the board
     */
    public boolean isInBoard(HashSet<MNKCell> alignment){
        Integer m = board.M, n = board.N;
        for(MNKCell cell : alignment){
            if(cell.i >= 0 && cell.i < m && cell.j >= 0 && cell.j < n) continue;
            else return false;
        }
        return true;
    }

    /**
     * checks if a given alignment contains marks of the other player
     * @param player current player
     * @param alignment 
     * @return true, if the alignment contains mark of the other player
     */
    public boolean containsMark(int player, HashSet<MNKCell> alignment){
        boolean containsMarkOfOtherPlayer = true;

        for (MNKCell cell : alignment) {
            if (MNKCellStateToInt(cell.state) != player) {
                containsMarkOfOtherPlayer = true;
                break;
            }
        }

        return containsMarkOfOtherPlayer;
    }

    /**
     * This is an Evaluation function.
     * It seeks to predict the likeliness that the state of an open game will result in a win for the player that will play the next move.
     * It does so by computing the likeliness that selecting a free cell will result in a win and returning the value of the best move the player has.
     * @param b The board, with an MNKGameState.OPEN, that we want to evaluate. 
     * @return The likeliness that the current player will win
     *//*
    public Double heuristic(MNKBoard b){
        //get all possible aligments for both players
        HashSet<HashSet<MNKCell>> allPossibleAligmentsGW = new HashSet<>();
        //HashSet<HashSet<MNKCell>> allPossibleAligmentsOpponent = new HashSet<>();
        allPossibleAligmentsGW = getAllWinningAliments(GW);
        //allPossibleAligmentsOpponent = getAllWinningAliments(opponent);

        //discard the ones that are out of the board and the ones that don't produce a win for the considered player
        filter(allPossibleAligmentsGW);
        //filter(allPossibleAligmentsOpponent);

        //
        Double optimalCellValue = -1000.0;
        for(HashSet<MNKCell> alignment : allPossibleAligmentsGW){
            for(MNKCell cell : alignment){
                Double currentCellValue = computeValue(cell, alignment, allPossibleAligmentsGW, b.K);
                if(currentCellValue > optimalCellValue) optimalCellValue = currentCellValue;
            }
        }
        return optimalCellValue;
    }





    /**
     * creates a set of all possible winning alignments without consideration of other marked cells
     * @param board 
     * @return set of all possible winning alignments
     */
    public HashSet<HashSet<MNKCell>> getAllWinningAliments(MNKBoard board) {
        HashSet<HashSet<MNKCell>> allAligments = new HashSet<>();
        for (MNKCell markedCell : board.getMarkedCells()) {
            // consider only the alignments of the current player
            if ((markedCell.state.equals(MNKCellState.P1) && board.currentPlayer() == 0) || (markedCell.state.equals(MNKCellState.P2) && board.currentPlayer() == 1)) {
            // find all possible alignments that contain markedCell
                for (int x = 0; x < board.K; x++) {
                    HashSet<MNKCell> horizontalAlignment = new HashSet<>();
                    HashSet<MNKCell> verticalAlignemnt = new HashSet<>();
                    HashSet<MNKCell> diagonalAlignment1 = new HashSet<>();
                    HashSet<MNKCell> diagonalAlignment2 = new HashSet<>();
                    for (int a = x-board.K+1; a <= x+board.K-1; a++) {
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
        }

        return allAligments;
    }
    //considering opponents best moves was not added yet



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