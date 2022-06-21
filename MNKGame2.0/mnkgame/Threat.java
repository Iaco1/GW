package mnkgame;

import java.util.LinkedList;

/**
 * Describes a sequence of cells on a certain axis that can lead to victory in k-i moves
 * with k being the number of cells to align to win the game
 * and i being how many cells the current threat lacks to get to size k
 * In this implementation, only k-1 and k-2 threats are considered
 * Left and right are the extremities of the threat and are always free, out of bounds or occupied by the enemy
 * The axis tells you the order in which to scan the threat
 */
public class Threat{
    public Axis axis;
    public LinkedList<MNKCell> cells;

    public Threat(){
        axis = Axis.HORIZONTAL;
    }

    /**
     * no check on whether positions are in the board
     * no check on whether positions are actually on the same axis
     * no check on threat type
     * no check on size
     * no check on jumps
     * @param a One extremity of the Threat
     * @param b the other extremity of the Threat
     * @param axis The axis on which the threat was found
     */
    public Threat(Axis axis, LinkedList<MNKCell> cells) {
        this.cells = cells;
        this.axis = axis;
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
    public static Direction sDirection(Axis axis) {
        switch (axis) {
            case HORIZONTAL: { return Direction.E; }
            case VERTICAL: { return Direction.S; }
            case DIAGONAL: { return Direction.SE; }
            case ANTIDIAGONAL: { return Direction.SW; }
            default: { return null; }
        }
    }

    /**
     * 
     * @return The opposite direction of sDirection
     */
    public static Direction oDirection(Axis axis) {
        switch (sDirection(axis)) {
            case N: { return Direction.S; }
            case NE: { return Direction.SW; }
            case E: { return Direction.W; }
            case SE: { return Direction.NW; }
            case S: { return Direction.N; }
            case SW: { return Direction.NE; }
            case W: { return Direction.E; }
            case NW: { return Direction.SE; }
            default: { return sDirection(axis); }
        }
    }

    public boolean contains(MNKCell cell){
        MNKCell left = cells.getFirst(), right = cells.getLast();
        int x1 = left.j, x2 = right.j, y1 = left.i, y2 = right.i, x = cell.j, y = cell.i;
        int lbi = Math.min(left.i, right.i), hbi = Math.max(left.i, right.i);
        int lbj = Math.min(left.j, right.j), hbj = Math.max(left.j, right.j);
        return (x - x1)*(y2 - y1) == (y - y1)*(x2 - x1) 
        && (y >= lbi && y <= hbi) && (x >= lbj && x <= hbj);
    }

    public LinkedList<MNKCell> getCells(){ return new LinkedList<>(cells); }

    public MNKCellState state(){ return cells.get(1).state; }

    public int threatSize(){
        int p1 = 0, p2 = 0;
        LinkedList<MNKCell> cells = getCells();
        cells.removeFirst(); cells.removeLast();
        for(MNKCell c : cells){
            if(c.state == MNKCellState.P1) p1++;
            else if(c.state == MNKCellState.P2) p2++;
        }
        return Integer.max(p1, p2);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("[" + axis + ",\n");
        for(MNKCell c : cells) sb.append(c.toString() + ", ");
        sb.append(" ]");
        return sb.toString();
    }
}