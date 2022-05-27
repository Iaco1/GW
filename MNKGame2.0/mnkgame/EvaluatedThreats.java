package mnkgame;

/**
 * Container class for threats that are considered by the simplified version of the
 * Abdoulaye-Houndji-Ezin-Aglin evaluation function
 * Namely k-1 open and half-open threats and k-2 open threats
 */
public class EvaluatedThreats {
    public ThreatsByAxis km1Open;
    public ThreatsByAxis km1HalfOpen;
    public ThreatsByAxis km2Open;

    public EvaluatedThreats(){
        km1Open = new ThreatsByAxis();
        km1HalfOpen = new ThreatsByAxis();
        km2Open = new ThreatsByAxis();
    }

    /**
     * Adds a Threat object to the correct ThreatsByAxis object
     * @param t The Threat to be added to one of the considered subsets of threats
     * @param tt The Type of threat to be added (OPEN or HALF_OPEN)
     * @param k The size of the threat that would lead to a victory
     * @param size The actual size of the threat
     */
    public void add(Threat t, ThreatType tt, int k, int size){
        switch(tt){
            case OPEN:{
                if(k > 1 && size > 0 && k > size){
                    switch(k-size){
                        case 1:{
                            //check whether actual object is changed or just a copy
                            km1Open.getHashSet(t.axis).add(t);
                            break;
                        }
                        case 2:{
                            km2Open.getHashSet(t.axis).add(t);
                            break;
                        }
                        default: return;
                    }
                }
                break;
            }
            case HALF_OPEN:{
                if(k > 1 && size > 0 && k > size && k-size == 1){
                    km1HalfOpen.getHashSet(t.axis).add(t);
                    break;
                }
                break;
            }
            default: return;
        }
    }

    public ThreatsByAxis getThreatsByType(ThreatType tt, int k, int size){
        switch(tt){
            case OPEN:{
                if(k - size == 1) return km1Open;
                else if(k - size == 2) return km2Open;
                break;
            }
            case HALF_OPEN:{
                if(k-size == 1) return km1HalfOpen;
                break;
            }
            default: break;
        }
        return null;
    }

    
}
