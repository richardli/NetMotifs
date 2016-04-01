package analysis;

import data.NodeMotifwithNeighbour;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

/**
 * Created by zehangli on 1/13/16.
 *
 * Get Motifs and some other statistics at a evey tSNAP (days) time interval
 * Specified shock time: tShock
 * Read two months of files
 * Output (Print) format in *_OtherStat.txt:
 *  1. ID
 *  2. TimePoint
 *  3-38. Network Size, type1-3 size, type1-3 ratio for (mutual, out, in, all, historical)
 *  39-41. in-shock-area?
 *  42. post-shock?
 */


public class NodeSampleEQ extends NodeSampleWeek{
    public static String shockTime0 = "080203|00:00:00";
    public static String shockTime1 = "080203|09:35:00";
    public static String shockTime2 = "080203|13:05:00";
    public static String shockTime3 = "080204|00:00:00";

    public static int[] towerAffected = new int[]{9, 20, 46, 47, 52, 74, 80, 125, 166, 172, 184, 241, 242};


    /** stream historical data **/
    /** this step does not handle outliers, instead, every node is saved to dictionary **/
    public void streamHistory(String[] files) throws IOException, ParseException {
        // parse time
        double time;
        double counter = Double.MIN_VALUE;
        String line;
        int nextindex = 0;

        // loop through files until end of time period is reached
        for (int i = 0; i < files.length; i++) {
            System.out.println("Read historical phone file: " + files[i]);
            BufferedReader br = new BufferedReader(new FileReader(files[i]));
            // read line
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = GlobalHelper.parseTime(field);
                    // parse ID
                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
                    // convert to Long ID
                    long s = Long.parseLong(sender);
                    long r = Long.parseLong(receiver);

                    if (this.dict.get(s) == null) {
                        this.dict.put(s, nextindex);
                        this.allMotif.nodes.put(nextindex, new NodeMotifwithNeighbour(nextindex));
                        nextindex++;
                    }
                    if (this.dict.get(r) == null) {
                        this.dict.put(r, nextindex);
                        this.allMotif.nodes.put(nextindex, new NodeMotifwithNeighbour(nextindex));
                        nextindex++;
                    }

                    // update neighborhood
                    int sid = this.dict.get(s);
                    int rid = this.dict.get(r);
                    this.allMotif.nodes.get(sid).hList.add(rid);
                    this.allMotif.nodes.get(rid).hList.add(sid);

                    // print a dot for each new day
                    if (time - counter > 24) {
                        System.out.printf(".");
                        counter = time;
                    }
                }
            }
            br.close();
        }
        this.allSize = nextindex;
    }

    /** TODO: check outliers for historical data and delete from hList **/
    public void checkOutlierHistory(){


    }


    /** copied from streamPhone method in NodeSampleWeek class **/
    /** also check to set type1, type2, type3 by location      **/
    public void streamPhone_checkEQ(String[] files, int max, String mmStart, String maxDate, int thre, String tEQ0,
                                    String tEQ1, String tEQ2,String tEQ3, int[] towers)
            throws
            ParseException, NumberFormatException, IOException {

        // parse time
        double startTime = GlobalHelper.parseDate(mmStart);
        double maxTime = GlobalHelper.parseDate(maxDate);

        double eq0 =  GlobalHelper.parseTime(tEQ0);
        double eq1 =  GlobalHelper.parseTime(tEQ1);
        double eq2 =  GlobalHelper.parseTime(tEQ2);
        double eq3 =  GlobalHelper.parseTime(tEQ3);
        HashSet<Integer> eqTower = new HashSet<Integer>();
        for(int i : towers){ eqTower.add(i); }

        double time;
        double counter = Double.MIN_VALUE;
        String line;
        System.out.println("Read phone from " + mmStart + " to " + maxDate);


        // loop through files until end of time period is reached
        for (int i = 0; i < files.length; i++) {
            BufferedReader br = new BufferedReader(new FileReader(files[i]));
            // read line
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = GlobalHelper.parseTime(field);
                    if (time < startTime) {
                        continue;
                    }
                    if (time >= maxTime) {
                        br.close();
                        return;
                    }


                    // parse ID
                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
                    // convert to Long ID
                    long s = Long.parseLong(sender);
                    long r = Long.parseLong(receiver);

                    // since the data has been stored already in outlier check,
                    // not in dictionary means they are outliers
                    if (this.dict.get(s) == null | this.dict.get(r) == null) {
                        continue;
                    }
                    // update neighborhood
                    int sid = this.dict.get(s);
                    int rid = this.dict.get(r);
                    if(this.allMotif.nodes.get(sid) == null | this.allMotif.nodes.get(rid) == null){
                        continue;
                    }
                    try {
                        this.allMotif.nodes.get(sid).sendto(rid);
                    }catch (UnsupportedOperationException e){
                        System.out.println(sid);
                        for(int tp :  this.allMotif.nodes.get(sid).sList){System.out.println(tp);}
                    }
                    this.allMotif.nodes.get(rid).recfrom(sid);

                    /********* check earthquake location  ********/
                    int Sloc1 = Integer.parseInt(field[5]);
                    int Sloc2 = Integer.parseInt(field[6]);
                    int Rloc1 = Integer.parseInt(field[7]);
                    int Rloc2 = Integer.parseInt(field[8]);

                    /** 00:00 - 09:35 type I          **/
                    /** 09:35 - 13:05 type I, II, III **/
                    /** 13:05 - 00:00 type III        **/
                    if(eqTower.contains(Sloc1) | eqTower.contains(Sloc2)) {
                        if (time >= eq0 & time < eq1) {
                            this.allMotif.nodes.get(sid).setType1(true);
                        } else if (time >= eq1 & time < eq2) {
                            this.allMotif.nodes.get(sid).setType1(true);
                            this.allMotif.nodes.get(sid).setType2(true);
                            this.allMotif.nodes.get(sid).setType3(true);
                        } else if (time >= eq2 & time < eq3) {
                            this.allMotif.nodes.get(sid).setType1(true);
                            this.allMotif.nodes.get(sid).setType3(true);

                        }
                    }

                    if(eqTower.contains(Rloc1) | eqTower.contains(Rloc2)) {
                        if (time >= eq0 & time < eq1) {
                            this.allMotif.nodes.get(rid).setType1(true);
                        } else if (time >= eq1 & time < eq2) {
                            this.allMotif.nodes.get(rid).setType1(true);
                            this.allMotif.nodes.get(rid).setType2(true);
                            this.allMotif.nodes.get(rid).setType3(true);
                        } else if (time >= eq2 & time < eq3) {
                            this.allMotif.nodes.get(rid).setType1(true);
                            this.allMotif.nodes.get(rid).setType3(true);

                        }
                    }
                    /********* check earthquake location finished  ********/

                    // print a dot for each new day
                    if (time - counter > 24) {
                        System.out.printf(".");
                        counter = time;
                    }
                }
            }
            br.close();
        }


        return;
    }
    public static void main(String[] args) throws IOException, ParseException {

        //whether or not to count motifs?
        boolean countMotif = false;

        // specify files to read
        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON-new.all.txt";
        // specify date to avoid changing too much
        String fileDates[] = new String[2];
        fileDates[0] = "0801";  // 0705, 0805, 0809
        fileDates[1] = "0802";  // 0706, 0806, 0810
        String endDate = "0803"; //  0707, 0807, 0811

        String phonefile[] = new String[2];
        phonefile[0] = "/data/rwanda_anon/CDR/" + fileDates[0] + "-Call.pai.sordate.txt";
        phonefile[1] = "/data/rwanda_anon/CDR/" + fileDates[1] + "-Call.pai.sordate.txt";


        // specify file header to output
        String outputHeaderOther = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "Statday";
        String outputHeader = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "day";
        String outputHeaderIn = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "NeighbourIn_day";
        String outputHeaderOut = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] + "NeighbourOut_day";
        String outputHeaderMut = "/data/rwanda_anon/richardli/MotifwithNeighbour/" + fileDates[0] +
                "NeighbourMutual_day";

        // parse start and end time
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long t1 = format.parse(fileDates[0] + "01|00:00:00").getTime();
        long t2 = format.parse(endDate + "01|00:00:00").getTime();
        int tSNAP = 1;

        // set calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse(fileDates[0] + "01|00:00:00"));

        // count number of hours
        int nday = ((int) ((t2 - t1) / 1000 / (3600) / 24));
        System.out.printf("%d days in the period\n", nday);
        int nPeriod = (int) (nday / (tSNAP+0.0));
        System.out.printf("%d snapshots to take in the period\n", nPeriod);

        // initialize mm file reader outside the loop
        NodeSampleEQ fullData = new NodeSampleEQ();

        /*******************************************************************************/
        /** read data for historical period **/
        String[] histDates = new String[]{"0701", "0702", "0703", "0704", "0705", "0706",
                                         "0707", "0708", "0709", "0710", "0711","0712"};
        String[] files = new String[histDates.length];
        for(int i = 0; i < histDates.length; i++){
            files[i] = "/data/rwanda_anon/CDR/" + histDates[i] + "-Call.pai.sordate.txt";
        }
        fullData.streamHistory(files);
        /*******************************************************************************/

        // read data for snapshot period
        for (int i = 0; i < nPeriod; i++) {

            String output = outputHeader + i + ".txt";
            String outputIn = outputHeaderIn + i + ".txt";
            String outputOut = outputHeaderOut + i + ".txt";
            String outputMut = outputHeaderMut + i + ".txt";

            /*******************************************************************************/
            /** Get Data in Order **/
            // define phoneEnd as the time when we consider as future MM sign-up
            // define MMEnd as the max time in the future we are looking at
            // set phone start date to be current calendar date, and move forward a period
            String phoneStart = format.format(cal.getTime()).substring(0, 6);
            // set phone end date as current calendar date, and move forward a priod
            cal.add(Calendar.DATE, tSNAP);
            String phoneEnd = format.format(cal.getTime()).substring(0, 6);
            // set MM end date as current calendar date
            cal.add(Calendar.DATE, tSNAP);
            String MMEnd = format.format(cal.getTime()).substring(0, 6);
            System.out.print("Checking status of sign-up from " + phoneEnd + " to " + MMEnd + "\n");
            // reset calendar to previous period again
            cal.add(Calendar.DATE, tSNAP * (-1));
            /*******************************************************************************/
            /** Read, check outlier, and read again **/
            // read MM file and update full data --> update outcome in the next day
            fullData.streamMM(mmfile, Integer.MAX_VALUE, phoneStart, phoneEnd, MMEnd);
            System.out.print("Checking status of sign-up done\n");
            // set parameter, hard threshold and independent sampling
            int hardThre = -1; // setting hardThre to -1, only check for outlier by quantile
            boolean indep = false;
            // check outlier
            fullData.checkOutlier(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, 1000, 0.99, hardThre, indep);
            // stream phone data  --> update who called who if they are not outliers
            /** also check the location and time for each node's appearance in EQ**/
            fullData.streamPhone_checkEQ(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, hardThre,
                    shockTime0, shockTime1, shockTime2, shockTime3, towerAffected);
            // get all data without sampling
            fullData.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
            System.out.println("Sample of nodes in the sample now:        " + fullData.sample.size());
            /*******************************************************************************/



            for (int j : fullData.allMotif.nodes.keySet()) {
                fullData.allMotif.nodes.get(j).organize();
            }

            // output to file
            int post_shock = (Integer.parseInt(phoneStart) >= Integer.parseInt("080203"))? 1:0;
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputHeaderOther + i + ".txt"));

            writer.write("ID date " +
                         "mN mN1 mN2 mN3 mR1 mR2 mR3 " +
                         "sN sN1 sN2 sN3 sR1 sR2 sR3 " +
                         "rN rN1 rN2 rN3 rR1 rR2 rR3 " +
                         "aN aN1 aN2 aN3 aR1 aR2 aR3 " +
                         "hN hN1 hN2 hN3 hR1 hR2 hR3 " +
                         "type1 type2 type3 postShock MM\n");
            for (int j : fullData.allMotif.nodes.keySet()){
                double[] counts = fullData.allMotif.nodes.get(j).TypeAnalysis(fullData.allMotif);
                int type1 = (fullData.allMotif.nodes.get(j).type1)? 1:0;
                int type2 = (fullData.allMotif.nodes.get(j).type2)? 1:0;
                int type3 = (fullData.allMotif.nodes.get(j).type3)? 1:0;
                StringBuilder sb = new StringBuilder();
                sb.append(j).append(" ");
                sb.append(phoneStart).append(" ");
                for(int jj = 0; jj < counts.length; jj++){
                    sb.append(counts[jj]).append(" ");
                }
                sb.append(type1).append(" ");
                sb.append(type2).append(" ");
                sb.append(type3).append(" ");
                sb.append(post_shock).append(" ");
                sb.append(fullData.allMotif.nodes.get(j).y).append("\n");
                writer.write(sb.toString());
                fullData.allMotif.nodes.get(j).swipe();
            }
            writer.close();


            if(countMotif) {
                /************ Motif count for each node locally ************/
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

                /************ Motif count for each node's neighbour ************/
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
    }

}
