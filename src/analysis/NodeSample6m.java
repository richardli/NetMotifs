package analysis;

import data.NodeMotif;
import data.NodeMotifwithColorNeighbour;
import util.MotifOrder;
import util.VectorUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zehangli on 1/25/17.
 */
public class NodeSample6m extends NodeSampleWeekNeighbour{


    public static void main(String[] args) throws IOException, ParseException {

        // specify files to read
        System.out.println("Generating Motif files for colored network with neighbours");
        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON-new.all.txt";
        // specify date to avoid changing too much
        int nfile = 6;
        String fileDates[][] = new String[2][nfile];
        fileDates[0] = new String[]{"0701", "0702", "0703", "0704", "0705", "0706"};
        fileDates[1] = new String[]{"0707", "0708", "0709", "0710", "0711", "0712"};

        boolean first_half = Integer.parseInt(args[0]) == 0701;
        // set the end date and which set of data to read
        String endDate = "0707";
        int set_to_read_phone = 0;
        if(!first_half){
            endDate = "0801";
            set_to_read_phone = 1;
        }

        String phonefile[][] = new String[2][nfile];
        for(int i = 0; i < nfile; i++){
            phonefile[0][i] = "/data/rwanda_anon/CDR/" + fileDates[0][i] + "-Call.pai.sordate.txt";
            phonefile[1][i] = "/data/rwanda_anon/CDR/" + fileDates[1][i] + "-Call.pai.sordate.txt";
        }

        // specify file header to output
        String outputHeader0 = "/data/rwanda_anon/richardli/MotifwithNeighbour/6mBasic_" + endDate;
        String outputHeader = "/data/rwanda_anon/richardli/MotifwithNeighbour/6m" + endDate;
        String outputHeaderYes = "/data/rwanda_anon/richardli/MotifwithNeighbour/6mYes_" + endDate;
        String outputHeaderNo = "/data/rwanda_anon/richardli/MotifwithNeighbour/6mNo_" + endDate;

        // parse start and end time
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long t1 = format.parse(fileDates[set_to_read_phone][0] + "01|00:00:00").getTime();
        long t2 = format.parse(endDate + "01|00:00:00").getTime();

        // set calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(fileDates[set_to_read_phone][0] + "01|00:00:00"));

        // count number of days
        int period = ((int) ((t2 - t1) / 1000 / (3600) / 24));
        System.out.printf("%d days in the period\n", period);


        // initialize mm file reader outside the loop
        NodeSample6m fullData = new NodeSample6m();
        String outputbasics = outputHeader0 + ".txt";
        String output = outputHeader + ".txt";
        String outputYes = outputHeaderYes + ".txt";
        String outputNo = outputHeaderNo  + ".txt";

            /**
             * put sender information into dictionary
             *
             *  ------------------ | phoneStart| ------------ | phoneEnd | ----------------- | MMEnd |
             *      Y = -1                         Y = -1                   Y = 1
             *    label = 1                        label = 1                 label = 0
             *
             * First Pass: (streamMM)
             * ---------------- Check MM status  ----------------- | ------ Check Signup -------- |
             * Second Pass: (checkOutlier)
             *                          |---- Remove outliers ---- |
             * Second Pass: (streamPhone)
             *                          |---- Gather Graph ------- |
             *                                              count motif_test
             *
             **/
            // define phoneEnd as the time when we consider as future MM sign-up
            // define MMEnd as the max time in the future we are looking at

            // set phone start date to be current calendar date, and move forward a period
//            System.out.println( format.format(cal.getTime()) );
            String phoneStart = format.format(cal.getTime()).substring(0, 6);

            // set phone end date as current calendar date, and move forward a priod
            cal.add(Calendar.DATE, period);
            String phoneEnd = format.format(cal.getTime()).substring(0, 6);

            // set MM end date as current calendar date
            cal.add(Calendar.DATE, period);
            String MMEnd = format.format(cal.getTime()).substring(0, 6);
            System.out.print("Checking status of sign-up from " + phoneEnd + " to " + MMEnd + "\n");

            // reset calendar to previous period again
            cal.add(Calendar.DATE, period * (-1));

            // read MM file and update full data
            fullData.streamMM(mmfile, Integer.MAX_VALUE, phoneStart, phoneEnd, MMEnd);
            System.out.print("Checking status of sign-up done\n");

            // set parameter, hard threshold and independent sampling
            int hardThre = 50;
            boolean indep = false;

            // check outlier  // TODO: check if this time span is right
            fullData.checkOutlier(phonefile[set_to_read_phone], Integer.MAX_VALUE, phoneStart, phoneEnd, 1000, 0.9, hardThre, indep);
            // stream phone data  // TODO: check if this time span is right
            fullData.streamPhone(phonefile[set_to_read_phone], Integer.MAX_VALUE, phoneStart, phoneEnd, hardThre);

            // get all data without sampling
            fullData.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
            System.out.println("Sample of nodes in the sample now:        " + fullData.sample.size());


            for (int j : fullData.allMotif.nodes.keySet()) {
                fullData.allMotif.nodes.get(j).organize();
            }

            // count motifs for each node themselves
            // without sample, output all nodes
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
            System.out.println("Start counting neighbour motif for each node with label");
            tempCount = 0;
            for (int j : fullData.dict.values()) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).motifCount_neighbour(fullData.allMotif);
                tempCount++;
                if (tempCount % 10000 == 0) System.out.printf("-");
            }

            System.out.println("Start changing motif back to final version");
            tempCount = 0;
            for(int j : fullData.dict.values()){
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                MotifOrder.changeOrder2(fullData.allMotif.nodes.get(j).motif);
                MotifOrder.changeOrderDouble2(fullData.allMotif.nodes.get(j).motif_from_no);
                MotifOrder.changeOrderDouble2(fullData.allMotif.nodes.get(j).motif_from_yes);
                tempCount++;
                if (tempCount % 10000 == 0) System.out.printf("-");
            }


            // output to file (simple summary stats)
            BufferedWriter sc0 = new BufferedWriter(new FileWriter(outputbasics));
            for (int j : fullData.dict.values()) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).printTo(sc0, 120, 2, true);
            }
            sc0.close();

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
