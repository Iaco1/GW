package mnkgame;

/**
 * Handy extension of MNKBoard that also contains the present threats on the board
 */
public class Board extends MNKBoard{
    public EvaluatedThreats p1Threats;
    public EvaluatedThreats p2Threats;

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

    /**
     * no checks whatsoever on the threats
     * @param t1
     * @param t2
     * @return true if the threats have at least one adjacent extremity
     */
    public boolean differByOne(Threat t1, Threat t2){
        Direction searchDirection = Threat.getSearchDirection(t1.axis);
        Direction oppDirection = Threat.getOppositeDirection(searchDirection);

        //if t1's left extremity is adjacent to t2's left extremity
        // or t1's right extremity is adjacent to t2' right extremity
        if(t1.left.equals(getAdjacentCell(t2.left, searchDirection)) || t1.left.equals(getAdjacentCell(t2.left, oppDirection))
        || t1.right.equals(getAdjacentCell(t2.right, searchDirection)) || t1.right.equals(getAdjacentCell(t2.right, oppDirection))){
            return true;
        }

        return false;
    }

    public boolean containerContainsContained(Threat container, Threat contained){
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

    public EvaluatedThreats getEvaluatedThreats(MNKCellState state){
        if(state == MNKCellState.P1) return p1Threats;
        else if(state == MNKCellState.P2) return p2Threats;
        else return null;
    }

    public void setEvaluatedThreats(MNKCellState state, EvaluatedThreats evTh){
        switch(state){
            case P1: {
                p1Threats = evTh;
                break;
            }
            case P2:{
                p2Threats = evTh;
                break;
            }
            default: break;
        }
    }
    public boolean isRedundant(Threat t, ThreatType tt, int size){
        //check whether the threat is redundant compared to the ones you already have
        
        // it 's redundant if it's either contained in another threat 
        // or if one or both of its extremities differ by one unit in either two of the threat's axis search direction
        for(Threat axisThreat : getEvaluatedThreats(t.player).getThreatsByType(tt, K, size).getHashSet(t.axis)){
            if (containerContainsContained(axisThreat, t) || differByOne(axisThreat, t)) return true;
        }
        return false;

    }
}
