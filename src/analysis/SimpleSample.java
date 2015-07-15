package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data.DocumentData;
import data.Info;

public class SimpleSample {
    public static void main(String[] args) throws Exception {
        HashMap<Long, HashMap<Long, Info>> data = new HashMap<Long, HashMap<Long, Info>>();
        String[] fileprefix = {"0701"};//, "0702" , "0703", "0704", "0705", "0706"};//,
        //	  	, "0707", "0708",
        //		"0709", "0710", "0711", "0712", "0801", "0802","0803" };
        int countEdge = 0;
        for (int i = 0; i < fileprefix.length; i++) {
            String calldata = "/data/rwanda_anon/CDR/" + fileprefix[i] + "-Call.pai.sordate.txt";
            //String calldata = "/Dropbox/calltest.txt" + fileprefix[i];
            // load and parse data into triples
            // note here we for now does not consider phone conversation length...
            // 			since adding length the time is no longer sorted.
            //		It could easily be switched by first sorting the data by time + duration
            boolean direct = true;
            countEdge += DocumentData.ReadInDataMulti(calldata, data, false, 50000, direct);
            System.out.println("Finish reading" + fileprefix[i]);
        }
        System.out.printf("Total Edges: %d \n", countEdge);
        int numEdges = 4000;
        int maxNodes = 1000;
        double odds = (numEdges + 0.0) / (countEdge + 0.0);

        ArrayList<Long> nodeSample = new ArrayList<Long>();
        int sampEdges = 0;
        int sampNodes = 0;
        for (Map.Entry<Long, HashMap<Long, Info>> node : data.entrySet()) {
            double u = Math.random();
            if (u <= (odds * node.getValue().size())) {
                nodeSample.add(node.getKey());
                sampEdges += node.getValue().size();
                sampNodes += 1;
                if (sampEdges >= numEdges | sampNodes >= maxNodes) {
                    System.out.printf("Sampled %d Edges, %d Nodes \n", sampEdges, sampNodes);
                    break;
                }
            }
        }
        // perform final update on the to-connect nodes and output
        File output = new File("/data/rwanda_anon/richardli/sample0701-0706.txt");
        BufferedWriter wsamp = new BufferedWriter(new FileWriter(output));
        for (long senderID : nodeSample) {
            HashMap<Long, Info> edges = data.get(senderID);
            for (Map.Entry<Long, Info> edge : edges.entrySet()) {
                edge.getValue().print(senderID, edge.getKey(), wsamp);
            }
        }
        wsamp.close();

        System.out.println("finished printing output");
    }
}
