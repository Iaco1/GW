package mnkgame;

public class EvaluationBoard {
    protected String[][] valueMap;

    public EvaluationBoard(){}

    public EvaluationBoard(int m, int n){
        valueMap = new String[m][n];
        for(String[] row : valueMap){
            for(String element : row){
                element = "FREE";
            }
        }
    }

    /**
     * Converts MNKCellState to a length 1 String
     * @param cs The MNKCellState you want to visualise
     * @return The String corresponding to a Tic-Tac-Toe Symbol
     * @author Davide Iacomino
     */
    public static String toSymbol(MNKCellState cs){
        switch(cs){
            case P1:{ return "XXXX"; }
            case P2:{ return "OOOO"; }
            case FREE: { return "FREE"; }
            default: return "";
        }
    }
    
    public void assignMark(MNKCell cell){
        valueMap[cell.i][cell.j] = toSymbol(cell.state);
    }

    public void assignValue(Double value, MNKCell cell){
        //this should be cell.state == gw's player state but I can't access it and -1.0 are only assigned to gw's cells anyway
        if(value.equals(-1.0) && cell.state != MNKCellState.FREE) valueMap[cell.i][cell.j] = toSymbol(cell.state);
        else valueMap[cell.i][cell.j] = value.toString();
    }

    public void printBoard(int move){
        System.out.println("________MOVE" + move + "____");
        for(String[] row : valueMap){
            for(String element : row){
                if(element == null) System.out.print("FREE\t");
                else System.out.print(element.substring(0, 4) + "\t");
            }
            System.out.println();
        }
        System.out.println("________GW________");
    }
}