package mnkgame;

/**
 * Draft implementation of an mnk-player
 * 
 * @author Davide Iacomino
 * @author Leonie Brockmann
 */
public class GW implements MNKPlayer {
    protected Board board;
    protected int timeout;
    Player player;



    public MNKCell iterativeDeepening(int itDepthMax) {

        double initialTime = System.currentTimeMillis(); // in ms

        int itDepth = 0;
        MNKCell optimalCell = this.board.getFreeCells()[0];

        while(( ((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-1) && (itDepth <= itDepthMax)) { // until time limit is reached
            //optimalCell = depthLimitedSearch(this.board, itDepth, itDepthMax);
            optimalCell = alphaBetaDriver(itDepth, itDepthMax, initialTime);
            itDepth += 1;
            //System.out.println(itDepth);
            //System.out.println((System.currentTimeMillis() - initialTime)/1000.0);
        }

        return optimalCell;
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
                return alphaBetaDriver(depth, itDepthMax, 0); // 0 placeholder value
            }
        }

        return optimalCell;
    }


    /**
     * placeholder evaluation function for the end-game
     * 
     * @param board
     * @return 1 if GW won, 0 if the game ended in a draw, -1 if the opponent won
     * @author Davide Iacomino
     */
    public Double evaluateEndGame(MNKBoard board) {
        if (Player.won(player.state(), board.gameState))
            return 1.0;
        if (Player.won(Player.getOpponent(player.state()), board.gameState))
            return -1.0;
        else
            return 0.0;
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
     * @author Davide Iacomino
     */
    public Integer alphaBeta(Board b, boolean max, Integer alpha, Integer beta, int goalDepth, int currentDepth, double initialTime) {
        Integer eval;
        if (b.gameState != MNKGameState.OPEN || currentDepth >= goalDepth || !(((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-1))
            return evaluate(board, player.state());
        else if (max) {
            eval = Integer.MIN_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                b.updateThreats(b.getCellAt(freeCell.i, freeCell.j));
                eval = Integer.max(eval, alphaBeta(b, false, alpha, beta, goalDepth, currentDepth + 1, initialTime));
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
                eval = Integer.min(eval, alphaBeta(b, true, alpha, beta, goalDepth, currentDepth + 1, initialTime));
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
     * @param M
     * @param N
     * @param K
     * @param first           True if GW is P1
     * @param timeout_in_secs
     * @author Davide Iacomino
     */
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        board = new Board(M, N, K);
        timeout = timeout_in_secs;

        if (first)
            player = new Player(0);
        else
            player = new Player(1);
    }

    /**
     * Driver method to select the best cell \in FC
     */
    public MNKCell alphaBetaDriver(int currentDepth, int goalDepth, double initialTime) {
        // optimal cell intitalization
        Integer optimalValue = Integer.MIN_VALUE;
        MNKCell optimalCell = board.getFreeCells()[0];

        // alpha-beta value intialization
        Integer alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;

        // running alpha beta on all free cells and memorizing the optimal cell to be
        // marked
        for (MNKCell freeCell : board.getFreeCells()) {
            if(!(((System.currentTimeMillis() - initialTime) / 1000.0) < timeout-1)) return optimalCell;
            board.markCell(freeCell.i, freeCell.j);
            board.updateThreats(board.getCellAt(freeCell.i, freeCell.j));
            Integer currentCellValue = alphaBeta(board, player.num() == board.currentPlayer(), alpha, beta, goalDepth, currentDepth, initialTime);

            if (currentCellValue > optimalValue) {
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
            board.updateThreats(board.getCellAt(freeCell.i, freeCell.j));
        }
        return optimalCell;
    }

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
     * Our current best guess for how to win any game
     * 
     * @param FC
     * @param MC
     * @author Davide Iacomino
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