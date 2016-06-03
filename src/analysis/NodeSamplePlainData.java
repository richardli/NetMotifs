package analysis;

import data.NodeMotifHashMap;
import data.NodeMotifwithColorNeighbour;
import data.NodeMotifwithNeighbour;
import util.MotifOrder;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by zehangli on 12/13/15.
 * Simple version of Motif counting, two input files, one [ID, outcome], another [ID1, ID2] directed edge list
 * Time stamp not used
 */
public class NodeSamplePlainData{
    /*
    * extends the motif counting class to also return neighbour motif sums
    */
    public NodeMotifHashMap allMotif = new NodeMotifHashMap();
    // dictionary of IDs
    public HashMap<String, Integer> dict = new HashMap<String, Integer>();
    // initialize size of nodes
    int allSize = 0;
    // a set of integers as sample
    public HashSet<Integer> sample = new HashSet<Integer>();

    /**
     *   Method to read node membership, from a file with two columns:
     *   [id, outcome]
     **/
    public void readMM(String file) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));
        int nextindex = 0;
        while ((line = br.readLine()) != null) {
            String[] field = line.split(" ");
            String id = field[0];
            Integer outcome = Integer.parseInt(field[1]);

            this.dict.put(id, nextindex);
            this.allMotif.nodes.put(nextindex, new NodeMotifwithColorNeighbour(id, nextindex, 12345, 1 - 2*outcome ,
                    outcome));
            nextindex++;
        }
        br.close();
    }

    /**
     * Method read phone record without time stamp
     * [sender ID, receiver ID]
     **/
    public void readPhone(String file) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            // each lines look like: "id outcome"
            String[] field = line.split(" ");
            String s = field[0];
            String r = field[1];
            if (this.dict.get(s) == null | this.dict.get(r) == null) {
                System.out.println("error, nodes without outcome");
            }else{
                int sid = this.dict.get(s);
                int rid = this.dict.get(r);
                this.allMotif.nodes.get(sid).sendto(rid);
                this.allMotif.nodes.get(rid).recfrom(sid);
            }
        }
        br.close();
    }

    /**
     * Main method to count motifs
     *
     **/
    public static void main(String[] args) throws IOException, ParseException {

        // read outcome file
        String outcomefile = "/Users/zehangli/Bitbucket-repos/NetMotif/data/3motif-outcome.txt";
        String edgelist = "/Users/zehangli/Bitbucket-repos/NetMotif//data/3motif-all.txt";


        // specify file header to output
        String output = "/Users/zehangli/Bitbucket-repos/NetMotif//data/3motif.txt";
        String outputYes = "/Users/zehangli/Bitbucket-repos/NetMotif//data/3motif_yes.txt";
        String outputNo = "/Users/zehangli/Bitbucket-repos/NetMotif//data/3motif_no.txt";

        NodeSamplePlainData fullData = new NodeSamplePlainData();

        fullData.readMM(outcomefile);
        fullData.readPhone(edgelist);

        for (int j : fullData.allMotif.nodes.keySet()) {
            fullData.allMotif.nodes.get(j).organize();
        }

        // count motifs
        System.out.println("Start counting motif for each node");
        int tempCount = 0;
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).motifCount_wlabel(fullData.allMotif);
            tempCount++;
            if (tempCount % 1000 == 0) {
                System.out.printf("-");
            }
        }
        System.out.println("Start counting neighbour motif for each node");
        tempCount = 0;
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).motifCount_neighbour(fullData.allMotif);
            tempCount++;
            if (tempCount % 1000 == 0) System.out.printf("-");
        }

        /**
         * Change the order of the motifs to the final version
         */

        System.out.println("Start changing motif back to final version");
        tempCount = 0;
        for(int j : fullData.dict.values()){
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }

//            int test = fullData.allMotif.nodes.get(j).motif.get(27);
            MotifOrder.changeOrder2(fullData.allMotif.nodes.get(j).motif);
//            Boolean test1 = (test == fullData.allMotif.nodes.get(j).motif.get(27));
//            Boolean test2 = (test == fullData.allMotif.nodes.get(j).motif.get(10));
//            if(!test1) System.out.printf("v");
//            if(!test2) System.out.printf(test1.toString() + test2.toString() + "\n");
            MotifOrder.changeOrderDouble2(fullData.allMotif.nodes.get(j)
                    .motif_from_no);
            MotifOrder.changeOrderDouble2(fullData.allMotif.nodes.get(j)
                    .motif_from_yes);
            tempCount++;

            if (tempCount % 1000 == 0) System.out.printf("*");
        }


        // output to file
        BufferedWriter sc = new BufferedWriter(new FileWriter(output));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc, 120, -1, true);
        }
        sc.close();

        // output to file
        BufferedWriter sc1 = new BufferedWriter(new FileWriter(outputYes));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc1, 120, 1, true);
        }
        sc1.close();

        // output to file
        BufferedWriter sc2 = new BufferedWriter(new FileWriter(outputNo));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc2, 120, 0, true);
        }
        sc2.close();

    }

}
