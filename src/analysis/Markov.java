package analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Markov {
    long id;
    ArrayList<Long> neigh = new ArrayList<Long>();
    // mc: arraylist of vectors of length 4, order:
    //     no-no, no-yes, yes-no, yes-yes
    ArrayList<Integer[]> mc = new ArrayList<Integer[]>();
    ArrayList<Integer> timelast = new ArrayList<Integer>();

    public Markov(long id, long firstnei, int index) {
        this.id = id;
        this.neigh.add(firstnei);
        Integer[] mcfirst = new Integer[4];
        // note Integer[] needs to initialize manually, not int[]
        for (int i = 0; i < 4; i++) mcfirst[i] = 0;
        this.timelast.add(index);
        this.mc.add(mcfirst);
    }

    public void add(long nextnei, int index) {
        int whichplace = this.neigh.indexOf(nextnei);
        if (whichplace != -1) {
            int timePrev = this.timelast.get(whichplace);
            if (timePrev < index) {
                this.timelast.set(whichplace, index);
                int daysPassed = index - timePrev;
                if (daysPassed > 1) {
                    this.mc.get(whichplace)[0] += daysPassed - 2;
                    this.mc.get(whichplace)[1] += 1;
                    this.mc.get(whichplace)[2] += 1;
                } else {
                    this.mc.get(whichplace)[3] += 1;
                }
            }
        } else {
            this.neigh.add(nextnei);
            Integer[] nextmc = new Integer[4];
            for (int i = 0; i < 4; i++) nextmc[i] = 0;
            this.mc.add(nextmc);
            this.timelast.add(index);
        }
    }

    public void updateTofinal(int indexT) {
        for (int i = 0; i < this.timelast.size(); i++) {
            if (this.timelast.get(i) < indexT) {
                this.mc.get(i)[2] += 1;
                this.mc.get(i)[0] += indexT - this.timelast.get(i) - 1;
            }
        }
    }

    public void Print(BufferedWriter w) throws IOException {
        for (int i = 0; i < this.timelast.size(); i++) {
            w.write(Arrays.toString(this.mc.get(i)).replace("[", "").replace("]", "\n"));
        }
        w.write("\n");
    }
}
