package analysis;

import com.google.common.collect.Sets;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Class to count Motifs
 *
 * Updated by zehangli on 7/14/15.
 */

public class countMM {
    /**
     * counts: Map[ , motif count vector]
     * friednmap: Map[sender ID, Set[receiver ID]]
     */

    HashMap<Integer, Integer[]> counts = new HashMap<Integer, Integer[]>();
    HashMap<String, HashSet<String>> friendMap = new HashMap<String, HashSet<String>>();


    public void mapprint(BufferedWriter out) throws IOException {
        for (String sender : this.friendMap.keySet()) {
            for (String receiver : this.friendMap.get(sender)) {
                out.write(sender + "," + receiver + "/n");
            }
        }
        out.close();
    }

    // read phone file
    public void read(String file) throws ParseException, NumberFormatException, IOException {
        // int lastIndex = 0;
        int lastIndex = GlobalHelper.absoluteStartingValue;
        double time;
        String line;
        Set<String> sList = new HashSet<String>();
        Set<String> rList = new HashSet<String>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            String[] field = line.split("\\|");
            if (field[4].charAt(0) != '-') {
                                // time = GlobalHelper.parseTime(field);
                String sender = field[0];
                String receiver = field[1];

                                // int currentIndex = (int) (time / 24.0);
                // current index is the integer version of year-month-date
                int currentIndex = Integer.parseInt(field[2]);

                /**
                 *
                 * check if last day has been reached
                 *
                 * wait this seems not right ??!!
                 *
                 */

                if (currentIndex != lastIndex) {
                    // save four things: last day, sender size, receiver size, union size
                    Integer[] output = new Integer[4];
                    output[0] = lastIndex;
                    output[1] = sList.size();
                    output[2] = rList.size();
                    output[3] = Sets.union(sList, rList).size();

                    // save four things to counts HashMap
                    this.counts.put(lastIndex, output);

                    // re-initialize sList and rList, print to screen the count summary
                    sList = new HashSet<String>();
                    rList = new HashSet<String>();
                    System.out.println(output);
                }

                // if
                sList.add(sender);
                rList.add(receiver);
                lastIndex = currentIndex;

            }
        }
        Integer[] output = new Integer[4];
        output[0] = lastIndex;
        output[1] = sList.size();
        output[3] = Sets.union(sList, rList).size();
        this.counts.put(lastIndex, output);
        br.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON-new.all.txt";
        String output = "/data/rwanda_anon/richardli/friendMap" + args[0] + "-daily.txt";
        //		countMM run = new countMM();
        //		run.read(mmfile);
        //
        //		// output to file
        //		BufferedWriter sc = new BufferedWriter(new FileWriter(output));
        //		for(int i: run.counts.keySet()){
        //			sc.write(Arrays.toString(run.counts.get(i)).replace("[", "").replace("]", "\n"));
        //		}
        //		sc.close();

        countMM run = new countMM();
        GlobalHelper.mapread(mmfile, Integer.parseInt(args[0]), Integer.parseInt(args[1]), run.friendMap);
        BufferedWriter sc = new BufferedWriter(new FileWriter(output));
        run.mapprint(sc);
    }
}
