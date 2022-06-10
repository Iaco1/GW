package mnkgame;

import java.util.HashSet;

/**
 * Handy extension of MNKBoard that also contains the present threats on the
 * board
 */
public class Board extends MNKBoard {
    public ThreatsByAxis p1Threats;
    public ThreatsByAxis p2Threats;

    //--------------------------------------------------------------------------------------------------------------------------------------------------
    /* PUBLIC INTERFACE FOR OTHER CLASSES (BELOW) */
    
    public Board(int m, int n, int k) {
        super(m, n, k);
        p1Threats = new ThreatsByAxis();
        p2Threats = new ThreatsByAxis();
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
     * updates the threats data structures by searching for new threats and deleting old ones
     * assumes itself was run on the moves preceeding the last moves 
     * @param lastMoves
     */
    public void updateThreats(int lastMoves) {
        MNKCell[] MC = getMarkedCells();
        for (int i = lastMoves; i > 0 || MC.length - i < 0; i--) {
            MNKCell pivot = MC[MC.length - i];
            addCellToOpponentThreats(pivot);

            updateOpenThreats(pivot, K - 1);
            updateOpenThreats(pivot, K - 2);
            updateHalfOpenThreats(pivot);
        }
    }

    /**
     * undoes the effect of updateThreats() on the threats data structures for the last specified moves
     * @param lastMoves
     */
    public void undoLastUpdate(int lastMoves){
        MNKCell[] MC = getMarkedCells();
        for (int i = lastMoves; i > 0 || MC.length - i < 0; i--) {
            MNKCell pivot = MC[MC.length - i];
            /**
             * update threats from the player who played the last move
             * take all threats that contain the last move
             * modify them so as to not contain the last marked cell
             * if they're still a threat keep it else discard it
             */
            updateThreatsContaining(pivot);
            

            
            /**
             * search for 
             * k-1 open
             * k-1 half-open
             * k-2 open
             * threats for the opponent of the player that played the last move 
             * by pivoting around the newly empty cell 
             * 
             */
        }
    }
    
    /* PUBLIC INTERFACE FOR OTHER CLASSES (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------



    //--------------------------------------------------------------------------------------------------------------------------------------------------
    /* OPEN THREAT SEARCH (BELOW) */
    
    /**
     * checks the definition of threat
     * @param t The threat to be analysed
     * @param size the desired size for the threat
     * @return
     */
    private boolean isStandaloneValidOpenThreat(Threat t, int size) {
        return t.left.state == MNKCellState.FREE && t.right.state == MNKCellState.FREE
                && contains(t.left) && contains(t.right)
                && t.size == size
                && t.jumps == 0;
    }

    /**
     * Searches for the extremities of an open threat
     * @param t The empty threat that will possibly be filled with data about the existing threat 
     * @param pivot The cell from which to start searching
     * @param size The size of the threat to look for
     * @return 
     */
    private Threat findOpenThreatExtremities(Threat t, MNKCell pivot, int size) {
        t.left = getAdjacentCell(pivot, t.oDirection());
        // find a possible left extremity and count the no. of marked cells to the left
        // of the pivot

        while (t.left.state == pivot.state && t.size < size && contains(t.left)) {
            t.size++;
            t.left = getAdjacentCell(t.left, t.oDirection());
        }

        // right extremity
        t.right = getAdjacentCell(pivot, t.sDirection());

        // count the no. of marked cells to the right of the pivot
        while (contains(t.right) && t.right.state == pivot.state && t.size < size) {
            t.size++;
            t.right = getAdjacentCell(t.right, t.sDirection());
        }
        return t;
    }

    /**
     * searches for new open threats of the requested size, on the requested axis by pivoting the search from the pivot cell
     * @param pivot
     * @param size
     * @param axis
     */
    private void updateOpenThreatsByAxis(MNKCell pivot, int size, Axis axis) {
        Threat t = new Threat();
        t.size = 1;
        t.axis = axis;
        t.tt = ThreatType.OPEN;
        t.player = pivot.state;

        t = findOpenThreatExtremities(t, pivot, size);

        // if the threat is of the desired size and is open add it to the correct
        // container object
        if (isStandaloneValidOpenThreat(t, size))
            addThreat(t);
    }

    /**
     * searches for open threats around a pivot cell on all four axes
     * 
     * @param pivot
     * @param size
     */
    private void updateOpenThreats(MNKCell pivot, int size) {
        for (Axis axis : Axis.values()) {
            updateOpenThreatsByAxis(pivot, size, axis);
        }
    }

    /* OPEN THREAT SEARCH (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------



    //--------------------------------------------------------------------------------------------------------------------------------------------------
    /* HALF-OPEN THREAT SEARCH (BELOW) */

    /**
     * Basically the definition of an half-open k-1 threat through which t must be passed to know if it actually is a valid threat
     * @param t The threat
     * @param pivot The cell from which the search started and 
     * @return true if it follows the definition
     */
    private boolean isStandaloneValidHalfOpenThreat(Threat t) {
        boolean zeroExtMarkedByPlayer = t.left.state != t.player && t.right.state != t.player;
        boolean exactlyOneExtIsFreeAndInBounds = (t.left.state == MNKCellState.FREE
                && contains(t.left)) ^ (t.right.state == MNKCellState.FREE && contains(t.right));
        boolean leqOneExtMarkedByPlayer = !(t.left.state == t.player && t.right.state == t.player);

        return t.size == K - 1
                &&
                ((t.jumps == 0 && exactlyOneExtIsFreeAndInBounds && zeroExtMarkedByPlayer)
                        || (t.jumps == 1 && leqOneExtMarkedByPlayer));
    }

    /**
     * receives an empty threat and is required to return an open threat of the desired size if possible 
     * @param t The object in which to store the threat
     * @param pivot The cell from which to start the search
     * @param size the size of the threat
     * @return the largest threat found
     */
    private Threat findHalfOpenThreatsExtremities(Threat t, MNKCell pivot, int size) {
        // left extremitiy
        t.left = getAdjacentCell(pivot, t.oDirection());
        // look for an extremity on the left
        while ((t.left.state == pivot.state || (isJumpOnAxis(t.left, t) && t.jumps < 1)) && t.size < size) {
            if (t.left.state == pivot.state) {
                t.size++;
            }
            if (isJumpOnAxis(t.left, t)) {
                t.jumps++;
            }

            t.left = getAdjacentCell(t.left, t.oDirection());
        }

        // right extremity
        t.right = getAdjacentCell(pivot, t.sDirection());
        // look for an extremity on the right
        while ((t.right.state == pivot.state || (isJumpOnAxis(t.right, t) && t.jumps < 1)) && t.size < size) {
            if (t.right.state == pivot.state) {
                t.size++;
            }
            if (isJumpOnAxis(t.right, t)) {
                t.jumps++;
            }

            t.right = getAdjacentCell(t.right, t.oDirection());
        }
        return t;
    }

    /**
     * Adds the k-1 half open threats to evaluatedThreats for the selected player
     * assumes pivot is of MNKCellState P1 or P2
     * 
     * @todo Could also fix the right extremity: now it's the player's last marked
     *       cell and it's supposed to be the cell after that
     * @todo does not detect the generation of 2 half open threats
     * @return
     */
    private void updateHalfOpenThreatsByAxis(MNKCell pivot, Axis axis) {
        Threat t = new Threat();
        t.axis = axis;
        t.player = pivot.state;
        t.size = 1;
        t.tt = ThreatType.HALF_OPEN;
        t.jumps = 0;

        t = findHalfOpenThreatsExtremities(t, pivot, K - 1);

        if (isStandaloneValidHalfOpenThreat(t))
            addThreat(t);
    }

    /**
     * driver method for updateHalfOpenThreatsByAxis()
     * @param cell
     */
    private void updateHalfOpenThreats(MNKCell cell) {
        for (Axis axis : Axis.values()) {
            updateHalfOpenThreatsByAxis(cell, axis);
        }
    }

    /* HALF-OPEN THREAT SEARCH (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------
    


    //--------------------------------------------------------------------------------------------------------------------------------------------------
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
     * does not check whether the cell is in the bounds of the board, but no P1 or
     * P2 cell should be out of bounds so that should be no problem
     * 
     * @param cell
     * @param axis
     * @return true if the cells left and right of the pivotal cell are of the same
     *         MNKCellState and not free and the cell is free
     */
    private boolean isJumpOnAxis(MNKCell cell, Threat t) {
        MNKCell left = getAdjacentCell(cell, t.sDirection());
        MNKCell right = getAdjacentCell(cell, t.oDirection());

        if (left.state == right.state && left.state != MNKCellState.FREE && cell.state == MNKCellState.FREE
                && contains(cell))
            return true;
        else
            return false;
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
     * @return The cell that is adjacent to pivot in the given direction
     */
    public MNKCell getAdjacentCell(MNKCell pivot, Direction direction) {
        if (pivot == null)
            return null;

        int i = pivot.i, j = pivot.j;
        switch (direction) {
            case N: {
                i--;
                break;
            }
            case NE: {
                i--;
                j++;
                break;
            }
            case E: {
                j++;
                break;
            }
            case SE: {
                i++;
                j++;
                break;
            }
            case S: {
                i++;
                break;
            }
            case SW: {
                i++;
                j--;
                break;
            }
            case W: {
                j--;
                break;
            }
            case NW: {
                i--;
                j--;
                break;
            }
            default:
                break;
        }
        return getCellAt(i, j);
    }

    /* CELL PROPERTIES (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------



    //--------------------------------------------------------------------------------------------------------------------------------------------------
    /* THREAT PROPERTIES AND MODIFYING (BELOW) */

    private boolean containerContainsContained(Threat container, Threat contained) {
        return container.contains(contained.left) && container.contains(contained.right);
    }

    /**
     * no checks whatsoever on the threats
     * assumes the threats are updated to the last move
     * 
     * @param t1
     * @param t2
     * @return true if the threats have at least one adjacent extremity
     */
    private boolean differByOne(Threat t1, Threat t2) {
        // if t1's left extremity is adjacent to t2's left extremity
        // or t1's right extremity is adjacent to t2' right extremity
        if (t1.left.equals(getAdjacentCell(t2.left, t1.sDirection()))
                || t1.left.equals(getAdjacentCell(t2.left, t1.oDirection()))
                || t1.right.equals(getAdjacentCell(t2.right, t1.sDirection()))
                || t1.right.equals(getAdjacentCell(t2.right, t1.oDirection()))) {
            return true;
        }

        return false;
    }

    /**
     * @todo did not change the extremity when cell was marked
     * assumes the cell to be belong to the opponent of t.player
     * @param t
     * @param cell
     * @return
     */
    private Threat updateHalfOpenThreatsExtremities(Threat t, MNKCell cell) {
        if (t.jumps == 0) {
            // if the threat contains the newly marked cell and we have a half open k-1
            // threat with one free extremity and zero jumps the marked cell must be the
            // other extremity thus closing the threat
            t.size = 0;
        }else if (t.jumps == 1) {
            if (Position.samePosition(cell, t.left))
                t.left = cell;
            else if (Position.samePosition(cell, t.right))
                t.right = cell;
            else
                t.size = 0; // the cell must have closed the jump
        }
        return t;
    }

    /**
     * assumes cell to belong to the oppoent of t.player
     * 
     * @param t
     * @param cell
     * @return
     */
    private Threat updateOpenThreatExtremities(Threat t, MNKCell cell) {
        if (K - t.size == 1) {
            if (Position.samePosition(t.left, cell)) {
                t.left = cell;
                t.tt = ThreatType.HALF_OPEN;
            } else if (Position.samePosition(t.right, cell)) {
                t.right = cell;
                t.tt = ThreatType.HALF_OPEN;
            }
        } else if (K - t.size == 2 && (Position.samePosition(t.left, cell) || Position.samePosition(t.right, cell))) {
            // the other methods already handle the change from a k-2 open threat to a k-1
            // open threat
            t.size = 0;
        }
        return t;
    }

    /**
     * Modifies the threat according to where cell appears in the threat
     * @param t
     * @param cell
     * @return
     */
    private Threat addCellToThreat(Threat t, MNKCell cell) {
        switch (t.tt) {
            case OPEN: {
                t = updateOpenThreatExtremities(t, cell);
                break;
            }
            case HALF_OPEN: {
                t = updateHalfOpenThreatsExtremities(t, cell);
                break;
            }
            default:
                break;
        }
        return t;
    }

    /**
     * assumes cell is marked by P1 or P2
     * 
     * @param cell
     */
    private void addCellToOpponentThreats(MNKCell cell) {
        for (Axis axis : Axis.values()) {
            HashSet<Threat> axisThreats = new HashSet<>();
            for (Threat t : getPlayerThreats(Player.getOpponent(cell.state)).getThreatsByAxis(axis)) {
                if (!t.contains(cell))
                    axisThreats.add(t);
                else {
                    t = addCellToThreat(t, cell);
                    if (t.size != 0)
                        axisThreats.add(t);
                }
            }
            getPlayerThreats(Player.getOpponent(cell.state)).setThreatsByAxis(axis, axisThreats);
        }
    }

    /**
     * true if cell is sandwiched between a jump and an extremity
     * also 
     * @param t
     * @param c
     * @return
     */
    private boolean sandwichMod(Threat t, MNKCell c){
        MNKCell leftAdj = getAdjacentCell(c, t.sDirection());
        MNKCell rightAdj = getAdjacentCell(c, t.oDirection());
        boolean isJmpOnAxis = isJumpOnAxis(c, t);

        if( leftAdj.equals(t.left) && isJmpOnAxis){
            t = new Threat(rightAdj, t.right, t.axis, t.player, t.size-1, ThreatType.OPEN, 0);
            return true;    
        }else if(rightAdj.equals(t.right) && isJmpOnAxis){
            t = new Threat(t.left, leftAdj, t.axis, t.player, t.size-1, ThreatType.OPEN, 0);
            return true;
        }else return false;

    }

    /**
     * assumes cell is of the same state as t.player
     * removes cell from the threat
     * generally if the threat contains a marked cell sandwiched between a jump and an extremity you will get a k-2 open threat
     * otherwise you will get a 0-sized threat
     * @param t
     * @param cell
     * @return
     */
    public Threat removeCellFromHalfOpenThreat(Threat t, MNKCell cell) {
        if(cell.equals(t.left)) t.left = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
        else if(cell.equals(t.right)) t.right = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
        else if(sandwichMod(t, cell)) return t;
        else return new Threat();
        
        return t;
    }

    /**
     * assumes cell is of the same MNKCellState as t.player
     * removes cell from the open threat
     * if the threat is of size k-1 and the cell is adjacent to an extremity you will get a k-2 threat
     * else the threat will be assigned size 0
     * @param t
     * @param cell
     * @return
     */
    public Threat removeCellFromOpenThreat(Threat t, MNKCell cell) {
        // the cell can never be an extremity because open threats have both extremies
        // free
        switch (K - t.size) {
            case 1: {
                // in this case if the removed cell is next to the extremity (edge) the threat
                // will be of size k-2
                MNKCell leftEdge = getAdjacentCell(t.left, t.sDirection());
                MNKCell rightEdge = getAdjacentCell(t.right, t.oDirection());
                if (leftEdge.equals(cell)) {
                    t.left = new MNKCell(leftEdge.i, leftEdge.j, MNKCellState.FREE);
                    t.size--;
                } else if (rightEdge.equals(cell)) {
                    t.right = new MNKCell(rightEdge.i, rightEdge.j, MNKCellState.FREE);
                    t.size--;
                } else {
                    // cell is not an edge but is in the threat thus the threat is irrequivocally
                    // split up
                    t = new Threat();
                }
                break;
            }
            case 2: {
                // the threat would become at best (edge case) a k-3 threat which we do not
                // consider
                // or at worst not a threat
                t = new Threat();
                break;
            }
            default:
                break;
        }
        return t;

    }

    /**
     * assumes cell is of the same MNKCellState as t.player
     * it removes the cell from the threat and modifies it accordingly
     * you will either get a 0-sized threat or a modified threat
     * 
     * @param t
     * @param cell
     * @return
     */
    public Threat removeCellFromThreat(Threat t, MNKCell cell) {
        switch (t.tt) {
            case OPEN: {
                // update threat by removing a cell in case Open
                t = removeCellFromOpenThreat(t, cell);
                break;
            }
            case HALF_OPEN: {
                // update threat by removing a cell in case Half Open
                t = removeCellFromHalfOpenThreat(t, cell);
                break;
            }
        }
        return t;
    }

    /**
     * removes cell from all threats that contain it and if the threat still follows the definition it will be kept and modified
     * else it will be discarded
     * 
     * @param cell
     */
    public void updateThreatsContaining(MNKCell cell) {
        for (Axis axis : Axis.values()) {
            HashSet<Threat> axisThreats = new HashSet<>();
            for (Threat t : getPlayerThreats(cell.state).getThreatsByAxis(axis)) {
                if (!t.contains(cell))
                    axisThreats.add(t);
                else {
                    t = removeCellFromThreat(t, cell);
                    
                    if (t.size != 0)
                        axisThreats.add(t);
                }
            }
            getPlayerThreats(cell.state).setThreatsByAxis(axis, axisThreats);
        }
    }

    /* THREAT PROPERTIES AND MODIFYING (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------



    //--------------------------------------------------------------------------------------------------------------------------------------------------
    /* THREATS BY AXIS OPERATIONS (BELOW) */

    /**
     * Some subset of threats can be closed upon marking the same cell,
     * so this makes sure that only one threat is counted
    */
    private void redundancyCheck(Threat t) {
        // it 's redundant if it's either contained in another threat
        // or if one or both of its extremities differ by one unit in either two of the
        // threat's axis search direction
        boolean redundant = false;
        HashSet<Threat> updatedThreats = new HashSet<>();

        for (Threat axisThreat : getPlayerThreats(t.player).getThreatsByAxis(t.axis)) {

            if (containerContainsContained(t, axisThreat)) {
                // getPlayerThreats(t.player).remove(axisThreat);
            } else
                updatedThreats.add(axisThreat);

            if (containerContainsContained(axisThreat, t) || differByOne(axisThreat, t))
                redundant = true;
        }

        if (!redundant)
            updatedThreats.add(t);
        getPlayerThreats(t.player).setThreatsByAxis(t.axis, updatedThreats);
    }

    private void addThreat(Threat t) {
        // check if threat is redundant
        redundancyCheck(t);
    }

    public ThreatsByAxis getPlayerThreats(MNKCellState state) {
        if (state == MNKCellState.P1)
            return p1Threats;
        else if (state == MNKCellState.P2)
            return p2Threats;
        else
            return null;
    }

    /* THREATS BY AXIS OPERATIONS (ABOVE) */
    //--------------------------------------------------------------------------------------------------------------------------------------------------
}
