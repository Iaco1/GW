package mnkgame;

/**
 * Handy extension of MNKBoard that also contains the present threats on the board
 */
public class Board extends MNKBoard{
    public EvaluatedThreats playerThreats;
    public EvaluatedThreats opponentThreats;

    public Board(int m, int n, int k){
        super(m,n,k);
    }

    /**
     * @param cell
     * @return true if the cell is in the bounds of the board
     * @author Davide Iacomino
     */
    public boolean contains(MNKCell cell) {
        return (cell.i >= 0 && cell.i < M && cell.j >= 0 && cell.j < N);
    }

    /**
     * @param cell The cell to evaluate
     * @return True if it's not an opponents' cell and is in the bounds of the board
     * @author Davide Iacomino
     */
    public boolean isAlignable(MNKCell pivot, MNKCell cell) {
        return (cell.state != Player.getOpponent(pivot.state) && contains(cell));
    }

    /**
     * @param i
     * @param j
     * @return the cell in (i,j)
     */
    MNKCell getCellAt(int i, int j) {
        MNKCell cell = new MNKCell(i, j);

        if(contains(cell)) return new  MNKCell(i, j, cellState(i, j));
        else return new MNKCell(i,j, MNKCellState.FREE);
    }

    /**
     * It can return out of bounds cells and cells that belong to the opponent
     * 
     * @param pivot     The cell containing the starting position
     * @param direction The direction to scan for the adjacent cell
     * @return The cell that is adjacent to pivot in the given direction
     */
    public MNKCell getAdjacentCell(MNKCell pivot, Direction direction) {
        if (pivot == null) return null;

        int i = pivot.i, j = pivot.j;
        switch (direction) {
            case N: { i--; break; }
            case NE: { i--; j++; break; }
            case E: { j++; break; }
            case SE: { i++; j++; break; }
            case S: { i++; break; }
            case SW: { i++; j--; break; }
            case W: { j--; break; }
            case NW: { i--; j--; break; }
            default: break;
        }
        return getCellAt(i, j);
    }

    public boolean containsRedundantThreat(Threat container, Threat contained){
        if(container.axis == contained.axis){
            switch(contained.axis){
                case HORIZONTAL:{
                    if(container.left.j <= contained.left.j && container.right.j >= contained.right.j) return true;
                    else return false;
                }
                case VERTICAL:{
                    if(container.left.i <= contained.left.i && container.right.i >= contained.right.j) return true;
                    else return false;
                }
                case NE_SW:
                case NW_SE:{
                    MNKCell iter = container.left;
                    MNKCell breakpoint = getAdjacentCell(container.right, Threat.getSearchDirection(container.axis));
                    boolean leftExtremityIntersected = false;
                    while(iter != breakpoint){
                        if(iter == contained.left) leftExtremityIntersected = true;
                        if(iter == contained.right && leftExtremityIntersected) return true;
                        iter = getAdjacentCell(iter, Threat.getSearchDirection(container.axis));
                    }
                    return false;
                }
                default: return false;
            }
        }else return false;
    }
}
