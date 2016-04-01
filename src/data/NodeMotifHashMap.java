package data;

import java.util.HashMap;

/**
 * Created by zehangli on 12/13/15.
 */
public class NodeMotifHashMap {
    public HashMap<Integer, NodeMotifwithColorNeighbour> nodes;

    public NodeMotifHashMap(){
        this.nodes = new HashMap<Integer, NodeMotifwithColorNeighbour>();
    }
}
