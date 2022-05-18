package mnkgame;

import java.util.HashSet;

/**
 * Draft implementation of an mnk-player
 * @author Davide Iacomino
 * @author Leonie Brockmann
 */
public class GW implements MNKPlayer {
    protected MNKBoard board;
    protected int timeout;
    protected int player;
    final Double MIN = -1000000000.0;
    HashSet<HashSet<MNKCell>> allWinningAlignments;
    protected EvaluationBoard evBoard;

    
    public MNKCellState getOpponent(MNKCellState state){
        switch(state){
            case P1: { return MNKCellState.P2; }
            case P2: { return MNKCellState.P1; }
            default: { return MNKCellState.FREE;} //Should I return an Exception here?
        }
    }

    /**
     * Casting method
     * @param player
     * @return P1 for 0, P2 for 1, FREE by default
     * @author Davide Iacomino
     */
    public MNKCellState intToMNKCellState(int player){
        switch(player){
            case 0: return MNKCellState.P1;
            case 1: return MNKCellState.P2;
            default: return MNKCellState.FREE;
        }
    }
    /**
     * Casting method
     * @param state
     * @return 0 for P1, 1 for P2, 2 as default
     * @author Davide Iacomino
     */
    public int MNKCellStateToInt(MNKCellState state){
        switch(state){
            case P1:{ return 0;}
            case P2:{ return 1;}
            default:{ return 2;}
        }
    }

    /**
     * placeholder evaluation function for the end-game
     * @param board
     * @return 1 if GW won, 0 if the game ended in a draw, -1 if the opponent won
     * @author Davide Iacomino
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
     * An implementation of the alpha-beta pruning algorithm for an mnk-game.
     * It calculates the likeliness of winning based on the state of the board.
     * @param b The Board to analyze
     * @param max Whether it is the player's turn or not
     * @param alpha The minimum attainable score for the player
     * @param beta The maximum attainable score for the adversary
     * @return The value of the current move
     * @author Davide Iacomino
     */
    public Double alphaBeta(MNKBoard b, boolean max, double alpha, double beta, int goalDepth, int currentDepth){
        Double eval;
        if(b.gameState != MNKGameState.OPEN || currentDepth == goalDepth) return heuristic(b);
        else if(max){
            eval = MIN;
            for(MNKCell freeCell : b.getFreeCells()){
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.max(eval, alphaBeta(b, false, alpha, beta, goalDepth, currentDepth+1));
                b.unmarkCell();
                alpha = Double.max(eval, alpha);
                if(alpha >= beta) break;
            }
        }else{
            eval = Double.MAX_VALUE;
            for(MNKCell freeCell : b.getFreeCells()){
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.min(eval, alphaBeta(b, true, alpha, beta, goalDepth, currentDepth+1));
                b.unmarkCell();
                beta = Double.min(eval, beta);
                if(alpha >= beta) break;
            }
        }
        return eval;
    }

    /**
     * @param M 
     * @param N
     * @param K 
     * @param first True if GW is P1
     * @param timeout_in_secs
     * @author Davide Iacomino
     */
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        board = new MNKBoard(M, N, K);
        timeout = timeout_in_secs;

        player = (first) ? 0 : 1;

        evBoard = new EvaluationBoard(M, N);
    }

    /**
     * Assigns a value \in [-1, 1] to <code> cell </code>.
     * The formula used is (# marked cell in alignment + 1) / k     +       (# times the cell appears in all the winning alignments / 4*k*k)    
     * @param cell The cell to be evaluated
     * @param currentAlignment The alignment (set of cells) the cell belongs to
     * @param winningAlignments The set of all alignments for the board in this state
     * @param k The number of marked cell in a row or column or diagonal that produces a win
     * @return The likeliness that the current player will win
     * @author Davide Iacomino
     */
    public Double computeValue(MNKCell cell, HashSet<MNKCell> currentAlignment, HashSet<HashSet<MNKCell>> winningAlignments){
        Integer k = board.K;

        //compute base value
        double value = 0.0;
        if(cell.state != MNKCellState.FREE) return -1.0;
        //cell must be free

        //count no. cells that are already marked
        Integer marked = 0;
        for(MNKCell currentCell : currentAlignment){
            if(currentCell.state != MNKCellState.FREE) marked++;
        }//maybe we can use the built-in search functions if we can find a way to compare to cells based on who's marked it

        marked++; //this counts the parameter: cell
        value += (double)(marked) / (double)(k);

        //compute increment based on how many alignments this cell intersects
        Integer possibleAlignments = 4*k;
        Integer intersectedAlignments = 0;

        for(HashSet<MNKCell> alignment : winningAlignments){
            for(MNKCell currentCell : alignment){
                //alignment.contains(cell) intersectedAlignments++;
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
     * @param cell The cell to evaluate
     * @return True if it's not an opponents' cell and is in the bounds of the board
     * @author Davide Iacomino
     */
    public boolean isAlignable(MNKCell pivot, MNKCell cell){
        return (cell.state != getOpponent(pivot.state) && isInBoard(cell));
    }
    

   


    /**
     * @param cell 
     * @return true if the cell is in the bounds of the board
     * @author Davide Iacomino
     */
    public boolean isInBoard(MNKCell cell){
        Integer m = board.M, n = board.N;
        return (cell.i >= 0 && cell.i < m && cell.j >= 0 && cell.j < n);
    }


    
    /**
     * This is an Evaluation function.
     * It seeks to predict the likeliness that the state of an open game will result in a win for the player that will play the next move.
     * It does so by computing the likeliness that selecting a free cell will result in a win and returning the value of the best move the player has.
     * @param b The board, with an MNKGameState.OPEN, that we want to evaluate. 
     * @return The likeliness that the current player will win
     * @author Davide Iacomino
     */
    public Double heuristic(MNKBoard b){
        //get all possible alignments for both players
        HashSet<HashSet<MNKCell>> allPossibleAlignmentsGW = new HashSet<>();
        createAllWinningAlignments(intToMNKCellState(player));
        allPossibleAlignmentsGW = this.allWinningAlignments;
        
        Double optimalCellValue = -1000.0;
        for(HashSet<MNKCell> alignment : allPossibleAlignmentsGW){
            for(MNKCell cell : alignment){
                Double currentCellValue = computeValue(cell, alignment, allPossibleAlignmentsGW);
                if(currentCellValue > optimalCellValue) optimalCellValue = currentCellValue;
            }
        }
        return optimalCellValue;
    }

    /**
     * The board is scanned:
     * left to right (W to E) for alignments on the HORIZONTAL axis
     * top to bottom (N to S) for alignments on the VERTICAL
     * top left to bottom right (NW to SE)
     * top right to bottom left (NE to SW)
     * @param axis The axis on which to scan the board for winning alignments
     * @return The Direction that guides the scanning of the board for winning alignments
     */
    Direction getSearchDirection(Axis axis){
        switch(axis){
            case HORIZONTAL: { return Direction.E; }
            case VERTICAL: { return Direction.S; }
            case NW_SE: { return Direction.SE; }
            case NE_SW: { return Direction.SW; }
            default: { return null; }
        }
    }

    /**
     * MNKBorad.cellState() throws an index out of bounds exception for cells outside the board
     * @param i
     * @param j
     * @return the cell in (i,j) or if it's out of the board a free cell in (i,j)
     */
    MNKCell getCellAt(int i, int j){
        MNKCell cell = new MNKCell(i, j, MNKCellState.FREE);
        if(isInBoard(cell)) cell = new MNKCell(i, j, board.cellState(i, j));
        return cell;
    }
    
    /**
     * It can return out of bounds cells and cells that belong to the opponent
     * @param pivot The cell containing the starting position
     * @param direction The direction to scan for the adjacent cell
     * @return The cell that is adjacent to pivot in the given direction
     */
    public MNKCell getAdjacentCell(MNKCell pivot, Direction direction){
        if(pivot == null) return null;
        
        int i = pivot.i, j = pivot.j;
        switch(direction){
            case N: {i--; break;}
            case NE: {i--; j++; break; }
            case E: {j++; break; }
            case SE: {i++; j++; break; }
            case S: {i++; break; }
            case SW: {i++; j--; break; }
            case W: {j--; break; }
            case NW: {i--; j--; break; }
            default: break;
        }

        return getCellAt(i, j);
    }

    /**
     * @param pivot
     * @param k
     * @param axis
     * @return The cell on the axis k-1 cells away from the pivot from which to start scanning for alignments
     */
    public MNKCell getStartingCell(MNKCell pivot, int k, Axis axis){
        int i = pivot.i, j = pivot.j;
        switch(axis){
            case HORIZONTAL:{
                j -= (k-1);
                break;
            }
            case VERTICAL:{
                i -= (k-1);
                break;
            }
            case NW_SE:{
                i -= (k-1); j -= (k-1);
                break;
            }
            case NE_SW:{
                i -= (k-1); j += (k-1);
                break;
            }
            default:{
                break;
            }
        }
        return getCellAt(i, j); 
    }

    /**
     * gets all the alignemnets that can lead to a win for the selected player
     * assumes the pivot is not a free cell
     * @param pivot
     * @param k
     * @param axis
     * @return
     */
    public HashSet<HashSet<MNKCell>> getAlignment(MNKCell pivot, int k, Axis axis){
        HashSet<HashSet<MNKCell>> axisAlignments = new HashSet<>();
        MNKCell startingCell = getStartingCell(pivot, k, axis);
        Direction searchDirection = getSearchDirection(axis);
        MNKCell iterator = startingCell;

        for(int alignmentNo=0; alignmentNo < k; alignmentNo++){

            HashSet<MNKCell> alignment = new HashSet<>();
            for(int alignedCells=0; alignedCells < k; alignedCells++){
                if(isAlignable(pivot, iterator)) alignment.add(iterator);
                else break;

                iterator = getAdjacentCell(iterator, searchDirection);
            }
            if(alignment.size() == k) axisAlignments.add(alignment);
            startingCell = getAdjacentCell(startingCell, searchDirection);
            iterator = startingCell;
        }
        return axisAlignments;
    }

    /**
     * creates a set of all possible winning alignments without consideration of other marked cells
     * @author Leonie Brockmann
     */
    public void createAllWinningAlignments(MNKCellState playerState) {
        allWinningAlignments = new HashSet<>();
        for(MNKCell markedCell : board.getMarkedCells()){
            if(markedCell.state == playerState){
                allWinningAlignments.addAll(getAlignment(markedCell, board.K, Axis.HORIZONTAL));
                allWinningAlignments.addAll(getAlignment(markedCell, board.K, Axis.VERTICAL));
                allWinningAlignments.addAll(getAlignment(markedCell, board.K, Axis.NW_SE));
                allWinningAlignments.addAll(getAlignment(markedCell, board.K, Axis.NE_SW));
            }
        }
    }
    //considering opponents best moves was not added yet

    /**
     * Driver class to select the best cell \in FC
     */
    public MNKCell alphaBetaDriver(MNKCell[] FC, MNKCell[] MC){
        //optimal cell intitalization
        Double optimalValue = MIN;
        MNKCell optimalCell = FC[0];

        //alpha-beta value intialization
        Double alpha = -1.0, beta = 1.0;

        //running alpha beta on all free cells and memorizing the optimal cell to be marked
        for(MNKCell freeCell : FC){
            board.markCell(freeCell.i, freeCell.j);
            Double currentCellValue = alphaBeta(board, (board.currentPlayer() == player) ? true : false, alpha, beta, 5, 0);

            if(currentCellValue > optimalValue){
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
            evBoard.assignValue(currentCellValue, freeCell);
        }
        return optimalCell;
    }

    /**
     * Our current best guess for how to win any game
     * @param FC
     * @param MC
     * @author Davide Iacomino
     */
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        //mark last played cell by the adversary 
        //and assigns a mark to the previously marked cells in evBoard
        if(MC.length > 0){ 
            board.markCell(MC[MC.length-1].i, MC[MC.length-1].j);
            evBoard.assignMark(MC[MC.length-1]);
        }

        //compute the value of each available move
        MNKCell optimalCell = alphaBetaDriver(FC, MC);

        //prints out the board of marked cells and estimates of winning (for debugging)
        evBoard.printBoard(MC.length);
        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }

    public void setBoard(MNKBoard b) {
        this.board = b;
    }
}