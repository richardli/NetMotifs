package data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zehangli on 12/13/15.
 * Extends NodeMotif,
 * - adding class: motifCount_neighbour,
 * - updating class: printTo
 */
public class NodeMotifwithNeighbour extends NodeMotif{
    public HashMap<Integer, Double> motif_from_in = new HashMap<Integer, Double>();
    public HashMap<Integer, Double> motif_from_out = new HashMap<Integer, Double>();
    public HashMap<Integer, Double> motif_from_mut = new HashMap<Integer, Double>();

    /** Objects for historical map **/
    public Set<Integer> hList = new HashSet<Integer>();

    /** Objects related to more nodal features**/
    public boolean type1 = false;
    public boolean type2 = false;
    public boolean type3 = false;

    public void setType1 (boolean x){this.type1 = x;}
    public void setType2 (boolean x){this.type2 = x;}
    public void setType3 (boolean x){this.type3 = x;}


    public NodeMotifwithNeighbour(int id) {
        super(id);
    }

    public NodeMotifwithNeighbour(int id, double t, int y, int label) {
       super(id, t, y, label);
    }


    // function to count how many type n node in the given ids and HashMap
    public int TypeCount(NodeMotifHashMap nodeMap, Set<Integer> ID, int type){
        int count = 0;
        if(type == 1){
            for(int node : ID){
                if(nodeMap.nodes.get(node) == null) continue;
                if(nodeMap.nodes.get(node).type1) count ++;
            }
        }
        if(type == 2){
            for(int node : ID){
                if(nodeMap.nodes.get(node) == null) continue;
                if(nodeMap.nodes.get(node).type2) count ++;
            }
        }
        if(type == 3){
            for(int node : ID){
                if(nodeMap.nodes.get(node) == null) continue;
                if(nodeMap.nodes.get(node).type3) count ++;
            }
        }
        return(count);
    }

    // function to count network size and for each type
    public double[] TypeAnalysis(NodeMotifHashMap nodeMap){
        /** return:
         *   for each type: m = 1, s = 2, r = 3, all = 4, hist = 5
         *   0. network size
         *   1-3. type1 - type3 size
         *   4-6 type1 - type3 ratio
         **/
        double[] out = new double[5 * 7];

        out[0] = this.mList.size();
        out[1] = TypeCount(nodeMap, this.mList, 1);
        out[2] = TypeCount(nodeMap, this.mList, 2);
        out[3] = TypeCount(nodeMap, this.mList, 3);
        out[4] = (out[0] == 0)? 0 : out[1] / (out[0] + 0.0);
        out[5] = (out[0] == 0)? 0 : out[2] / (out[0] + 0.0);
        out[6] = (out[0] == 0)? 0 : out[3] / (out[0] + 0.0);

        int counter = 7;
        out[counter + 0] = this.sList.size();
        out[counter + 1] = TypeCount(nodeMap, this.sList, 1);
        out[counter + 2] = TypeCount(nodeMap, this.sList, 2);
        out[counter + 3] = TypeCount(nodeMap, this.sList, 3);
        out[counter + 4] = (out[counter + 0] == 0)? 0 : out[counter + 1] / (out[counter + 0] + 0.0);
        out[counter + 5] = (out[counter + 0] == 0)? 0 : out[counter + 2] / (out[counter + 0] + 0.0);
        out[counter + 6] = (out[counter + 0] == 0)? 0 : out[counter + 3] / (out[counter + 0] + 0.0);

        counter += 7;
        out[counter + 0] = this.rList.size();
        out[counter + 1] = TypeCount(nodeMap, this.rList, 1);
        out[counter + 2] = TypeCount(nodeMap, this.rList, 2);
        out[counter + 3] = TypeCount(nodeMap, this.rList, 3);
        out[counter + 4] = (out[counter + 0] == 0)? 0 : out[counter + 1] / (out[counter + 0] + 0.0);
        out[counter + 5] = (out[counter + 0] == 0)? 0 : out[counter + 2] / (out[counter + 0] + 0.0);
        out[counter + 6] = (out[counter + 0] == 0)? 0 : out[counter + 3] / (out[counter + 0] + 0.0);

        counter += 7;
        out[counter + 0] = out[0] + out[7] + out[14];
        out[counter + 1] = out[1] + out[8] + out[15];
        out[counter + 2] = out[2] + out[9] + out[16];
        out[counter + 3] = out[3] + out[10] + out[17];
        out[counter + 4] = (out[counter + 0] == 0)? 0 : out[counter + 1] / (out[counter + 0] + 0.0);
        out[counter + 5] = (out[counter + 0] == 0)? 0 : out[counter + 2] / (out[counter + 0] + 0.0);
        out[counter + 6] = (out[counter + 0] == 0)? 0 : out[counter + 3] / (out[counter + 0] + 0.0);

        counter += 7;
        out[counter + 0] = this.hList.size();
        out[counter + 1] = TypeCount(nodeMap, this.hList, 1);
        out[counter + 2] = TypeCount(nodeMap, this.hList, 2);
        out[counter + 3] = TypeCount(nodeMap, this.hList, 3);
        out[counter + 4] = (out[counter + 0] == 0)? 0 : out[counter + 1] / (out[counter + 0] + 0.0);
        out[counter + 5] = (out[counter + 0] == 0)? 0 : out[counter + 2] / (out[counter + 0] + 0.0);
        out[counter + 6] = (out[counter + 0] == 0)? 0 : out[counter + 3] / (out[counter + 0] + 0.0);




        return(out);
    }



    // function to deal with construction of \tilde X_{ij}
    public double tildeLand(double before, int add){
        // double out = before + add;
        double out = (add == 0) ? before : (before + Math.log(add + 0.0));
        return(out);
    }

    //  new method to grab motif counts from neibour nodes
    public void motifCount_neighbour(NodeMotifHashMap nodeMap) {
        // neighbour by incoming links
        if(this.rList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_in.put(i, 0.0);
            }
        }else{
            for(int sender : this.rList){
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(sender).motif;
                for(int i = 0; i < 121; i++){
                    if(this.motif_from_in.get(i) == null){
                        this.motif_from_in.put(i,  tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_in.put(i,  tildeLand(this.motif_from_in.get(i), neiMotif.get(i)));
                    }
                }
            }
        }

        // neighbour by outgoing links
        if(this.sList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_out.put(i, 0.0);
            }
        }else{
            for(int receiver : this.sList){
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(receiver).motif;
                for(int i = 0; i < 121; i++){
                    if(this.motif_from_out.get(i) == null){
                        this.motif_from_out.put(i,  tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_out.put(i,  tildeLand(this.motif_from_out.get(i), neiMotif.get(i)));
                    }
                }
            }
        }

        // neighbour by mutual links
        if(this.mList.size() == 0){
            for(int i = 0; i < 121; i++){
                this.motif_from_mut.put(i, 0.0);
            }
        }else {
            for (int friend : this.mList) {
                HashMap<Integer, Integer> neiMotif = nodeMap.nodes.get(friend).motif;
                for (int i = 0; i < 121; i++) {
                    if(this.motif_from_mut.get(i) == null){
                        this.motif_from_mut.put(i, tildeLand(0, neiMotif.get(i)));
                    }else{
                        this.motif_from_mut.put(i, tildeLand(this.motif_from_mut.get(i), neiMotif.get(i)));
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
            for (int i = 0; i < nvar; i++) {
                sb.append(this.motif.get(i));
                sb.append(",");
            }
        }else if(which == 2){
            for (int i = 0; i < nvar; i++) {
                sb.append(this.motif_from_in.get(i))
                .append(",");
            }
        }else if(which == 3){
            for (int i = 0; i < nvar; i++) {
                sb.append(this.motif_from_out.get(i))
                .append(",");
            }
        }else if(which == 4){
            for (int i = 0; i < nvar; i++) {
                sb.append(this.motif_from_mut.get(i))
                .append(",");
            }
        }

        // add columns to check outlier is indeed removed
        sb.append(this.inFreq + ",")
        .append(this.outFreq + ",")
        .append(this.sList.size() + ",")
        .append(this.rList.size() + ",")
        .append(this.mList.size() + ",")
        .append(this.nList.size())
        .append("\n");
        sc.write(sb.toString());
    }

}
