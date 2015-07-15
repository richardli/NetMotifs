package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


public class NeatMotif {
    int featurecount;
    HashMap<Integer, HashMap<Integer, Integer>> dict = new HashMap<Integer, HashMap<Integer, Integer>>();
    HashSet<Integer> badnode = new HashSet<Integer>();
    // node - time - (in, out, mut)
    HashMap<Integer, HashMap<Integer, Integer[]>> degbyTime = new HashMap<Integer, HashMap<Integer, Integer[]>>();


    HashMap<Integer, HashMap<Integer, Integer[]>> features = new HashMap<Integer, HashMap<Integer, Integer[]>>();
    HashMap<Integer, HashMap<Integer, Integer>> result = new HashMap<Integer, HashMap<Integer, Integer>>();

    public NeatMotif(int featurecount) {
        this.featurecount = featurecount;
    }


    public void read(String data, String deg, int maxPeriod) throws NumberFormatException, IOException {
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(data)));
        BufferedReader scdeg = new BufferedReader(new BufferedReader(new FileReader(deg)));
        String line;
        int ecount = 0;
        int starttime = 0;

        while ((line = scdeg.readLine()) != null) {
            String[] sp = line.split(",");
            int node = Integer.parseInt(sp[0]);
            int time = Integer.parseInt(sp[1]);
            if (time < starttime) starttime = time;

            Integer[] degs = new Integer[3];
            degs[0] = Integer.parseInt(sp[2]);
            degs[1] = Integer.parseInt(sp[3]);
            degs[2] = Integer.parseInt(sp[4]);

            HashMap<Integer, Integer[]> temp = new HashMap<Integer, Integer[]>();
            temp.put(time, degs);
            this.degbyTime.put(node, temp);
        }
        scdeg.close();

		/*
		 * delete outliers
		 */
        for (int node : degbyTime.keySet()) {
            int allin = 0;
            int allout = 0;
            for (Integer[] tmp : degbyTime.get(node).values()) {
                allin += tmp[0];
                allout += tmp[1];
            }
            if (allin * allout == 0 & (allin + allout > 1000)) {
                badnode.add(node);
            }
        }

        while ((line = sc.readLine()) != null) {
            String[] sp = line.split(",");
            int sender = Integer.parseInt(sp[0]);
            int receiver = Integer.parseInt(sp[1]);
            if (badnode.contains(sender) | badnode.contains(receiver)) {
                continue;
            }

            int time = Integer.parseInt(sp[2]);
            int y = Integer.parseInt(sp[3]);
            int type = Integer.parseInt(sp[4]) - starttime;

            Integer[] x = new Integer[64];
            for (int i = 0; i < x.length; i++) {
                x[type * 16 + i] = Integer.parseInt(sp[i + 5]);
            }

            int whichedge;
            if (this.dict.get(sender) != null) {
                if (this.dict.get(sender).get(receiver) != null) {
                    whichedge = this.dict.get(sender).get(receiver);
                } else {
                    this.dict.get(sender).put(receiver, ecount);
                    whichedge = ecount;
                    ecount++;
                }
            } else {
                this.dict.put(sender, new HashMap<Integer, Integer>());
                this.dict.get(sender).put(receiver, ecount);
                whichedge = ecount;
                ecount++;
            }
            if (this.features.get(whichedge) == null) {
                HashMap<Integer, Integer[]> tmp = new HashMap<Integer, Integer[]>();
                HashMap<Integer, Integer> tmpresult = new HashMap<Integer, Integer>();
                tmp.put(time, x);
                tmpresult.put(time, y);
                this.features.put(whichedge, tmp);
                this.result.put(whichedge, tmpresult);
            } else {
                this.features.get(whichedge).put(time, x);
                this.result.get(whichedge).put(time, y);
            }
        }
        sc.close();
    }

}
