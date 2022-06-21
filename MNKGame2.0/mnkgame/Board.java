package mnkgame;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Handy extension of MNKBoard that also contains the present threats on the
 * board
 */
public class Board extends MNKBoard {
    public HashSet<Threat> threats; //rename to threats and delete p2Threats
    public String boardVisualisation;

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* PUBLIC INTERFACE FOR OTHER CLASSES (BELOW) */

    public Board(int m, int n, int k) {
        super(m, n, k);
        threats = new HashSet<>();
    }

    /**
     * @param state The player's whose threats we are interested in
     * @return 1 if the game is over and the selected player has won, 0 otherwise
     */
    public int getVictories(MNKCellState state) {
        switch (state) {
            case P1: {
                return (gameState == MNKGameState.WINP1) ? 1 : 0;
            }
            case P2: {
                return (gameState == MNKGameState.WINP2) ? 1 : 0;
            }
            default: {
                return 0;
            }
        }
    }

    /**
     * the list of free cells to later perform the check to make sure the new threat is not redundant given the already existing threats
     * @param t
     * @return
     */
    private LinkedList<MNKCell> getRelevantFreeCells(Threat t){
        LinkedList<MNKCell> relevantFreeCells = new LinkedList<>();
        switch(t.cells.size() - K){
            case 0:{ //k-2 open threat
                //add the two extremities
                relevantFreeCells.add(t.cells.getFirst());
                relevantFreeCells.add(t.cells.getLast());
                break;
            }
            case 1:{ //k-1 open threat or k-1 half open with 0 jumps
                if(isOpenThreat(t)){ //k-1 open threat
                    //add the two extremities
                    relevantFreeCells.add(t.cells.getFirst());
                    relevantFreeCells.add(t.cells.getLast());
                }else{ // k-1 half open with 0 jumps
                    //add the free extremity
                    MNKCell left = t.cells.getFirst();
                    if(left.state == MNKCellState.FREE && contains(left)) relevantFreeCells.add(left);
                    else relevantFreeCells.add(t.cells.getLast());
                }
                break;
            }
            case 2:{ //k-1 half open with 1 jump
                //add the jump
                for(MNKCell c : t.cells){
                    if(c == t.cells.getLast() || c == t.cells.getFirst()) continue;
                    else if(c.state == MNKCellState.FREE) relevantFreeCells.add(c);
                }
                break;
            }
            default : break;
        }
        return relevantFreeCells;
    }

    public boolean isRedundant(Threat t){
        LinkedList<MNKCell> relevantFreeCellsT = getRelevantFreeCells(t);
        boolean tIsOpen = isOpenThreat(t);
        int tSize = t.threatSize();
        for(Threat et : threats){
            if(et.state() != t.state()) continue; // can't be redundant if it doesn't belong to the same player
                
            //basically only a k-1 open threat and a k-1 half open threat sharing a free cell are consiered redundant
            if(tIsOpen && tSize == K-1 && !isHalfOpenThreat(et)) continue;
            //2 open threat that share an extremity but that are not on the same axis are considered not redundant
            if(tIsOpen && tSize == K-2 && isOpenThreat(et) && t.axis != et.axis) continue;
            //for half open threats, if they share their free cell (extremity case 0 jumps and jump in case jumps == 1)
            //they're redundant 

            LinkedList<MNKCell> relevantFreeCellsEt = getRelevantFreeCells(et);
            for(MNKCell c : relevantFreeCellsT){
                if(relevantFreeCellsEt.contains(c)) return true;
            }
        }
        return false;
    }

    
    /**
     * assumes t.cells is initialized correctly relatively to the size K
     * receives a threat containing a list of cells and checks whether the threat has 2 free and in bounds extremities and K-1 aligned cells (in bounds) for one of the players
     * @return
     */
    public boolean isOpenThreat(Threat t){
        int p1Count = 0, p2Count = 0;
        LinkedList<MNKCell> cells = t.getCells();
        MNKCell left = cells.getFirst(), right = cells.getLast();
        if(left.state == MNKCellState.FREE && contains(left) && right.state == MNKCellState.FREE && contains(right)){
            cells.removeFirst();
            cells.removeLast();
            for(MNKCell c : cells){
                if(contains(c)){
                    switch(c.state){
                        case P1:{ p1Count++; break; }
                        case P2:{ p2Count++; break; }
                        default: break;
                    }
                }
            }
        }else return false;

        return p1Count == (t.cells.size()-2) || p2Count == (t.cells.size()-2);
    }

    
    /**
     * assumes t.cells is initialized correctly relatively to the size K
     * receives a threat and checks for (exactly 1 free extremity (case 0 jumps) or doesn't check extremities (case 1 jump)) && (k-1 aligned cells of the same player (in bounds))
     * @param t
     * @return
     */
    public boolean isHalfOpenThreat(Threat t){
        int p1Count = 0, p2Count = 0, freeCount = 0;
        LinkedList<MNKCell> cells = t.getCells();
        if(cells.size() == K + 1){//Half open with 0 jumps
            MNKCell left = cells.getFirst(), right = cells.getLast();
            if((left.state == MNKCellState.FREE && contains(left)) ^ (right.state == MNKCellState.FREE && contains(right))){ //exactly one ext. is free and in bounds
                cells.removeFirst();
                cells.removeLast();
                for(MNKCell c : cells){
                    if(contains(c)){
                        switch(c.state){
                            case P1:{ p1Count++; break; }
                            case P2:{ p2Count++; break; }
                            default: break;
                        }
                    }
                }
            }else return false;

            return p1Count == (t.cells.size()-2) || p2Count == (t.cells.size()-2);
        }else{ // half open with 1 jump
            cells.removeFirst();
            cells.removeLast();
            //the jump can't be in a cell adjacent to the extremities
            if(cells.getFirst().state == MNKCellState.FREE || cells.getLast().state == MNKCellState.FREE) return false;
            
            for(MNKCell c : cells){
                if(contains(c)){
                    switch(c.state){
                        case P1:{ p1Count++; break; }
                        case P2:{ p2Count++; break; }
                        case FREE:{ freeCount++; break; }
                        default: break;
                    }
                }
            }
            return (p1Count == (t.cells.size() - 3 ) || p2Count == (t.cells.size() - 3 )) && freeCount == 1;
        }
    }


    /**
     * @param t
     * @return true if the threats are of the correct size and (isHalfOpenThreat or isOpenThreat is true) 
     */
    private boolean isThreat(Threat t){
        
        switch(t.cells.size() - K){
            case 0:{ //case k-2 open threats
                return isOpenThreat(t);
            }
            case 1:{ //case k-1 open threats and half open threats with 0 jumps
                return isOpenThreat(t) || isHalfOpenThreat(t);
            }
            case 2:{ //case k-1 half open threats 
                return isHalfOpenThreat(t);
            }
            default: return false;
        }
    }

    /*
     * returns the list of all possible MNKCell left extremities in threats that would contain the pivot
     */
    private LinkedList<MNKCell> getLeftExtremityByAxis(MNKCell pivot, int size, Axis axis){
        LinkedList<MNKCell> leftBounds = new LinkedList<>();
        MNKCell iter = pivot;
        for(int i=1; i<=size; i++){
            leftBounds.add(getCellAt(iter.i, iter.j));
            iter = getAdjacentCell(pivot, Threat.oDirection(axis), i);
        }
        return leftBounds;
    }

    /**
     * returns the list of all possible threats of the specified size that contain the pivot
     * @param pivot
     * @param size
     * @return
     */
    private LinkedList<Threat> getSegments(MNKCell pivot, int size){
        LinkedList<Threat> apt = new LinkedList<>();

        for(Axis axis : Axis.values()){
            LinkedList<MNKCell> leftBounds = getLeftExtremityByAxis(pivot, size, axis);
            for(MNKCell c : leftBounds){
                LinkedList<MNKCell> cells = new LinkedList<>();
                cells.add(c);
                for(int i=1; i<size; i++){
                    cells.add(getAdjacentCell(c, Threat.sDirection(axis), i));
                }
                apt.add(new Threat(axis, cells));
            }
        }
        return apt;
    }

    /**
     * driver method for getting the list of all possible threats being created around the pivot for k-1 open, k-2 open and k-1 half open threats
     * @param pivot
     * @return
     */
    public LinkedList<Threat> getAllPossibleThreats(MNKCell pivot){
        LinkedList<Threat> apt = new LinkedList<>();
        apt.addAll(getSegments(pivot, K-1+2)); //k-1 open threats and half open threats with 0 jumps
        apt.addAll(getSegments(pivot, K-1+3)); //k-1 half open threats with 1 jump
        apt.addAll(getSegments(pivot, K-2+2)); //k-2 open threats
        return apt;
    }

    /**
     * returns the set of threats on the board which are in no way affected by the pivot changing
     * @param pivot
     * @return
     */
    private HashSet<Threat> deleteThreatsContaining(MNKCell pivot){
        HashSet<Threat> nonAffectedThreats = new HashSet<>();

        for(Threat t : this.threats){
            if(!t.contains(pivot)) nonAffectedThreats.add(t);
        }

        return nonAffectedThreats;
    }

    /**
     * updates the threats data structures by searching for new threats and deleting
     * old ones
     * assumes itself was run on the moves preceeding the last moves
     * 
     * @param lastMoves
     */
    public void updateThreats(MNKCell pivot) {
        threats = deleteThreatsContaining(pivot);
        LinkedList<Threat> apt = getAllPossibleThreats(pivot);

        for(Threat t : apt){
            if(isThreat(t) /*&& !isRedundant(t)*/) {
                threats.add(t);
            }
        }
    }

    public int[] getNumberOfThreats(){
        int[] th = new int[6];

        for(Threat t : threats){
            int index = -1;
            if(isOpenThreat(t)){
                if(t.threatSize() == K-1) index = 0;
                else if(t.threatSize() == K-2) index = 2;
            }else if(isHalfOpenThreat(t)) index = 1;

            if(index >= 0 && index <= 2){
                if(t.state() == MNKCellState.P1);
                else if(t.state() == MNKCellState.P2) index+=3;
                
                th[index]++;
            }
        }
        return th;
    }
    /* PUBLIC INTERFACE FOR OTHER CLASSES (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* CELL PROPERTIES (BELOW) */

    /**
     * @param cell
     * @return true if the cell is in the bounds of the board
     * @author Davide Iacomino
     */
    public boolean contains(MNKCell cell) {
        return (cell.i >= 0 && cell.i < M && cell.j >= 0 && cell.j < N);
    }


    /**
     * @param i
     * @param j
     * @return the cell in (i,j)
     */
    public MNKCell getCellAt(int i, int j) {
        MNKCell cell = new MNKCell(i, j);

        if (contains(cell))
            return new MNKCell(i, j, cellState(i, j));
        else
            return new MNKCell(i, j, MNKCellState.FREE);
    }

    /**
     * It can return out of bounds cells and cells that belong to the opponent
     * 
     * @param pivot     The cell containing the starting position
     * @param direction The direction to scan for the adjacent cell
     * @param distance 
     * @return The cell that is adjacent to pivot in the given direction
     */
    public MNKCell getAdjacentCell(MNKCell pivot, Direction direction, int distance) {
        if (pivot == null)
            return null;

        int i = pivot.i, j = pivot.j;
        switch (direction) {
            case N: {
                i-=distance;
                break;
            }
            case NE: {
                i-=distance;
                j+=distance;
                break;
            }
            case E: {
                j+=distance;
                break;
            }
            case SE: {
                i+=distance;
                j+=distance;
                break;
            }
            case S: {
                i+=distance;
                break;
            }
            case SW: {
                i+=distance;
                j-=distance;
                break;
            }
            case W: {
                j-=distance;
                break;
            }
            case NW: {
                i-=distance;
                j-=distance;
                break;
            }
            default:
                break;
        }
        return getCellAt(i, j);
    }

    /* CELL PROPERTIES (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    public void updateBoardVisualisation(){
        StringBuilder sb = new StringBuilder("\n");
        int i=0;
        for(MNKCellState[] row : B){
            sb.append(i + " \t");
            for(MNKCellState c : row ){
                if(c == MNKCellState.FREE) sb.append(c.toString().substring(0, 2) + "\t");
                else sb.append(c.toString().substring(0, 2) + "\t");
            }
            sb.append("\n");
            i++;
        }
        boardVisualisation = sb.toString();
    }
}
