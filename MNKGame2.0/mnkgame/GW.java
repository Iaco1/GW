package mnkgame;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Draft implementation of an mnk-player
 * 
 * @author Davide Iacomino
 * @author Leonie Brockmann
 */
public class GW implements MNKPlayer {
    protected Board board;
    protected int timeout;
    protected int insurance;
    Player player;

    /**
     * @param M no. of rows in the board
     * @param N no. of columns in the board
     * @param K no. of cells to align in the board
     * @param first whether this player is P1 or not
     * @param timeout_in_secs the time this player has for returning a move
     */
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        board = new Board(M, N, K);
        timeout = timeout_in_secs;
        if(M*N >= 2500) insurance = 2;
        else insurance = 1; 

        if (first)
            player = new Player(0);
        else
            player = new Player(1);
    }

    public MNKCell depthLimitedSearch(Board b, int depth, int itDepthMax) {
        MNKCell optimalCell = b.getFreeCells()[0];

        for(MNKCell freeCell : b.getFreeCells()) {
            if (depth < itDepthMax) {
                b.markCell(freeCell.i, freeCell.j);
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                if (b.gameState.equals(MNKGameState.OPEN)) {
                    optimalCell = depthLimitedSearch(b, depth+1, itDepthMax);
                }
                b.unmarkCell();
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
            } else {
                return searchDriver(itDepthMax, 0, new LinkedList<>()); // 0 placeholder value
            }
        }

        return optimalCell;
    }

    /**
     * Driver algorithm that performs null-window alphabeta calls dependent of a first good guess f
     * @param b The board to analyse
     * @param depth The search depth
     * @param f The initial guess of the value of alphabeta for the current node
     * @param initialTime the time when iterativeDeepening() was called
     * @return the value of the current board position
     */
    public Integer MTD(Board b, int depth, Integer f, double initialTime){
        Integer eval = f;
        Integer ub = Integer.MAX_VALUE, lb = Integer.MIN_VALUE;
        Integer beta;
        while(ub > lb){
            if(eval == lb) beta = eval + 1;
            else beta = eval; 
            eval = alphaBeta(b, b.currentPlayer == player.num(), beta-1, beta, depth, initialTime);
            
            if(eval < beta) ub = eval;
            else lb = eval;
        }
        return eval;
    }

    /**
     * The evaluation function that predicts how likely a player is to win given the state of the board
     * It gives the most value to k-1 open threat (which give the player 2 ways of winning in the next move)
     * It gives an intermediate value to k-2 open threat because they have the potential to become at least a k-1 half open threat and in the best case a k-1 open threat
     * It gives the least value to k-1 half open threat because they can be easily blocked in one move
     * @param b The board to analyse
     * @param state The player for which to return the likeliness of winning
     * @return A number between -1 000 000 and 1 000 000 from sure loss to sure win and everything in between
     */
    //evaluate will always expect you to have evaluated the threats in all previous turns
    public int evaluate(Board b, MNKCellState state) {
        final int victoryParam = 1000000;

        final int km1otStateParam = 250;
        final int km1hotStateParam = 80;
        final int km2otStateParam = 100;

        final int km1otOpponentParam = 5020;
        final int km1hotOpponentParam = 2000;
        final int km2otOpponentParam = 1300;

        int stateVictories = b.getVictories(state);
        int opponentVictories = b.getVictories(Player.getOpponent(state));

        if (stateVictories != opponentVictories){
            if(Integer.max(stateVictories, opponentVictories) == stateVictories) return victoryParam;
            else return -victoryParam;
        }

        int[] th = b.getNumberOfThreats();

        int p1 = th[0] * km1otStateParam + th[1] * km1hotStateParam + th[2] * km2otStateParam;
        int p2 = th[3] * km1otOpponentParam + th[4] * km1hotOpponentParam + th[5] * km2otOpponentParam;
        
        if(state == MNKCellState.P1) return p1 - p2;
        else return p2 - p1; 

    }

    /**
     * An implementation of the alpha-beta pruning algorithm for an mnk-game.
     * It calculates the likeliness of winning based on the state of the board.
     * 
     * @param b     The Board to analyze
     * @param max   Whether it is the player's turn or not
     * @param alpha The minimum attainable score for the player
     * @param beta  The maximum attainable score for the adversary
     * @return The value of the current move
     */
    public Integer alphaBeta(Board b, boolean max, Integer alpha, Integer beta, int depth, double initialTime) {
        Integer eval;
        if (b.gameState != MNKGameState.OPEN || depth == 0 || !(((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-insurance))
            return evaluate(board, player.state());
        else if (max) {
            eval = Integer.MIN_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                eval = Integer.max(eval, alphaBeta(b, false, alpha, beta, depth - 1, initialTime));
                b.unmarkCell();
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                alpha = Integer.max(eval, alpha);
                if (alpha >= beta)
                    break;
            }
        } else {
            eval = Integer.MAX_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                eval = Integer.min(eval, alphaBeta(b, true, alpha, beta, depth - 1, initialTime));
                b.unmarkCell();
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                beta = Integer.min(eval, beta);
                if (alpha >= beta)
                    break;
            }
        }
        return eval;
    }


    /**
     * Driver method to select the best cell \in FC
     * uses alphaBeta to determine the first best guess and MTD(f) for later guesses based on the first one
     * @param goalDepth The tree search depth
     * @param initialTime the time when iterativeDeepening() was called
     * @param interestingCells The list of cells that have a subtree we want to search
     * @return The deemed best move in the current state of the game
     */
    public MNKCell searchDriver(int goalDepth, double initialTime, LinkedList<MNKCell> interestingCells) {
        // optimal cell intitalization
        Integer optimalValue = Integer.MIN_VALUE;
        MNKCell optimalCell = board.getFreeCells()[0];

        // alpha-beta value intialization
        Integer alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;
        boolean firstSearch = true;


        // running alpha beta on all free cells and memorizing the optimal cell to be
        // marked
        for (MNKCell fc : interestingCells) {
            if(!(((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-insurance)) return optimalCell;
            board.markCell(fc.i, fc.j);
            board.updateThreats(board.getCellAt(fc.i, fc.j));
            Integer currentCellValue;

            if(firstSearch){
                currentCellValue = alphaBeta(board, player.num() == board.currentPlayer(), alpha, beta, goalDepth, initialTime);
                firstSearch = false;
            }else currentCellValue = MTD(board, goalDepth, optimalValue, initialTime);

            if (currentCellValue > optimalValue) {
                optimalValue = currentCellValue;
                optimalCell = fc;
            }
            board.unmarkCell();
            board.updateThreats(board.getCellAt(fc.i, fc.j));
        }
        return optimalCell;
    }

    /**
     * Handles the timeout restriction by performing deeper searches of the game tree at each iteration
     * It uses the previous best guess as the first node to search in the next iteration to increase pruning in the other branches
     * @param itDepthMax The upper boundary of the search
     * @return The deemed best move in the current state of the game
     */
    public MNKCell iterativeDeepening(int itDepthMax) {

        double initialTime = System.currentTimeMillis(); // in ms

        int itDepth = 0;
        MNKCell optimalCell = this.board.getFreeCells()[0];
        LinkedList<MNKCell> interestingCells = new LinkedList<>(Arrays.asList(board.getFreeCells()));
        if(board.MC.size() > 1) interestingCells = new LinkedList<>(board.contour());

        while(( ((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-insurance) && (itDepth <= itDepthMax)) { // until time limit is reached
            //optimalCell = depthLimitedSearch(this.board, itDepth, itDepthMax);
            optimalCell = searchDriver(itDepth, initialTime, interestingCells);
            interestingCells.remove(optimalCell);
            interestingCells.addFirst(optimalCell);
            itDepth += 1;
            //System.out.print(itDepth + ", ");
            //System.out.println((System.currentTimeMillis() - initialTime)/1000.0);
        }
        if(((System.currentTimeMillis() - initialTime) / 1000.0) > timeout-1) insurance+=2;

        return optimalCell;
    }


    /**
     * Our current best guess for how to win any game
     * It updates an extension of MNKBoard with marked cells and player threats
     * and calls iterativeDeepening() to return the cell with the highest value before the time is up
     * @return The cell that is more likely to lead to a win given the performed search
     */
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        // mark last played cell by the adversary
        if(MC.length == 1){
            MNKCell opponentCell = MC[MC.length-1];
            board.markCell(opponentCell.i, opponentCell.j);
            board.updateThreats(opponentCell);
        }
        else if (MC.length > 0){
            MNKCell playerCell = MC[MC.length-2];
            MNKCell opponentCell = MC[MC.length-1];
            board.markCell(playerCell.i, playerCell.j);
            board.markCell(opponentCell.i, opponentCell.j);
            board.updateThreats(playerCell);
            board.updateThreats(opponentCell);
        }
        
        MNKCell optimalCell = iterativeDeepening(board.K);
        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}