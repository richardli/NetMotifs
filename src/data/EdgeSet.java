package data;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class EdgeSet {
    Set<Integer> in = new HashSet<Integer>();
    Set<Integer> out = new HashSet<Integer>();
    Set<Integer> mut = new HashSet<Integer>();

    public EdgeSet(AdjMatrix adj, int node) {
        if (adj.slist.get(node) != null && adj.rlist.get(node) != null) {
            this.mut = Sets.intersection(adj.slist.get(node), adj.rlist.get(node));
        }
        if (adj.slist.get(node) != null) {
            this.out = Sets.difference(adj.slist.get(node), this.mut);
        }
        if (adj.rlist.get(node) != null) {
            this.in = Sets.difference(adj.rlist.get(node), this.mut);
        }
    }

}
