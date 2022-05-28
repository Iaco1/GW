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
    public Integer alphaBeta(MNKBoard b, boolean max, Integer alpha, Integer beta, int goalDepth, int currentDepth) {
        Integer eval;
        if (b.gameState != MNKGameState.OPEN || currentDepth == goalDepth)
            return evaluate(board, player.state());
        else if (max) {
            eval = Integer.MIN_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                eval = Integer.max(eval, alphaBeta(b, false, alpha, beta, goalDepth, currentDepth + 1));
                b.unmarkCell();
                alpha = Integer.max(eval, alpha);
                if (alpha >= beta)
                    break;
            }
        } else {
            eval = Integer.MAX_VALUE;
            for (MNKCell freeCell : b.getFreeCells()) {
                b.markCell(freeCell.i, freeCell.j);
                eval = Integer.min(eval, alphaBeta(b, true, alpha, beta, goalDepth, currentDepth + 1));
                b.unmarkCell();
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
     * @param first True if GW is P1
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

        board.setEvaluatedThreats(MNKCellState.P1, new EvaluatedThreats());
        board.setEvaluatedThreats(MNKCellState.P2, new EvaluatedThreats());
    }

    /**
     * Driver method to select the best cell \in FC
     */
    public MNKCell alphaBetaDriver(MNKCell[] FC, MNKCell[] MC) {
        // optimal cell intitalization
        Integer optimalValue = Integer.MIN_VALUE;
        MNKCell optimalCell = FC[0];

        // alpha-beta value intialization
        Integer alpha = Integer.MIN_VALUE, beta = Integer.MAX_VALUE;

        // running alpha beta on all free cells and memorizing the optimal cell to be
        // marked
        for (MNKCell freeCell : FC) {
            board.markCell(freeCell.i, freeCell.j);
            Integer currentCellValue = alphaBeta(board, player.num() == board.currentPlayer(), alpha,
                    beta, 5,
                    0);

            if (currentCellValue > optimalValue) {
                optimalValue = currentCellValue;
                optimalCell = freeCell;
            }
            board.unmarkCell();
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
                    // if the threat is of the desired size and is open add it to the correct container object
                    if (threatSize == size && threatIter.state == MNKCellState.FREE && board.contains(threatIter)) {
                        Threat threatInterval = new Threat(cell, threatIter, axis, state);
                        
                        //assign to playerThreats or opponentThreats
                        addThreat(threatInterval, ThreatType.OPEN, size);
                        
                        ot++;
                    }
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
    

    public void addThreat(Threat t, ThreatType tt, int size){
        //check if threat is redundant
        if(board.isRedundant(t, tt, size)) return;

        board.getEvaluatedThreats(t.player).add(t, tt, board.K, size);
    }

    /**
     * Adds the k-1 half open threats to evaluatedThreats for the selected player
     * @return
     */
    public int getHalfOpenThreatsByAxis(Board b, MNKCellState state, Axis axis){
        int hot = 0; //it's the no. of Half Open Threats, don't get me wrong
        for(int i=0; i < b.M; i++){
            for(int j=0; j < b.N; j++){
                MNKCell cell = b.getCellAt(i, j);
                Direction searchDirection = Threat.getSearchDirection(axis);
                //start looking for k-2 cells on this axis
                if(cell.state == state){
                    MNKCell leftExtremity = b.getAdjacentCell(cell, Threat.getOppositeDirection(searchDirection));
                    int alignedCells = 0, jumps = 0;
                    //keep searching until you have a k-1 threat or you find an opponent's mark or you go out of bounds
                    while(alignedCells < b.K-1 && jumps < 2 && cell.state != Player.getOpponent(state) && b.contains(cell)){
                        if(cell.state == MNKCellState.FREE) jumps++;
                        if(cell.state == state) alignedCells++;

                        cell = b.getAdjacentCell(cell, searchDirection);
                    }

                    boolean zeroExtMarkedByPlayer = leftExtremity.state != state && cell.state != state;
                    boolean exactlyOneExtIsFreeAndInBounds = (leftExtremity.state == MNKCellState.FREE && b.contains(leftExtremity)) ^ (cell.state == MNKCellState.FREE && b.contains(cell));
                    boolean leqOneExtMarkedByPlayer = !(leftExtremity.state == state && cell.state == state);
                    if(alignedCells != b.K-1 ) continue;
                    else if(jumps > 1) continue;
                    else if(
                    (jumps == 1 && leqOneExtMarkedByPlayer) // has k-1 aligned cells, 1 jump and extremities are not of our MNKCellState
                    || //or has k-1 aligned cells and only one of the extremities is a free cell while the other one is not our cell
                    (jumps == 0 && exactlyOneExtIsFreeAndInBounds && zeroExtMarkedByPlayer)
                    ){ 
                        Threat threatInterval = new Threat(leftExtremity, cell, axis, state);

                        //assign to playerThreats or opponentThreats
                        addThreat(threatInterval, ThreatType.HALF_OPEN, b.K-1);

                        hot++;
                    }
                }

            }
        }
        return hot;
    }
    
    public int getHalfOpenThreats(Board b, MNKCellState state){
        int hot=0;
        for(Axis axis : Axis.values()){
            hot += getHalfOpenThreatsByAxis(b, state, axis);
        }
        return hot;
    }
    
    public int evaluate(Board b, MNKCellState state){
        final int victoryParam = 1000000;
        
        final int km1otStateParam = 250;
        final int km1hotStateParam = 80;
        final int km2otStateParam = 100;

        final int km1otOpponentParam = 5020;
        final int km1hotOpponentParam = 2000;
        final int km2otOpponentParam = 1300;
        
        int stateVictories = getVictories(b, state);
        int opponentVictories = getVictories(b, Player.getOpponent(state));

        if(stateVictories != opponentVictories) return Integer.max(stateVictories, opponentVictories)*victoryParam;

        MNKCellState opponent = Player.getOpponent(state);
        return 
        ( getOpenThreats(b, state, b.K-1)*km1otStateParam + getHalfOpenThreats(b, state)*km1hotStateParam + getOpenThreats(b, state, b.K-2)*km2otStateParam )
        - ( getOpenThreats(b, opponent, b.K-1)*km1otOpponentParam + getHalfOpenThreats(b, opponent)*km1hotOpponentParam + getOpenThreats(b, opponent, b.K-2)*km2otOpponentParam);

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
        if (MC.length > 0) board.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);

        // compute the value of each available move
        MNKCell optimalCell = alphaBetaDriver(FC, MC);

        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}