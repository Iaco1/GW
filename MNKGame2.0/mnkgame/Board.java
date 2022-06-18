package mnkgame;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Handy extension of MNKBoard that also contains the present threats on the
 * board
 */
public class Board extends MNKBoard {
    public HashSet<Threat> p1Threats;
    public HashSet<Threat> p2Threats;

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* PUBLIC INTERFACE FOR OTHER CLASSES (BELOW) */

    public Board(int m, int n, int k) {
        super(m, n, k);
        p1Threats = new HashSet<>();
        p2Threats = new HashSet<>();
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
     * updates the threats data structures by searching for new threats and deleting
     * old ones
     * assumes itself was run on the moves preceeding the last moves
     * 
     * @param lastMoves
     */
    public void updateThreats(int lastMoves) {
        MNKCell[] MC = getMarkedCells();
        for (int i = lastMoves; i > 0 || MC.length - i < 0; i--) {
            MNKCell pivot = MC[MC.length - i];
            addCellToOpponentThreats(pivot);

            findOpenThreatsOnAllAxes(pivot, K - 1, false);
            findOpenThreatsOnAllAxes(pivot, K - 2, false);
            findHalfOpenThreatsOnAllAxes(pivot, false);
        }
    }

    /**
     * undoes the effect of updateThreats() on the threats data structures for the
     * last specified moves
     * 
     * @param lastMoves
     */
    public void undoLastUpdate(int lastMoves) {
        MNKCell[] MC = getMarkedCells();
        for (int i = lastMoves-1; i >= 0; i--) {
            MNKCell pivot = MC[i];
            /**
             * update threats from the player who played the last move
             * take all threats that contain the last move
             * modify them so as to not contain the last marked cell
             * if they're still a threat keep it else discard it
             */
            updateThreatsContaining(pivot);

            //unmarkCell(); // to be commented
            /**
             * search for
             * k-1 open
             * k-1 half-open
             * k-2 open
             * threats for the opponent of the player that played the last move
             * by pivoting around the newly empty cell
             */
            findOpenThreatsOnAllAxes(pivot, K - 1, true);
            findOpenThreatsOnAllAxes(pivot, K - 2, true);
            findHalfOpenThreatsOnAllAxes(pivot, true);
        }
    }

    /* PUBLIC INTERFACE FOR OTHER CLASSES (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* OPEN THREAT SEARCH (BELOW) */

    /**
     * checks the definition of threat
     * 
     * @param t    The threat to be analysed
     * @param size the desired size for the threat
     * @return
     */
    private boolean isStandaloneValidOpenThreat(Threat t) {
        return t.left.state == MNKCellState.FREE && t.right.state == MNKCellState.FREE
                && contains(t.left) && contains(t.right)
                && (t.size == K-1 || t.size == K-2) 
                && t.jumps == 0;
    }

    private Threat findOpenThreatExtremity(Threat t, MNKCell pivot, int size, boolean left) {
        if (left) {
            // case left
            t.left = pivot;
            // find a possible left extremity and count the no. of marked cells to the left
            // of the pivot

            while (t.left.state == t.player && t.size < size && contains(t.left)) {
                t.size++;
                t.left = getAdjacentCell(t.left, t.oDirection(), 1);
            }
        } else {
            // case right
            t.right = pivot;

            // count the no. of marked cells to the right of the pivot
            while (contains(t.right) && t.right.state == pivot.state && t.size < size) {
                t.size++;
                t.right = getAdjacentCell(t.right, t.sDirection(), 1);
            }
        }
        return t;
    }

    /**
     * Searches for the extremities of an open threat
     * if t.size == 0 it means the pivot has been freed and that it can only be an
     * extremity
     * if t.size == 1 the pivot has just been marked and could be in any position
     * 
     * @param t     The empty threat that will possibly be filled with data about
     *              the existing threat
     * @param pivot The cell from which to start searching
     * @param size  The size of the threat to look for
     * @return
     */
    private Threat findOpenThreatExtremities(Threat t, MNKCell pivot, int size) {
        if (pivot.state != MNKCellState.FREE) {
            // case where pivot is a newly marked cell
            t = findOpenThreatExtremity(t, pivot, size, true);
            t.size--;
            // right extremity
            t = findOpenThreatExtremity(t, pivot, size, false);

        } else if (pivot.state == MNKCellState.FREE) {
            // case where pivot is FREE
            if (isJumpOnAxis(new MNKCell(pivot.i, pivot.j), t, true))
                return t; // this case is quite complicate
            else {
                // case where pivot is FREE and not a jump
                MNKCell right = getAdjacentCell(pivot, t.sDirection(), 1);
                MNKCell left = getAdjacentCell(pivot, t.oDirection(), 1);

                if (right.state == Player.getOpponent(t.player)) {
                    // case where pivot is the left extremity
                    t.left = pivot;
                    t = findOpenThreatExtremity(t, pivot, size, false);

                } else if (left.state == Player.getOpponent(t.player)) {
                    // case where pivot is the right extremity
                    t.right = pivot;
                    t = findOpenThreatExtremity(t, pivot, size, true);
                }
            }
        }
        return t;
    }

    /**
     * searches for new open threats of the requested size, on the requested axis by
     * pivoting the search from the pivot cell
     * 
     * @param pivot
     * @param size
     * @param axis
     */
    private void findOpenThreat(MNKCell pivot, int size, Axis axis, boolean freed) {
        Threat t = new Threat();
        t.axis = axis;
        t.tt = ThreatType.OPEN;
        t.size = 0;

        if (freed) {
            t.player = Player.getOpponent(pivot.state);
            pivot = new MNKCell(pivot.i, pivot.j, MNKCellState.FREE);
        } else {
            t.player = pivot.state;
        }

        t = findOpenThreatExtremities(t, pivot, size);

        // if the threat is of the desired size and is open add it to the correct
        // container object
        if (isStandaloneValidOpenThreat(t))
            addThreat(t);
    }

    /**
     * searches for open threats around a pivot cell on all four axes
     * 
     * @param pivot
     * @param size
     */
    private void findOpenThreatsOnAllAxes(MNKCell pivot, int size, boolean freed) {
        for (Axis axis : Axis.values()) {
            findOpenThreat(pivot, size, axis, freed);
        }
    }

    /* OPEN THREAT SEARCH (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* HALF-OPEN THREAT SEARCH (BELOW) */

    /**
     * Basically the definition of an half-open k-1 threat through which t must be
     * passed to know if it actually is a valid threat
     * 
     * @param t     The threat
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

    private Threat findHalfOpenThreatExtremity(Threat t, MNKCell pivot, int size, boolean left) {
        if (left) {
            if(pivot.state == MNKCellState.FREE) t.left = getAdjacentCell(pivot, t.oDirection(), 1);
            else t.left = pivot;
            // look for an extremity on the left
            while ((t.left.state == t.player || (isJumpOnAxis(t.left, t, true) && t.jumps < 1)) && t.size < size) {
                if (t.left.state == t.player) {
                    t.size++;
                }
                if (isJumpOnAxis(t.left, t, true)) {
                    t.jumps++;
                }

                t.left = getAdjacentCell(t.left, t.oDirection(), 1);
            }
        } else {
            if(pivot.state == MNKCellState.FREE) t.right = pivot;
            else t.right = getAdjacentCell(pivot, t.sDirection(), 1);
            // look for an extremity on the right
            while ((t.right.state == t.player || (isJumpOnAxis(t.right, t, true) && t.jumps < 1)) && t.size < size) {
                if (t.right.state == t.player) {
                    t.size++;
                }
                if (isJumpOnAxis(t.right, t, true)) {
                    t.jumps++;
                }

                t.right = getAdjacentCell(t.right, t.sDirection(), 1);
            }
        }
        return t;
    }

    /**
     * receives an empty threat and is required to return an open threat of the
     * desired size if possible
     * 
     * @param t     The object in which to store the threat
     * @param pivot The cell from which to start the search
     * @param size  the size of the threat
     * @return the largest threat found
     */
    private Threat findHalfOpenThreatsExtremities(Threat t, MNKCell pivot, int size) {
        // left extremitiy
        findHalfOpenThreatExtremity(t, pivot, size, true);
        // right extremity
        findHalfOpenThreatExtremity(t, pivot, size, false);
        return t;
    }

    /**
     * Adds the k-1 half open threats to evaluatedThreats for the selected player
     * assumes pivot is of MNKCellState P1 or P2
     * 
     * when removing an opponent's cell a new threat can only be generated if the
     * cell now contains a jump
     * if the extremities are modified the half open threat will stay the same
     */
    private void findHalfOpenThreats(MNKCell pivot, Axis axis, boolean freed) {
        Threat t = new Threat();
        t.axis = axis;
        t.tt = ThreatType.HALF_OPEN;
        t.size = 0;

        if (freed) {
            // case where the pivot is free
            t.player = Player.getOpponent(pivot.state);
            pivot = new MNKCell(pivot.i, pivot.j, MNKCellState.FREE);

            if (isJumpOnAxis(pivot, t, true)) {
                // case where the pivot is a jump
                t.jumps = 1;
            } else {
                // case where the pivot is an extremity
                t.jumps = 0;
            }
        } else {
            // case where the pivot is inside the threat
            t.jumps = 0;
            t.player = pivot.state;
        }

        t = findHalfOpenThreatsExtremities(t, pivot, K - 1);

        if (isStandaloneValidHalfOpenThreat(t))
            addThreat(t);
    }

    /**
     * driver method for updateHalfOpenThreatsByAxis()
     * 
     * @param cell
     */
    private void findHalfOpenThreatsOnAllAxes(MNKCell cell, boolean freed) {
        for (Axis axis : Axis.values()) {
            findHalfOpenThreats(cell, axis, freed);
        }
    }

    /* HALF-OPEN THREAT SEARCH (ABOVE) */
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
     * does not check whether the cell is in the bounds of the board, but no P1 or
     * P2 cell should be out of bounds so that should be no problem
     * 
     * @param cell
     * @param axis
     * @return true if the cells left and right of the pivotal cell are of the same
     *         MNKCellState and not free and the cell is free
     */
    private boolean isJumpOnAxis(MNKCell cell, Threat t, boolean sameCell) {
        MNKCell left = getAdjacentCell(cell, t.oDirection(), 1);
        MNKCell right = getAdjacentCell(cell, t.sDirection(), 1);

        if (sameCell) {
            return left.state == right.state && left.state != MNKCellState.FREE
                    && cell.state == MNKCellState.FREE && contains(cell);
        } else {
            return left.state != MNKCellState.FREE && right.state != MNKCellState.FREE
                    && left.state != right.state && cell.state == MNKCellState.FREE && contains(cell);
        }
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

    // --------------------------------------------------------------------------------------------------------------------------------------------------
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
        if ((t1.left.equals(getAdjacentCell(t2.left, t1.sDirection(), 1))
                || t1.left.equals(getAdjacentCell(t2.left, t1.oDirection(), 1))
                || t1.right.equals(getAdjacentCell(t2.right, t1.sDirection(), 1))
                || t1.right.equals(getAdjacentCell(t2.right, t1.oDirection(), 1))
            )
                && (t1.axis == t2.axis )) {
            return true;
        }

        return false;
    }

    /**
     * @todo did not change the extremity when cell was marked
     *       assumes the cell to be belong to the opponent of t.player
     * @param t
     * @param cell
     * @return
     */
    private Threat addCellToHalfOpenThreat(Threat t, MNKCell cell) {
        if (t.jumps == 0) {
            // if the threat contains the newly marked cell and we have a half open k-1
            // threat with one free extremity and zero jumps the marked cell must be the
            // other extremity thus closing the threat
            t.size = 0;
        } else if (t.jumps == 1) {
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
    private Threat addCellToOpenThreat(Threat t, MNKCell cell) {
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
     * 
     * @param t
     * @param cell
     * @return
     */
    private Threat addCellToThreat(Threat t, MNKCell cell) {
        switch (t.tt) {
            case OPEN: {
                t = addCellToOpenThreat(t, cell);
                break;
            }
            case HALF_OPEN: {
                t = addCellToHalfOpenThreat(t, cell);
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
        HashSet<Threat> rmSet = new HashSet<>();
        HashSet<Threat> addSet = new HashSet<>();

        for (Iterator<Threat> ti = getPlayerThreats(Player.getOpponent(cell.state)).iterator(); ti.hasNext();) {
            Threat t = ti.next();
            if (t.contains(cell)) {
                rmSet.add(t);
                t = addCellToThreat(t, cell);
                if (isStandaloneValidHalfOpenThreat(t) || isStandaloneValidOpenThreat(t))
                    addSet.add(t);
            }
        }
        getPlayerThreats(Player.getOpponent(cell.state)).removeAll(rmSet);
        getPlayerThreats(Player.getOpponent(cell.state)).addAll(addSet);
    }

    private Threat substituteExtremity(Threat t, MNKCell cell) {
        MNKCell c = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
        MNKCell leftAdj = getAdjacentCell(c, t.oDirection(), 1);
        MNKCell rightAdj = getAdjacentCell(c, t.sDirection(), 1);

        if(t.jumps == 0){
            if (leftAdj.equals(t.left)) {
                return new Threat(c, t.right, t.axis, t.player, t.size - 1, ThreatType.OPEN, 0);
            } else if (rightAdj.equals(t.right)) {
                return new Threat(t.left, c, t.axis, t.player, t.size - 1, ThreatType.OPEN, 0);
            }
        }
        else{
            MNKCell ext;
            if(leftAdj.equals(t.left)){
                ext = getAdjacentCell(leftAdj, t.sDirection(), 2);
                return new Threat(ext, t.right, t.axis, t.player, t.size-1, ThreatType.OPEN, 0);
            } else if(rightAdj.equals(t.right)){
                ext = getAdjacentCell(rightAdj, t.oDirection(), 2);
                return new Threat(t.left, ext, t.axis, t.player, t.size-1, ThreatType.OPEN, 0);
            }
        }
        return t;

    }

    /**
     * true if cell is a a jump and an extremity
     * also
     * 
     * @param t
     * @param c
     * @return
     */
    private boolean cellIsNewExtremity(Threat t, MNKCell c) {
        MNKCell leftAdj = getAdjacentCell(c, t.oDirection(), 1);
        MNKCell rightAdj = getAdjacentCell(c, t.sDirection(), 1);

        return leftAdj.equals(t.left) || rightAdj.equals(t.right);
    }

    private boolean isNewOpenThreat(Threat t, MNKCell c){
        MNKCell leftAdj = getAdjacentCell(c, t.oDirection(), 1);
        MNKCell rightAdj = getAdjacentCell(c, t.sDirection(), 1);

        return 
        ( (leftAdj.equals(t.left) && isJumpOnAxis(rightAdj, t, true))
        || (isJumpOnAxis(leftAdj, t, true) && rightAdj.equals(t.right))
        )
        && 
        (t.left.state == MNKCellState.FREE && contains(t.left) && t.right.state == MNKCellState.FREE && contains(t.right));
    }

    /**
     * assumes cell is of the same state as t.player
     * removes cell from the threat
     * generally if the threat contains a marked cell sandwiched between a jump and
     * an extremity you will get a k-2 open threat
     * otherwise you will get a 0-sized threat
     * 
     * @param t
     * @param cell
     * @return
     */
    public Threat removeCellFromHalfOpenThreat(Threat t, MNKCell cell) {
        if (cellIsNewExtremity(t, cell) && t.jumps == 0) // case k-1 open without jumps
            return substituteExtremity(t, cell);
        else if(isNewOpenThreat(t, cell) ){ //case k-1 open with 1 jump
            return substituteExtremity(t, cell);
        }else if(Position.samePosition(t.left, cell)){
            t.left = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
            return t;
        }else if(Position.samePosition(t.right, cell)){
            t.right = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
            return t;
        }
            return new Threat();
    }

    /**
     * assumes cell is of the same MNKCellState as t.player
     * removes cell from the open threat
     * if the threat is of size k-1 and the cell is adjacent to an extremity you
     * will get a k-2 threat
     * else the threat will be assigned size 0
     * 
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
                MNKCell leftEdge = getAdjacentCell(t.left, t.sDirection(), 1);
                MNKCell rightEdge = getAdjacentCell(t.right, t.oDirection(), 1);
                if (Position.samePosition(leftEdge, cell)) {
                    t.left = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
                    t.size--;
                } else if (Position.samePosition(rightEdge, cell)) {
                    t.right = new MNKCell(cell.i, cell.j, MNKCellState.FREE);
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
     * removes cell from all threats that contain it and if the threat still follows
     * the definition it will be kept and modified
     * else it will be discarded
     * 
     * @param cell
     */
    public void updateThreatsContaining(MNKCell cell) {
        HashSet<Threat> rmSet = new HashSet<>();
        HashSet<Threat> addSet = new HashSet<>();
        for (Iterator<Threat> ti = getPlayerThreats(cell.state).iterator(); ti.hasNext();) {
            Threat t = ti.next();
            if (t.contains(cell)) {
                rmSet.add(t);
                t = removeCellFromThreat(t, cell);
                if (isStandaloneValidHalfOpenThreat(t) || isStandaloneValidOpenThreat(t))
                    addSet.add(t);
            }
        }
        getPlayerThreats(cell.state).removeAll(rmSet);
        getPlayerThreats(cell.state).addAll(addSet);

        rmSet = new HashSet<>();
        addSet = new HashSet<>();
        for (Iterator<Threat> ti = getPlayerThreats(Player.getOpponent(cell.state)).iterator(); ti.hasNext();) {
            Threat t = ti.next();
            if (t.contains(cell)) {
                rmSet.add(t);
                t = removeCellFromThreat(t, cell);
                if (isStandaloneValidHalfOpenThreat(t) || isStandaloneValidOpenThreat(t))
                    addSet.add(t);
            }
        }
        getPlayerThreats(Player.getOpponent(cell.state)).removeAll(rmSet);
        getPlayerThreats(Player.getOpponent(cell.state)).addAll(addSet);
    }

    /* THREAT PROPERTIES AND MODIFYING (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    /* P1 P2 THREATS OPERATIONS (BELOW) */

    public int[] getThreatSize(HashSet<Threat> threats) {
        int[] c = new int[3];
        for (Threat t : threats) {
            switch (t.tt) {
                case OPEN: {
                    switch (K - t.size) {
                        case 1: {
                            c[0]++;
                            break;
                        }
                        case 2: {
                            c[2]++;
                            break;
                        }
                        default:
                            break;
                    }
                }
                case HALF_OPEN: {
                    if (K - t.size == 1)
                        c[1]++;
                    break;
                }
                default:
                    break;
            }
        }
        return c;
    }

    /**
     * Some subset of threats can be closed upon marking the same cell,
     * so this makes sure that only one threat is counted
     */
    private void redundancyCheck(Threat t) {
        // it 's redundant if it's either contained in another threat
        // or if one or both of its extremities differ by one unit in either two of the
        // threat's axis search direction
        boolean redundant = false;
        HashSet<Threat> rmSet = new HashSet<>();

        for (Iterator<Threat> ti = getPlayerThreats(t.player).iterator(); ti.hasNext();) {
            Threat vt = ti.next();
            if (containerContainsContained(t, vt)) {
                rmSet.add(vt);
            }
            if(differByOne(vt, t)) redundant = true;
            if (containerContainsContained(vt, t))
                if(!Position.samePosition(vt.left, t.left) && !Position.samePosition(vt.right, t.right)) redundant = true; // maybe I can just return here
        }

        if (!redundant)
            getPlayerThreats(t.player).add(t);

        getPlayerThreats(t.player).removeAll(rmSet);
    }

    private void addThreat(Threat t) {
        // check if threat is redundant
        redundancyCheck(t);
    }

    public HashSet<Threat> getPlayerThreats(MNKCellState state) {
        if (state == MNKCellState.P1)
            return p1Threats;
        else if (state == MNKCellState.P2)
            return p2Threats;
        else
            return null;
    }

    /* P1 P2 THREATS OPERATIONS (ABOVE) */
    // --------------------------------------------------------------------------------------------------------------------------------------------------
}
