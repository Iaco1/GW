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

    //evaluate will now always expect you to have evaluated the threats in all previous turns except the last one
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

        if (stateVictories != opponentVictories)
            return Integer.max(stateVictories, opponentVictories) * victoryParam;

        MNKCellState opponent = Player.getOpponent(state);
        b.updateThreats(1);

        //adjusting the evalutation formula
        int[] playerThreatSize = b.getPlayerThreats(state).getThreatSize(b.K);
        int[] opponentThreatSize = b.getPlayerThreats(opponent).getThreatSize(b.K);
        
        return 
        (playerThreatSize[0] * km1otStateParam
        + playerThreatSize[1] * km1hotStateParam
        + playerThreatSize[2] * km2otStateParam)
        
        - 
        (opponentThreatSize[0] * km1otOpponentParam
        + opponentThreatSize[1] * km1hotOpponentParam
        + opponentThreatSize[2] * km2otOpponentParam);

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
        if (MC.length > 0)
            board.markCell(MC[MC.length - 1].i, MC[MC.length - 1].j);

        // compute the value of each available move
        MNKCell optimalCell = alphaBetaDriver(FC, MC);

        board.markCell(optimalCell.i, optimalCell.j);

        return optimalCell;
    }

    public String playerName() {
        return "GW";
    }
}