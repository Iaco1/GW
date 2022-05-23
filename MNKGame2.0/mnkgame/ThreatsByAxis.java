package mnkgame;

import java.util.HashSet;

/**
 * Describes the set of all threats of a particular kind (e.g. k-1 open threats) grouped by axis
 */
public class ThreatsByAxis {
    public HashSet<Threat> horizontal;
    public HashSet<Threat> vertical;
    public HashSet<Threat> nw_se;
    public HashSet<Threat> ne_sw;

    /**
     * Initialises the sets to empty
     */
    public ThreatsByAxis(){
        horizontal = new HashSet<>();
        vertical = new HashSet<>();
        nw_se = new HashSet<>();
        ne_sw = new HashSet<>();
    }
    /**
     * @return The sum of the sizes for threats on all 4 axes
     */
    public int getSize(){ return horizontal.size() + vertical.size() + nw_se.size() + ne_sw.size(); }
    
    public HashSet<Threat> getHashSet(Axis axis){
        switch(axis){
            case HORIZONTAL: return horizontal;
            case VERTICAL: return vertical;
            case NW_SE: return nw_se;
            case NE_SW: return ne_sw;
            default: return null;
        }
    }
    
}
