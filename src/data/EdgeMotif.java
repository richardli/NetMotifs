package data;

import java.util.HashMap;

/*
* 	EdgeMotif is a hashMap to hold 
* 		< receiver, < time, MotifInstance >)
* 		  MotifInstance is a triple <time, type, motif-vector>
*/
public class EdgeMotif {
    // receiver - time - motif
    public HashMap<Integer, HashMap<Integer, MotifInstance>> outmotif = new HashMap<Integer, HashMap<Integer, MotifInstance>>();
    // receiver - time - 0/1 (yes or no) --> the yes/no is whether a tie formed at t+1
    public HashMap<Integer, HashMap<Integer, Integer>> result = new HashMap<Integer, HashMap<Integer, Integer>>();

    // add a motif count at time t
    public void add(int receiver, int t, int type, int[] motif) {
        MotifInstance temp = new MotifInstance(t, type, motif);
        if (this.outmotif.get(receiver) != null) {
            this.outmotif.get(receiver).put(t, temp);
        } else {
            HashMap<Integer, MotifInstance> tempmap = new HashMap<Integer, MotifInstance>();
            tempmap.put(t, temp);
            this.outmotif.put(receiver, tempmap);
        }
    }

    // add a tie result at time t
    public void addresult(int receiver, int t, int tie) {
        if (this.result.get(receiver) != null) {
            this.result.get(receiver).put(t, tie);
        } else {
            HashMap<Integer, Integer> newtie = new HashMap<Integer, Integer>();
            newtie.put(t, tie);
            this.result.put(receiver, newtie);
        }
    }


}
