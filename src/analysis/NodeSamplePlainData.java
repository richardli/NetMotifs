package analysis;

import data.NodeMotifHashMap;
import data.NodeMotifwithNeighbour;

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
    public HashMap<Long, Integer> dict = new HashMap<Long, Integer>();
    // initialize size of nodes
    int allSize = 0;
    // a set of integers as sample
    public HashSet<Integer> sample = new HashSet<Integer>();

    public void readMM(String file) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));
        int nextindex = 0;
        while ((line = br.readLine()) != null) {
            // each lines look like: "id outcome"
            String[] field = line.split(" ");
            Long id = Long.parseLong(field[0]);
            Integer outcome = Integer.parseInt(field[1]);
            this.dict.put(id, nextindex);
            NodeMotifwithNeighbour test = new NodeMotifwithNeighbour(nextindex, 12345, outcome, 0);
            this.allMotif.nodes.put(nextindex, new NodeMotifwithNeighbour(nextindex, 12345, outcome, 0));
            nextindex++;
        }
        br.close();
    }
    public void readPhone(String file) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            // each lines look like: "id outcome"
            String[] field = line.split(" ");
            Long s = Long.parseLong(field[0]);
            Long r = Long.parseLong(field[1]);
            if (this.dict.get(s) == null | this.dict.get(r) == null) {
                System.out.println("error, nodes without outcome");;
            }else{
                int sid = this.dict.get(s);
                int rid = this.dict.get(r);
                this.allMotif.nodes.get(sid).sendto(rid);
                this.allMotif.nodes.get(rid).recfrom(sid);
            }
        }
        br.close();
    }
    public static void main(String[] args) throws IOException, ParseException {

        // read outcome file
        String outcomefile = "../data/flu2weekafter.txt";
        String edgelist = "../data/call.txt";


        // specify file header to output
        String output = "../data/FluMotifWithNeighbour.txt";
        String outputIn = "../data/FluMotifWithNeighbour_In.txt";
        String outputOut = "../data/FluMotifWithNeighbour_Out.txt";
        String outputMut = "../data/FluMotifWithNeighbour_Mutual.txt";

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
            if (tempCount % 10000 == 0) System.out.printf("-");
        }
        System.out.println("Start counting neighbour motif for each node");
        tempCount = 0;
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).motifCount_neighbour(fullData.allMotif);
            tempCount++;
            if (tempCount % 10000 == 0) System.out.printf("-");
        }

        tempCount = 0;
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).motifCount_neighbour(fullData.allMotif);
            tempCount++;
            if (tempCount % 10000 == 0) System.out.printf("+");
        }

        // output to file
        BufferedWriter sc = new BufferedWriter(new FileWriter(output));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc, 121, 1);
        }
        sc.close();

        // output to file
        BufferedWriter sc1 = new BufferedWriter(new FileWriter(outputIn));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc1, 121, 2);
        }
        sc1.close();

        // output to file
        BufferedWriter sc2 = new BufferedWriter(new FileWriter(outputOut));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc2, 121, 3);
        }
        sc2.close();

        // output to file and remove motif counts
        BufferedWriter sc3 = new BufferedWriter(new FileWriter(outputMut));
        for (int j : fullData.dict.values()) {
            if (fullData.allMotif.nodes.get(j) == null) {
                continue;
            }
            fullData.allMotif.nodes.get(j).printTo(sc3, 121, 4);
            // wipe out fulldata.allMotif
            fullData.allMotif.nodes.get(j).swipe();
        }
        sc3.close();

    }

}
