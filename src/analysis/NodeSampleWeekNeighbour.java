package analysis;

import data.NodeMotif;
import data.NodeMotifHashMap;
import data.NodeMotifwithNeighbour;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by zehangli on 12/13/15.
 */
public class NodeSampleWeekNeighbour extends NodeSampleWeek {
    /*
     * extends the motif counting class to also return neighbour motif sums
     */
    public NodeMotifHashMap allMotif = new NodeMotifHashMap();

    public static void main(String[] args) throws IOException, ParseException {

//				/*
//				 * for testing purpose...
//				 */
//						String mmfile = "/Users/zehangli/data/rwanda_anon/CDR/0701-motif-Call.pai.sordate2.txt";
//						String phonefile = "/Users/zehangli/data/rwanda_anon/CDR/0701-motif-Call.pai.sordate.txt";
//						NodeSample fulldata = new NodeSample();
//						// read MM file and update full data
//						fulldata.initMM(mmfile, Integer.MAX_VALUE, "070101", "080101");
//						fulldata.sampleNode(Integer.MAX_VALUE, 1);
//						fulldata.checkOutlier(phonefile, Integer.MAX_VALUE, "080101", 1000, 1.0);
//						fulldata.readPhone(phonefile, Integer.MAX_VALUE, "080101");
//						fulldata.sampleNode(Integer.MAX_VALUE, 0);
//
//						for(int i : fulldata.allMotif.keySet()){
//							fulldata.allMotif.get(i).organize();
//						}
//
//						for(int i : fulldata.sample){
//							fulldata.allMotif.get(i).motifCount_wlabel(fulldata.allMotif);
//							System.out.println(fulldata.allMotif.get(i).id);
//							System.out.println(fulldata.allMotif.get(i).label);
//							System.out.println(fulldata.allMotif.get(i).y);
//							System.out.println(fulldata.allMotif.get(i).motif.toString());
//						}


        // specify files to read
        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON-new.all.txt";
        // specify date to avoid changing too much
        String fileDates[] = new String[2];
        fileDates[0] = "0705";  // 0705, 0805, 0809
        fileDates[1] = "0706";  // 0706, 0806, 0810
        String endDate = "0707"; //  0707, 0807, 0811

        String phonefile[] = new String[2];
        phonefile[0] = "/data/rwanda_anon/CDR/" + fileDates[0] + "-Call.pai.sordate.txt";
        phonefile[1] = "/data/rwanda_anon/CDR/" + fileDates[1] + "-Call.pai.sordate.txt";


        // specify file header to output
        String outputHeader = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "week";
        String outputHeaderIn = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "NeighbourIn_week";
        String outputHeaderOut = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "NeighbourOut_week";
        String outputHeaderMut = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] +
                "NeighbourMutual_week";

        // parse start and end time
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long t1 = format.parse(fileDates[0] + "01|00:00:00").getTime();
        long t2 = format.parse(endDate + "01|00:00:00").getTime();
        int period = 7;

        // set calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(fileDates[0] + "01|00:00:00"));

        // count number of days
        int nday = ((int) ((t2 - t1) / 1000 / (3600) / 24));
        System.out.printf("%d days in the period\n", nday);
        int nPeriod = (int) (nday / (period + 0.0));
        System.out.printf("%d periods in the period\n", nPeriod);

        // initialize mm file reader outside the loop
        NodeSampleWeekNeighbour fullData = new NodeSampleWeekNeighbour();

        for (int i = 0; i < nPeriod; i++) {

            String output = outputHeader + i + ".txt";
            String outputIn = outputHeaderIn + i + ".txt";
            String outputOut = outputHeaderOut + i + ".txt";
            String outputMut = outputHeaderMut + i + ".txt";

            // define phoneEnd as the time when we consider as future MM sign-up
            // define MMEnd as the max time in the future we are looking at

            // set phone start date to be current calendar date, and move forward a period
            String phoneStart = format.format(cal.getTime()).substring(0, period - 1);

            // set phone end date as current calendar date, and move forward a priod
            cal.add(Calendar.DATE, period);
            String phoneEnd = format.format(cal.getTime()).substring(0, period - 1);

            // set MM end date as current calendar date
            cal.add(Calendar.DATE, period);
            String MMEnd = format.format(cal.getTime()).substring(0, period - 1);
            System.out.print("Checking status of sign-up from " + phoneEnd + " to " + MMEnd + "\n");

            // reset calendar to previous period again
            cal.add(Calendar.DATE, period * (-1));

            // read MM file and update full data
            fullData.streamMM(mmfile, Integer.MAX_VALUE, phoneStart, phoneEnd, MMEnd);
            System.out.print("Checking status of sign-up done\n");

            // set parameter, hard threshold and independent sampling
            int hardThre = 2;
            boolean indep = false;

            // check outlier  // TODO: outliers now are checked for each period, maybe better to check once for all
            fullData.checkOutlier(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, 1000, 0.99, hardThre, indep);
            // stream phone data  //TODO: phone files always starts with the first one, going through previous dates is a waste
            fullData.streamPhone(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, hardThre);

            // get all data without sampling
            fullData.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
            System.out.println("Sample of nodes in the sample now:        " + fullData.sample.size());

            // sample Y = 1 nodes
            //     fullData.sampleNode(Integer.MAX_VALUE, 1, indep);
            //     System.out.println("Sample of nodes signed up in this period: " + fullData.sample.size());
            // sample Y = 0 nodes
            //     fullData.sampleNode(Integer.MAX_VALUE, 0, indep);

            // get all the data organized. Note this is necessary as those not in the sample could be reached by friendship map
            for (int j : fullData.allMotif.nodes.keySet()) {
                fullData.allMotif.nodes.get(j).organize();
            }

            // count motifs
            int tempCount = 0;
            for (int j : fullData.sample) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).motifCount_neighbour(fullData.allMotif);
                tempCount++;
                if (tempCount % 10000 == 0) System.out.printf("-");

            }

            // output to file
            BufferedWriter sc = new BufferedWriter(new FileWriter(output));
            for (int j : fullData.sample) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).printTo(sc, 121, 1);
            }
            sc.close();

            // output to file
            BufferedWriter sc1 = new BufferedWriter(new FileWriter(outputIn));
            for (int j : fullData.sample) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).printTo(sc1, 121, 2);
            }
            sc1.close();

            // output to file
            BufferedWriter sc2 = new BufferedWriter(new FileWriter(outputOut));
            for (int j : fullData.sample) {
                if (fullData.allMotif.nodes.get(j) == null) {
                    continue;
                }
                fullData.allMotif.nodes.get(j).printTo(sc2, 121, 3);
            }
            sc2.close();

            // output to file and remove motif counts
            BufferedWriter sc3 = new BufferedWriter(new FileWriter(outputMut));
            for (int j : fullData.sample) {
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
}
