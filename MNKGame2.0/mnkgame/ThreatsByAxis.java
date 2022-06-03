package mnkgame;

import java.util.HashSet;

/**
 * Describes the set of all threats of a particular kind (e.g. k-1 open threats) grouped by axis
 */
public class ThreatsByAxis {
    public HashSet<Threat> horizontal;
    public HashSet<Threat> vertical;
    public HashSet<Threat> diagonal;
    public HashSet<Threat> antidiagonal;

    /**
     * Initialises the sets to empty
     */
    public ThreatsByAxis(){
        horizontal = new HashSet<>();
        vertical = new HashSet<>();
        diagonal = new HashSet<>();
        antidiagonal = new HashSet<>();
    }


    public HashSet<Threat> getThreatsByAxis(Axis axis){
        switch(axis){
            case HORIZONTAL: return horizontal;
            case VERTICAL: return vertical;
            case DIAGONAL: return diagonal;
            case ANTIDIAGONAL: return antidiagonal;
            default: return null;
        }
    }

    public void remove(Threat t){
        getThreatsByAxis(t.axis).remove(t);
    }

    public int[] getThreatSize(int k){ 
        int[] c = new int[3];
        for(Axis axis : Axis.values()){
            for(Threat t : getThreatsByAxis(axis)){
                switch(t.tt){
                    case OPEN:{
                        switch(k - t.size){
                            case 1:{
                                c[0]++;
                                break;
                            }
                            case 2:{
                                c[2]++;
                                break;
                            }
                            default: break;
                        }
                    }
                    case HALF_OPEN:{
                        if(k - t.size == 1) c[1]++;
                        break;
                    }
                    default: break;
                }
            }
        }
        return c;
    }

    public void setThreatsByAxis(Axis axis, HashSet<Threat> axisThreats){
        switch(axis){
            case HORIZONTAL:{
                horizontal = axisThreats;
                break;
            }
            case VERTICAL:{
                vertical = axisThreats;
                break;
            }
            case DIAGONAL:{
                diagonal = axisThreats;
                break;
            }
            case ANTIDIAGONAL:{
                antidiagonal = axisThreats;
                break;
            }
            default: break;
        }
    }

    public HashSet<Threat> getThreatsByType(Axis axis, ThreatType tt, int size){
        HashSet<Threat> thByType = new HashSet<>();
        for(Threat t : getThreatsByAxis(axis)){
            if(t.tt == tt && t.size == size) thByType.add(t);
        }
        return thByType;
    }
}
