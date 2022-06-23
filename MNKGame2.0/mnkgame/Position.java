package mnkgame;

/**
 * Describes a point on a 2D plane
 */
public class Position {
    public Integer i;
    public Integer j;

    public Position(){}
    
    public Position(Integer i, Integer j){
        this.i = i;
        this.j = j;
    }
    
    public static boolean samePosition(MNKCell c1, MNKCell c2){ return c1.i == c2.i && c1.j == c2.j; }

    public String toString(){ return "(" + i + "," + j + ")"; }
}
