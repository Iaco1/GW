package mnkgame;

import java.util.HashSet;

/**
 * Handy extension of MNKBoard that also contains the present threats on the
 * board
 */
public class Board extends MNKBoard {
    public ThreatsByAxis p1Threats;
    public ThreatsByAxis p2Threats;

    public Board(int m, int n, int k) {
        super(m, n, k);
        p1Threats = new ThreatsByAxis();
        p2Threats = new ThreatsByAxis();
    }

    /**
     * @param b     The board to check for k-sized threats
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
     * searches for open threats around a pivot cell on all four axes
     * 
     * @param pivot
     * @param size
     */
    public void updateOpenThreats(MNKCell pivot, int size) {
        for (Axis axis : Axis.values()) {
            updateOpenThreatsByAxis(pivot, size, axis);
        }
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
    boolean isJumpOnAxis(MNKCell cell, Axis axis) {
        MNKCell left = getAdjacentCell(cell, Threat.getSearchDirection(axis));
        MNKCell right = getAdjacentCell(cell, Threat.getOppositeDirection(Threat.getSearchDirection(axis)));

        if (left.state == right.state && left.state != MNKCellState.FREE && cell.state == MNKCellState.FREE
                && contains(cell))
            return true;
        else
            return false;
    }

    public Threat findOpenThreatExtremities(Threat t, MNKCell pivot, int size) {
        Direction oppositeDirection = Threat.getOppositeDirection(Threat.getSearchDirection(t.axis));
        t.left = getAdjacentCell(pivot, oppositeDirection);
        // find a possible left extremity and count the no. of marked cells to the left
        // of the pivot

        while (t.left.state == pivot.state && t.size < size && contains(t.left)) {
            t.size++;
            t.left = getAdjacentCell(t.left, oppositeDirection);
        }

        // right extremity
        Direction searchDirection = Threat.getSearchDirection(t.axis);
        t.right = getAdjacentCell(pivot, searchDirection);

        // count the no. of marked cells to the right of the pivot
        while (contains(t.right) && t.right.state == pivot.state && t.size < size) {
            t.size++;
            t.right = getAdjacentCell(t.right, searchDirection);
        }
        return t;
    }

    public boolean isStandaloneValidOpenThreat(Threat t, int size) {
        return t.left.state == MNKCellState.FREE && t.right.state == MNKCellState.FREE
                && contains(t.left) && contains(t.right)
                && t.size == size
                && t.jumps == 0;
    }

    // now performs search by pivoting aronund a cell
    // improve diagonal and antidiagonal search
    // assumes pivot is a marked cell
    public void updateOpenThreatsByAxis(MNKCell pivot, int size, Axis axis) {
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

    public void updateHalfOpenThreats(MNKCell cell) {
        for (Axis axis : Axis.values()) {
            updateHalfOpenThreatsByAxis(cell, axis);
        }
    }

    public Threat findHalfOpenThreatsExtremities(Threat t, MNKCell pivot, int size) {
        // left extremitiy
        Direction oppositeDirection = Threat.getOppositeDirection(Threat.getSearchDirection(t.axis));
        t.left = getAdjacentCell(pivot, oppositeDirection);
        // look for an extremity on the left
        while ((t.left.state == pivot.state || (isJumpOnAxis(t.left, t.axis) && t.jumps < 1)) && t.size < size) {
            if (t.left.state == pivot.state) {
                t.size++;
            }
            if (isJumpOnAxis(t.left, t.axis)) {
                t.jumps++;
            }

            t.left = getAdjacentCell(t.left, oppositeDirection);
        }

        // right extremity
        Direction searchDirection = Threat.getSearchDirection(t.axis);
        t.right = getAdjacentCell(pivot, searchDirection);
        // look for an extremity on the right
        while ((t.right.state == pivot.state || (isJumpOnAxis(t.right, t.axis) && t.jumps < 1)) && t.size < size) {
            if (t.right.state == pivot.state) {
                t.size++;
            }
            if (isJumpOnAxis(t.right, t.axis)) {
                t.jumps++;
            }

            t.right = getAdjacentCell(t.right, oppositeDirection);
        }
        return t;
    }

    public boolean isStandaloneValidHalfOpenThreat(Threat t, MNKCell pivot) {
        boolean zeroExtMarkedByPlayer = t.left.state != pivot.state && t.right.state != pivot.state;
        boolean exactlyOneExtIsFreeAndInBounds = (t.left.state == MNKCellState.FREE
                && contains(t.left)) ^ (t.right.state == MNKCellState.FREE && contains(t.right));
        boolean leqOneExtMarkedByPlayer = !(t.left.state == pivot.state && t.right.state == pivot.state);

        return t.size == K - 1
                &&
                ((t.jumps == 0 && exactlyOneExtIsFreeAndInBounds && zeroExtMarkedByPlayer)
                        || (t.jumps == 1 && leqOneExtMarkedByPlayer));
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
    public void updateHalfOpenThreatsByAxis(MNKCell pivot, Axis axis) {
        Threat t = new Threat();
        t.axis = axis;
        t.player = pivot.state;
        t.size = 1;
        t.tt = ThreatType.HALF_OPEN;
        t.jumps = 0;

        t = findHalfOpenThreatsExtremities(t, pivot, K-1);

        if (isStandaloneValidHalfOpenThreat(t, pivot))
            addThreat(t);
    }

    public void addThreat(Threat t) {
        // check if threat is redundant
        redundancyCheck(t);
    }



    /**
     * assumes cell to belong to the oppoent of t.player
     * 
     * @param t
     * @param cell
     * @return
     */
    public Threat updateOpenThreatExtremities(Threat t, MNKCell cell) {
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
     * @todo did not change the extremity when cell was marked
     * @param t
     * @param cell
     * @return
     */
    public Threat updateHalfOpenThreatsExtremities(Threat t, MNKCell cell) {

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

    public Threat reviseThreat(Threat t, MNKCell cell) {
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
    public void updateOpponentThreats(MNKCell cell) {
        for (Axis axis : Axis.values()) {
            HashSet<Threat> axisThreats = new HashSet<>();
            for (Threat t : getPlayerThreats(Player.getOpponent(cell.state)).getThreatsByAxis(axis)) {
                if (!t.contains(cell))
                    axisThreats.add(t);
                else {
                    t = reviseThreat(t, cell);
                    if (t.size != 0)
                        axisThreats.add(t);
                }
            }
            // check if it actually works
            getPlayerThreats(Player.getOpponent(cell.state)).setThreatsByAxis(axis, axisThreats);
        }
    }

    public void updateThreats(int lastMoves) {
        MNKCell[] MC = getMarkedCells();
        for (int i = lastMoves; i > 0; i--) {
            // check for no longer valid threats or check for them when performing the
            // redundancy check

            MNKCell pivot = MC[MC.length - i];
            updateOpponentThreats(pivot);
            // need to update the methods to perfrom the search in both directions
            updateOpenThreats(pivot, K - 1);
            updateOpenThreats(pivot, K - 2);
            updateHalfOpenThreats(pivot);
        }
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

    /**
     * no checks whatsoever on the threats
     * assumes the threats are updated to the last move
     * 
     * @param t1
     * @param t2
     * @return true if the threats have at least one adjacent extremity
     */
    public boolean differByOne(Threat t1, Threat t2) {
        Direction searchDirection = Threat.getSearchDirection(t1.axis);
        Direction oppDirection = Threat.getOppositeDirection(searchDirection);

        // if t1's left extremity is adjacent to t2's left extremity
        // or t1's right extremity is adjacent to t2' right extremity
        if (t1.left.equals(getAdjacentCell(t2.left, searchDirection))
                || t1.left.equals(getAdjacentCell(t2.left, oppDirection))
                || t1.right.equals(getAdjacentCell(t2.right, searchDirection))
                || t1.right.equals(getAdjacentCell(t2.right, oppDirection))) {
            return true;
        }

        return false;
    }

    // update so that diagonal axes take constant time
    public boolean containerContainsContained(Threat container, Threat contained) {
        return container.contains(contained.left) && container.contains(contained.right);
    }

    public ThreatsByAxis getPlayerThreats(MNKCellState state) {
        if (state == MNKCellState.P1)
            return p1Threats;
        else if (state == MNKCellState.P2)
            return p2Threats;
        else
            return null;
    }

    public void setPlayerThreats(MNKCellState state, ThreatsByAxis threats) {
        switch (state) {
            case P1: {
                p1Threats = threats;
                break;
            }
            case P2: {
                p2Threats = threats;
                break;
            }
            default:
                break;
        }
    }

    /**
     * Some subset of threats can be closed upon marking the same cell,
     * so this makes sure that only one threat is counted
     */
    public void redundancyCheck(Threat t) {
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
}
