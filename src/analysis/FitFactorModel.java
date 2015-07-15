package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import util.MathUtil;
import data.DocumentData;
import data.Triple;

/**
 * Not updated on July 14th, 2015
 *
 */

public class FitFactorModel {
    public static void main(String[] args) throws ParseException, IOException {

        HashMap<Long, NodePara> nodes = new HashMap<Long, NodePara>();
        String[] fileprefix = {"0701", "0702", "0703", "0704", "0705", "0706"};//,
        //	  	, "0707", "0708",
        //		"0709", "0710", "0711", "0712", "0801", "0802","0803" };

        int indexT = 0;
        for (int i = 0; i < fileprefix.length; i++) {
            String calldata = "/data/rwanda_anon/CDR/" + fileprefix[i] + "-Call.pai.sordate.txt";
            //String calldata = "/Dropbox/calltest.txt" + fileprefix[i];
            // load and parse data into triples
            // note here we for now does not consider phone conversation length...
            // 			since adding length the time is no longer sorted.
            //		It could easily be switched by first sorting the data by time + duration
            ArrayList<Triple> data = DocumentData.NaiveRead(calldata, false, 0);
            indexT = Parametrize(data, nodes, indexT);
            System.out.println("Finish reading" + fileprefix[i]);


            // perform final update on the to-connect nodes
            for (NodePara node : nodes.values()) {
                node.updateToConnectFinal(indexT);
            }

		 /*
		  * Note for output:
		  * \bar Y_{i . . } = outputS[i]/outputSall[i]
		  * \bar Y_{. j . } = outputR[j]/outputSall[j]
		  * \bar Y_{. . k } = outputT[k]/sum(outputSall)
		  * \bar Y_{i j . }[i, ] = outputSR[i, ] / outputSall[i]
		  * \bar Y_{i j . }[, j] = outputRS[j, ] / outputSall[i]
		  * \bar Y_{i . k } = outputST[i, k] / outputSdaily[i, k]
		  * \bar Y_{. j k } = outputRT[j, k] / outputSdaily[j, k] 
		  */
            File outputSall = new File("/data/rwanda_anon/richardli/outputSall" + fileprefix[i] + ".txt");
            File outputSdaily = new File("/data/rwanda_anon/richardli/outputSdaily" + fileprefix[i] + ".txt");

            File outputS = new File("/data/rwanda_anon/richardli/outputS" + fileprefix[i] + ".txt");
            File outputR = new File("/data/rwanda_anon/richardli/outputR" + fileprefix[i] + ".txt");
            File outputT = new File("/data/rwanda_anon/richardli/outputT" + fileprefix[i] + ".txt");
            File outputSR = new File("/data/rwanda_anon/richardli/outputSR" + fileprefix[i] + ".txt");
            File outputRS = new File("/data/rwanda_anon/richardli/outputRS" + fileprefix[i] + ".txt");
            File outputRT = new File("/data/rwanda_anon/richardli/outputRT" + fileprefix[i] + ".txt");
            File outputST = new File("/data/rwanda_anon/richardli/outputST" + fileprefix[i] + ".txt");
		 
		 		 
		 /*
		  * output sum variables
		  */
            BufferedWriter wSall = new BufferedWriter(new FileWriter(outputSall));
            for (NodePara node : nodes.values()) {
                wSall.write(Integer.toString(node.getAllPossible(indexT)) + "\n");
            }
            wSall.close();
            System.out.println("finished calculating sum variables");

            BufferedWriter wSdaily = new BufferedWriter(new FileWriter(outputSdaily));
            for (NodePara node : nodes.values()) {
                if (node.count1neiDaily.size() != node.count2neiDaily.size()) {
                    System.out.println(node.count1neiDaily.size());
                    System.out.println(node.count2neiDaily.size());
                    System.out.println(indexT + 1);
                }
                wSdaily.write(MathUtil.ArraySum(node.count2neiDaily, node.count1neiDaily).toString().replace("[", "").replace("]", "\n"));
            }
            wSdaily.close();
		 
		 /*
		  * output node-time factor
		  */
            BufferedWriter wS = new BufferedWriter(new FileWriter(outputS));
            BufferedWriter wR = new BufferedWriter(new FileWriter(outputR));
            BufferedWriter wT = new BufferedWriter(new FileWriter(outputT));
            BufferedWriter wST = new BufferedWriter(new FileWriter(outputST));
            BufferedWriter wRT = new BufferedWriter(new FileWriter(outputRT));
            int[] collapsed = new int[indexT + 1];
            int[] nodewiseS = new int[indexT + 1];
            int[] nodewiseR = new int[indexT + 1];
            int sumS, sumR;

            for (NodePara node : nodes.values()) {
                nodewiseS = node.sumNodebyTime(indexT, true);
                nodewiseR = node.sumNodebyTime(indexT, false);
                sumS = MathUtil.vectorSum(nodewiseS);
                sumR = MathUtil.vectorSum(nodewiseR);
                wS.write(Integer.toString(sumS) + "\n");
                wR.write(Integer.toString(sumR) + "\n");
                wST.write(Arrays.toString(nodewiseS).replace("[", "").replace("]", "\n"));
                wRT.write(Arrays.toString(nodewiseR).replace("[", "").replace("]", "\n"));
                MathUtil.StringAdd(collapsed, nodewiseS, nodewiseR);
            }
            wT.write(Arrays.toString(collapsed).replace("[", "").replace("]", "\n"));
            wS.close();
            wR.close();
            wT.close();
            wST.close();
            wRT.close();
            System.out.println("finished calculating node-time factor");
		 
	
		 /*
		  * output node-node factor
		  */
            BufferedWriter wSR = new BufferedWriter(new FileWriter(outputSR));
            for (NodePara node : nodes.values()) {
                ArrayList<Integer> newline = node.sumNodeByReceiver();
                wSR.write(newline.toString().replace("[", "").replace("]", "\n"));
            }
            wSR.close();
            System.out.println("finished calculating node-node factor by receiver");

            BufferedWriter wRS = new BufferedWriter(new FileWriter(outputRS));
            for (NodePara node : nodes.values()) {
                ArrayList<Integer> newline = node.sumNodeBySender();
                wRS.write(newline.toString().replace("[", "").replace("]", "\n"));
            }
            wRS.close();
            System.out.println("finished calculating node-node factor by sender");
        }

    }

    public static int Parametrize(ArrayList<Triple> data,
                                  HashMap<Long, NodePara> nodes,
                                  int indexT) {
        int countEdge = data.size();
        // iterate this set of data and store the statistics
        for (int itr = 0; itr < countEdge; itr++) {
            if (itr % 1000 == 0) System.out.printf(".");

            long tempS = data.get(itr).sender;
            long tempR = data.get(itr).receiver;

            // ignore self-loop
            if (tempS == tempR) continue;

            double tempT = data.get(itr).time;
            // get the index by day
            indexT = (int) (tempT / 24);
            //---------------------------------------------------//
			 /*
			  * Update sender information
			  */
            if (nodes.get(tempS) != null) {
                nodes.get(tempS).addasSender(tempR, indexT);
            } else {
                NodePara newNode = new NodePara(tempS);
                newNode.addasSender(tempR, indexT);
                nodes.put(tempS, newNode);
            }
            //---------------------------------------------------//
			 /*
			  * Update receiver information
			  */
            if (nodes.get(tempR) != null) {
                nodes.get(tempR).addasReceiver(tempS, indexT);
            } else {
                NodePara newNode = new NodePara(tempR);
                newNode.addasReceiver(tempS, indexT);
                nodes.put(tempR, newNode);
            }
            //---------------------------------------------------//
			/*
			 * Update to-connect information
			 */
            nodes.get(tempR).checkToConnect(nodes, nodes.get(tempS), indexT);
            nodes.get(tempS).checkToConnect(nodes, nodes.get(tempR), indexT);
        }
        return (indexT);

    }
}
