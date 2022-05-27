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
    final Double MIN = -1000000000.0;
    protected DebugBoard debugBoard;



    public MNKCell iterativeDeepening(int itDepthMax) {

        double initialTime = System.currentTimeMillis(); // in ms

        int itDepth = 0;
        MNKCell optimalCell = this.board.getFreeCells()[0];

        while((System.currentTimeMillis() - initialTime) / 1000.0 < timeout-1) { // until time limit is reached
            optimalCell = depthLimitedSearch(this.board, itDepth, itDepthMax);
            itDepth += 1;
            //System.out.println(itDepth);
            //System.out.println((System.currentTimeMillis() - initialTime)/1000.0);
        }

        return optimalCell;
    }



    public MNKCell depthLimitedSearch(MNKBoard b, int depth, int itDepthMax) {
        MNKCell optimalCell = b.getFreeCells()[0];

        for(MNKCell freeCell : b.getFreeCells()) {
            if (depth < itDepthMax) {
                b.markCell(freeCell.i, freeCell.j);
                if (b.gameState.equals(MNKGameState.OPEN)) {
                    optimalCell = depthLimitedSearch(b, depth+1, itDepthMax);
                }
                b.unmarkCell();
            } else {
                return alphaBetaDriver(b.getFreeCells(), b.getMarkedCells());
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

    public int getVictories(MNKBoard b, MNKCellState state) {
        switch (state) {
            case P1: {
                return (b.gameState == MNKGameState.WINP1) ? 1 : 0;
            }
            case P2: {
                return (b.gameState == MNKGameState.WINP2) ? 1 : 0;
            }
            default: {
                return 0;
            }
        }
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
    public Double alphaBeta(MNKBoard b, boolean max, double alpha, double beta, int goalDepth, int currentDepth) {
        Double eval;
        if (b.gameState != MNKGameState.OPEN || currentDepth == goalDepth)
            return evaluateEndGame(board);
        else if (max) {
            eval = MIN;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.max(eval, alphaBeta(b, false, alpha, beta, goalDepth, currentDepth + 1));
                b.unmarkCell();
                alpha = Double.max(eval, alpha);
                if (alpha >= beta)
                    break;
            }
        } else {
            eval = Double.MAX_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                eval = Double.min(eval, alphaBeta(b, true, alpha, beta, goalDepth, currentDepth + 1));
                b.unmarkCell();
                beta = Double.min(eval, beta);
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

        debugBoard = new DebugBoard(M, N);

        board.playerThreats = new EvaluatedThreats();
        board.opponentThreats = new EvaluatedThreats();
    }

    /**
     * Driver method to select the best cell \in FC
     */
    public MNKCell alphaBetaDriver(MNKCell[] FC, MNKCell[] MC) {
        // optimal cell intitalization
        Double optimalValue = MIN;
        MNKCell optimalCell = FC[0];

        // alpha-beta value intialization
        Double alpha = -1.0, beta = 1.0;

        // running alpha beta on all free cells and memorizing the optimal cell to be
        // marked
        for (MNKCell freeCell : FC) {
            board.markCell(freeCell.i, freeCell.j);
            Double currentCellValue = alphaBeta(board, (board.currentPlayer() == player.num()) ? true : false, alpha,
                    beta, 5,
                    0);

            if (currentCellValue > optimalValue) {
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
            debugBoard.assignValue(currentCellValue, freeCell);
        }
        return optimalCell;
    }

    //add support to add threats for the enemy
    //improve diagonal and antidiagonal search 
    public int getOpenThreatsByAxis(Board board, MNKCellState state, int size, Axis axis) {
        int ot = 0;
        for (int i = 0; i < board.M; i++) {
            for (int j = 0; j < board.N; j++) {
                MNKCell cell = new MNKCell(i, j, board.cellState(i, j));
                // find a free cell, marked cell pair in this axis' search direction
                Direction searchDirection = Threat.getSearchDirection(axis);
                if (cell.state == MNKCellState.FREE && board.getAdjacentCell(cell, searchDirection).state == state) {
                    MNKCell threatIter = board.getAdjacentCell(cell, searchDirection);
                    int threatSize = 0;
                    // count the no. of marked cells after the pair
                    while (board.contains(threatIter) && threatIter.state == state && threatSize < size) {
                        threatSize++;
                        threatIter = board.getAdjacentCell(threatIter, searchDirection);
                    }
                    // if the threat is of the desired size and is open add it to the correct object
                    if (threatSize == size && threatIter.state == MNKCellState.FREE && board.contains(threatIter)) {
                        Threat threatInterval = new Threat(cell, threatIter, axis);
                        
                        //assign to playerThreats or opponentThreats
                        if(state == player.state()) board.playerThreats.add(threatInterval, ThreatType.OPEN, board.K, size);
                        else board.opponentThreats.add(threatInterval, ThreatType.OPEN, board.K, size);
                        
                        ot++;
                    }
                    cell = threatIter; // restart search from last searched cell
                }
            }
        }
        return ot;
    }

    public int getOpenThreats(Board b, MNKCellState state, int size){
        int ot = 0;
        for(Axis axis : Axis.values()){
            ot += getOpenThreatsByAxis(b, state, size, axis); 
        }
        return ot;
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
        // and assigns a mark to the previously marked cells in evBoard
        if (MC.length > 0) {
            board.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
            debugBoard.assignMark(MC[MC.length - 1]);
        }

        // compute the value of each available move
        //MNKCell optimalCell = alphaBetaDriver(FC, MC);
        MNKCell optimalCell = iterativeDeepening(1);

        // prints out the board of marked cells and estimates of winning (for debugging)
        debugBoard.printBoard(MC.length);
        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}