package mnkgame;

/**
 * Describes the player's number, gives information about the opponent, some
 * casting methods
 */
public class Player {
    private int num;

    Player() {
    }

    Player(int n) {
        num = MNKCellStateToInt(intToMNKCellState(n));
    }

    Player(MNKCellState state) {
        num = MNKCellStateToInt(state);
    }

    /**
     * Casting method
     * 
     * @param state
     * @return 0 for P1, 1 for P2, 2 as default
     */
    private static int MNKCellStateToInt(MNKCellState state) {
        switch (state) {
            case P1: {
                return 0;
            }
            case P2: {
                return 1;
            }
            default: {
                return 2;
            }
        }
    }

    /**
     * Casting method
     * 
     * @param player
     * @return P1 for 0, P2 for 1, FREE by default
     */
    private static MNKCellState intToMNKCellState(int player) {
        switch (player) {
            case 0:
                return MNKCellState.P1;
            case 1:
                return MNKCellState.P2;
            default:
                return MNKCellState.FREE;
        }
    }

    public int num() {
        return num;
    }

    public MNKCellState state() {
        return intToMNKCellState(num);
    }

    public static MNKCellState getOpponent(MNKCellState state) {
        switch (state) {
            case P1: {
                return MNKCellState.P2;
            }
            case P2: {
                return MNKCellState.P1;
            }
            default: {
                return MNKCellState.FREE;
            } // Should I return an Exception here?
        }
    }

}
