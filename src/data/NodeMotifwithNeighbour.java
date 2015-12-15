package data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by zehangli on 12/13/15.
 * Extends NodeMotif,
 * - adding class: motifCount_neighbour,
 * - updating class: printTo
 */
public class NodeMotifwithNeighbour extends NodeMotif{
    public HashMap<Integer, Integer> motif_from_in = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> motif_from_out = new HashMap<Integer, Integer>();
    public HashMap<Integer, Integer> motif_from_mut = new HashMap<Integer, Integer>();

    public NodeMotifwithNeighbour(int id) {
        super(id);
    }

    public NodeMotifwithNeighbour(int id, double t, int y, int label) {
       super(id, t, y, label);
    }
    // function to deal with construction of \tilde X_{ij}
    public double tildeLand(int before, int add){
        double out = before + add;
        return(out);
    }

    //  new method to grab motif counts from neibour nodes
    public void motifCount_neighbour(NodeMotifHashMap nodeMap) {

        // neighbour by incoming links
        if(this.rList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_in.put(i, 0);
            }
        }else{
            for(int sender : this.rList){
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(sender).motif;
                for(int i = 0; i < 121; i++){
                    if(this.motif_from_in.get(i) == null){
                        this.motif_from_in.put(i, (int) tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_in.put(i, (int) tildeLand(this.motif_from_in.get(i), neiMotif.get(i)));
                    }
                }
            }
        }

        // neighbour by outgoing links
        if(this.sList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_out.put(i, 0);
            }
        }else{
            for(int receiver : this.sList){
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(receiver).motif;
                for(int i = 0; i < 121; i++){
                    if(this.motif_from_out.get(i) == null){
                        this.motif_from_out.put(i, (int) tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_out.put(i, (int) tildeLand(this.motif_from_out.get(i), neiMotif.get(i)));
                    }
                }
            }
        }

        // neighbour by mutual links
        if(this.mList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_mut.put(i, 0);
            }
        }else {
            for (int friend : this.mList) {
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(friend).motif;
                for (int i = 0; i < 121; i++) {
                    if(this.motif_from_mut.get(i) == null){
                        this.motif_from_mut.put(i, (int) tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_mut.put(i, (int) tildeLand(this.motif_from_mut.get(i), neiMotif.get(i)));
                    }
                }
            }
        }
    }

    /* which = 1, motif
     * which = 2, motif_from_in
     * which = 3, motif_from_out
     * which = 4, motif_from_mut
     */
    public void printTo(BufferedWriter sc,int nvar, int which)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.y)
        .append(",")
        .append(this.t)
        .append(",");

        if(which == 1){
            for (int i = 0; i < nvar - 1; i++) {
                sb.append(this.motif.get(i));
                sb.append(",");
            }
        }else if(which == 2){
            for (int i = 0; i < nvar - 1; i++) {
                sb.append(this.motif_from_in.get(i))
                .append(",");
            }
        }else if(which == 3){
            for (int i = 0; i < nvar - 1; i++) {
                sb.append(this.motif_from_out.get(i))
                .append(",");
            }
        }else if(which == 4){
            for (int i = 0; i < nvar - 1; i++) {
                sb.append(this.motif_from_mut.get(i))
                .append(",");
            }
        }
        sb.append(this.motif.get(nvar - 1));

        // add columns to check outlier is indeed removed
        sb.append(",")
        .append(this.inFreq + ",")
        .append(this.outFreq + ",")
        .append(this.sList.size() + ",")
        .append(this.rList.size() + ",")
        .append(this.mList.size() + ",")
        .append(this.nList.size())
        .append("\n");
        sc.write(sb.toString());
    }

}
