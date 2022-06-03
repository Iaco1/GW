package mnkgame;

/**
 * Let k (> 1) be the number of symbols to align to win the game and i a number \in [1, k-1]
 * 1. An Open threat is: k-i consecutively aligned symbols with two free extremities
 * 
 * 2. An Half Open threat is either:
 *      - k-i consecutively aligned symbols with one free extremity
 *      - k-i aligned symbols with a jump (or hole) and 0,1 or 2 free extremities
 * 
 * 3. A closed threat is: k-i consecutively aligned symbols with no free extremities
 */
public enum ThreatType {
    OPEN, HALF_OPEN;
}