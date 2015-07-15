package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/*
 * HashMap to hold EdgeMotif
 * 		EdgeMotif is a hashMap to hold 
 * 			(receiver, < time, MotifInstance >)
 * 		  MotifInstance is a triple <time, type, motif-vector>
 */
public class Allmotif {
    public HashMap<Integer, EdgeMotif> all = new HashMap<Integer, EdgeMotif>();

    public EdgeMotif get(Integer who) {
        return (this.all.get(who));
    }

    public void put(Integer who, EdgeMotif motif) {
        this.all.put(who, motif);
    }

    public String getEdge(int s, int r) {
        String out = new String();
        HashMap<Integer, MotifInstance> temp = this.all.get(s).outmotif.get(r);
        HashMap<Integer, Integer> ytemp = this.all.get(s).result.get(r);

        StringBuilder output = new StringBuilder();
        /*
		 * format: 
		 * sender, receiver, time, Y_(t+1), type, motif(16)
		 */
        for (int t : temp.keySet()) {
            output.append(s);
            output.append(",");
            output.append(r);
            output.append(",");
            output.append(t);
            output.append(",");
            output.append(ytemp.get(t));
            output.append(",");
            output.append(temp.get(t).type);
            output.append(Arrays.toString(temp.get(t).count).replace("[", ",").replace("]", "\n"));
        }
        return (output.toString());
    }

    public String getEdgeSparseOutput(int s, int r, int maxtime) {
        String out = new String();
        HashMap<Integer, MotifInstance> temp = this.all.get(s).outmotif.get(r);
        HashMap<Integer, Integer> ytemp = this.all.get(s).result.get(r);

        StringBuilder output = new StringBuilder();
		/*
		 * format: 
		 * sender, receiver, t, Y_(t+1), feature_index, feature_count
		 */
        for (int t = 0; t < maxtime; t++) {
            // if the time is not recorded, simply output zero
            if (!temp.keySet().contains(t)) {
                //output.append(s + "," + r + "," + ytemp.get(t) + "," +"0\n");
            } else {
                for (int index = 0; index < 16; index++) {
                    if (temp.get(t).count[index] != 0) {
                        output.append(s);
                        output.append(",");
                        output.append(r);
                        output.append(",");
                        output.append(t);
                        output.append(",");
                        output.append(ytemp.get(t));
                        output.append(",");
                        output.append(temp.get(t).type * 16 + index);
                        output.append(",");
                        output.append(temp.get(t).count[index] + "\n");
                    }
                }
            }
        }
        return (output.toString());
    }

    public void getFeature(int s, int r, int maxtime, int lag) {
        String out = new String();
        HashMap<Integer, MotifInstance> temp = this.all.get(s).outmotif.get(r);
        HashMap<Integer, Integer> ytemp = this.all.get(s).result.get(r);

        StringBuilder output = new StringBuilder();
		/*
		 * format: 
		 * sender, receiver, t, Y_(t+1), feature_index, feature_count
		 */
        for (int t1 = 0; t1 < maxtime - lag; t1++) {
            if (temp.keySet().contains(t1)) {
                // construct the feature map for this time
                HashMap<Integer, Integer> featuremap = new HashMap<Integer, Integer>();
                // get the outcome for this pair at t1 + lag time
                int outcome = ytemp.get(t1 + lag);
                // populate the feature map until t1 + lag time
                for (int t2 = t1; t2 < t1 + lag; t2++) {
                    for (int i = 0; i < temp.get(t2).count.length; i++) {
                        if (temp.get(t2).count[i] > 0) featuremap.put((t2 - t1) * 64 + i, temp.get(t2).count[i]);
                    }
                }
                // get this feature and outcome out
				/*
				 * to finish here.
				 */
            }

        }
        return;
    }

    public void print(String path) throws IOException {
        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(path)));
        for (int s : this.all.keySet()) {
            for (int r : this.all.get(s).outmotif.keySet()) {
                out.write(getEdge(s, r));
            }
        }
        out.close();
    }

    public void print(String path, int maxTime) throws IOException {
        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(path)));
        for (int s : this.all.keySet()) {
            for (int r : this.all.get(s).outmotif.keySet()) {
                out.write(getEdgeSparseOutput(s, r, maxTime));
            }
        }
        out.close();
    }


}
