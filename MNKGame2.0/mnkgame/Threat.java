package mnkgame;
/**
 * Describes a sequence of cells on a certain axis that can lead to victory in k-i moves
 * with k being the number of cells to align to win the game
 * and i being how many cells the current threat lacks to get to size k
 * In this implementation, only k-1 and k-2 threats are considered
 * Left and right are the extremities of the threat and are always free, out of bounds or occupied by the enemy
 * The axis tells you the order in which to scan the threat
 */
public class Threat{
    public MNKCell left;
    public MNKCell right;
    public Axis axis;
    public MNKCellState player;

    public Threat(){}

    /**
     * no check on whether positions are in the board
     * no check on whether positions are actually on the same axis
     * @param a One extremity of the Threat
     * @param b the other extremity of the Threat
     * @param axis The axis on which the threat was found
     */
    public Threat(MNKCell a, MNKCell b, Axis axis, MNKCellState player) {
        this.axis = axis;
        switch(axis){
            case HORIZONTAL:{
                if(a.j <= b.j) { left = a; right = b; }
                else { left = b; right = a; }
                break;
            }
            case VERTICAL:{
                if(a.i <= b.i) { left = a; right = b; }
                else { left = b; right = a; }
                break;
            }
            case NW_SE: {
                if(a.i <= b.i && a.j <= b.j){ left = a; right = b; }
                else { left = b; right = a; }
                break;
            }
            case NE_SW: {
                if(a.i <= b.i && a.j >= b.j){ left = a; right = b; }
                else { left = b; right = a; }
                break;
            }
            default:{
                left = a;
                right = b;
            }
        }
        this.player = player;
    }

    /**
     * The board is scanned:
     * left to right (W to E) for alignments on the HORIZONTAL axis
     * top to bottom (N to S) for alignments on the VERTICAL
     * top left to bottom right (NW to SE)
     * top right to bottom left (NE to SW)
     * 
     * @param axis The axis on which to scan the board for winning alignments
     * @return The Direction that guides the scanning of the board for winning
     *         alignments
     */
    public static Direction getSearchDirection(Axis axis) {
        switch (axis) {
            case HORIZONTAL: { return Direction.E; }
            case VERTICAL: { return Direction.S; }
            case NW_SE: { return Direction.SE; }
            case NE_SW: { return Direction.SW; }
            default: { return null; }
        }
    }

    public static Direction getOppositeDirection(Direction direction) {
        switch (direction) {
            case N: { return Direction.S; }
            case NE: { return Direction.SW; }
            case E: { return Direction.W; }
            case SE: { return Direction.NW; }
            case S: { return Direction.N; }
            case SW: { return Direction.NE; }
            case W: { return Direction.E; }
            case NW: { return Direction.SE; }
            default: { return direction; }
        }
    }

}