package mnkgame;

public class EvaluationBoard {
    protected String[][] valueMap;

    public EvaluationBoard(){}

    public EvaluationBoard(int m, int n){
        valueMap = new String[m][n];
    }

    /**
     * Converts MNKCellState to a length 1 String
     * @param cs The MNKCellState you want to visualise
     * @return The String corresponding to a Tic-Tac-Toe Symbol
     * @author Davide Iacomino
     */
    public static String toSymbol(MNKCellState cs){
        switch(cs){
            case P1:{ return "X"; }
            case P2:{ return "O"; }
            case FREE: { return " "; }
            default: return "";
        }
    }
    
    public void assignMark(MNKCell cell){
        valueMap[cell.i][cell.j] = toSymbol(cell.state);
    }

    public void assignValue(Double value, MNKCell cell){
        valueMap[cell.i][cell.j] = value.toString();
    }

    public void printBoard(int move){
        System.out.println("________MOVE" + move + "____");
        for(String[] row : valueMap){
            for(String element : row){
                System.out.print(element+"      ");
            }
            System.out.println();
        }
        System.out.println("________GW________");
    }
}