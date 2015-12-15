package analysis;

/**
 * Created by zehangli on 8/7/15.
 */

import com.google.common.collect.Sets;
import data.NodeMotif;
import util.VectorUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.common.collect.Sets;
import data.NodeMotif;
import util.VectorUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Count Motifs before Earthquake Feb 3 2008, motif defined by the location of nodes
 *
 *  MM  status: user before Feb 03, 2008; new user within Feb 03 -
 * files read: Aug 2007 - Jan 2008 phone record
 *
 *  Note: outliers are checked for each period individually, as outlier IDs could be re-entered in dict
 *
 * Updated by zehangli 07/14/15
 */

public class NodeSampleEQ {
//    // dictionary of IDs
//    public HashMap<Long, Integer> dict = new HashMap<Long, Integer>();
//    // initialize size of nodes
//    int allSize = 0;
//    // a set of integers as sample
//    public HashSet<Integer> sample = new HashSet<Integer>();
//    // HashMap of NodeMotifs
//    public HashMap<Integer, NodeMotif> allMotif = new HashMap<Integer, NodeMotif>();
//
//
//    // initialize with MM file
//    // for example, if we want to count motif from 0701 to 0706, see results during 0707 to 0712
//    // sign up before 070101: Y = -1, label = 1 (might need more Y values later for this)
//    // sign up 070101 - 070701: Y = -1, label = 1
//    // sign up 070101 - 080101: Y = 1, label = 0;
//    // sign up after 080101: ignore
//    //
//    // so MMstart = 070701
//    //    PhoneEnd = 070707
//    //	  MMend = 080101
//
//    /**
//     * Read Mobile money data
//     *
//     * @param file file name
//     * @param max maximum number of records read
//     * @param phoneStart  Start date in string; in weekly case, day 0
//     * @param PhoneEnd Middle date in string; in weekly case, day 7
//     * @param maxDate  End date in string; in weekly case, day 14
//     * @throws IOException
//     * @throws ParseException
//     */
//    public void streamMM(String file, int max, String phoneStart, String PhoneEnd, String maxDate) throws IOException, ParseException {
//        int nextindex = this.allSize;
//        // time starting reading Phone
//        double startTime = GlobalHelper.parseDate(phoneStart);
//        // time starting checking MM sign-up
//        double midTime = GlobalHelper.parseDate(PhoneEnd);
//        // time stop reading data
//        double maxTime = GlobalHelper.parseDate(maxDate);
//        double time = 0.0;
//
//        String line;
//        BufferedReader br = new BufferedReader(new FileReader(file));
//
//        // start reading file
//        while ((line = br.readLine()) != null) {
//
//            // each lines look like: "L45485508|L10822145|080703|09:40:55|20|170|172"
//            // each block: 0-sender, 1-receiver, 3-date, 4-time, 5-amount, 6-senderTower, 7-receiverTower
//
//            String[] field = line.split("\\|");
//            if (field[4].charAt(0) != '-') {
//
//                // parse time
//                time = GlobalHelper.parseTime(field);
//
//                // check if maximum time or number of records is reached
//                //       if time before starting time, still need to proceed
//                if (time >= maxTime | nextindex > max) {
//                    break;
//                }
//
//                // parse sender into Long ID
//                String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                long s = Long.parseLong(sender);
//
//                // no need to parse receiver ID here, since only sending MM is considered here
//
//                /**
//                 * put sender information into dictionary
//                 * Y     : the outcome going into regression
//                 * label : MM user or not (currently the same as Y)
//                 *      sign up before startTime: Y = -1, label = 1
//                 *      sign up startTime to midTime: Y = -1, label = 1 (TODO: need to handle when started)
//                 *      sign up midTime to maxTime: Y = 1, label = 0
//                 *      sign up after maxTime: ignore (not reaching here)
//                 */
//                if (this.dict.get(s) == null) {
//                    this.dict.put(s, nextindex);
//                    if (time < startTime) {
//                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
//                    } else if (time < midTime) {
//                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
//                    } else {
//                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, 1, 0));
//                    }
//                    nextindex++;
//                } else {
//                    // if node already in the file, update label and y
//                    int index = this.dict.get(s);
//
//                    if (time < startTime) {
//                        this.allMotif.get(index).y = -1;
//                        this.allMotif.get(index).label = 1;
//
//                    } else if (time < midTime) {
//                        this.allMotif.get(index).y = -1;
//                        this.allMotif.get(index).label = 1;
//
//                        // extra case when streaming:
//                        // if this node signed up before this period, but no transfer sent in first week
//                    } else if (this.allMotif.get(index).y == 1 & this.allMotif.get(index).t < midTime){
//                        this.allMotif.get(index).label = 1;
//                        this.allMotif.get(index).y = -1;
//                    } else{
//                        this.allMotif.get(index).label = 0;
//                        this.allMotif.get(index).y = 1;
//                    }
//
//                }
//            }
//        }
//        this.allSize = nextindex;
//        br.close();
//        System.out.println("Finish reading MM files for current period");
//        System.out.println("Number of nodes now: " + nextindex);
//        System.out.println("Last transaction time: " + time);
//    }
//
//
//    /***
//     * Read phone files
//     *
//     * @param files array of file lists
//     * @param max maximum number of records (not used)
//     * @param mmStart time start counting MM
//     * @param maxDate maximum date to read
//     * @param thre threshold (not used)
//     * @throws ParseException
//     * @throws NumberFormatException
//     * @throws IOException
//     */
//    public void streamPhone(String[] files, int max, String mmStart, String maxDate, int thre) throws ParseException, NumberFormatException, IOException {
//
//        // parse time
//        double startTime = GlobalHelper.parseDate(mmStart);
//        double maxTime = GlobalHelper.parseDate(maxDate);
//        double time;
//        double counter = Double.MIN_VALUE;
//        String line;
//        System.out.println("Read phone from " + mmStart + "to" + maxDate);
//
//        // loop through files until end of time period is reached
//        for (int i = 0; i < files.length; i++) {
//            BufferedReader br = new BufferedReader(new FileReader(files[i]));
//            // read line
//            while ((line = br.readLine()) != null) {
//                String[] field = line.split("\\|");
//                if (field[4].charAt(0) != '-') {
//                    time = GlobalHelper.parseTime(field);
//                    if (time < startTime) {
//                        continue;
//                    }
//                    if (time >= maxTime) {
//                        br.close();
//                        return;
//                    }
//
//                    // parse ID
//                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
//                    // convert to Long ID
//                    long s = Long.parseLong(sender);
//                    long r = Long.parseLong(receiver);
//
//                    // since the data has been stored already in outlier check,
//                    // not in dictionary means they are outliers
//                    if (this.dict.get(s) == null | this.dict.get(r) == null) {
//                        continue;
//                    }
//
//                    // update neighborhood
//                    int sid = this.dict.get(s);
//                    int rid = this.dict.get(r);
//                    this.allMotif.get(sid).sendto(rid);
//                    this.allMotif.get(rid).recfrom(sid);
//
//                    // print a dot for each new day
//                    if (time - counter > 24) {
//                        System.out.printf(".");
//                        counter = time;
//                    }
//                }
//            }
//            br.close();
//        }
//        return;
//
//        //	comment out for now, not sure what is going on here...
//        //			Iterator<Map.Entry<Long,Integer>> iter2 = this.dict.entrySet().iterator();
//        //			int countremove = 0;
//        //			while(iter2.hasNext()){
//        //				Map.Entry<Long,Integer> entry = iter2.next();
//        //				int id = entry.getValue();
//        //				NodeMotif temp = this.allMotif.get(id);
//        //				temp.thinFreq(threa);
//        //				if(temp.sList.size() + temp.rList.size() == 0){
//        //					iter2.remove();
//        //					this.allMotif.remove(id);
//        //					countremove ++;
//        //					continue;
//        //				}
//        //			}
//        //			System.out.println("Number of nodes deleted again: " + countremove);
//    }
//
//    /**
//     * Calculate min threshold of the frequencies under normal assumption
//     * @param raw ArrayList of call frequencies
//     * @return mean - sd * 3
//     */
//    public double freqProcess(ArrayList<Integer> raw) {
//
//        double mean = 0.0;
//        double mean2 = 0.0;
//        int count = 0;
//        for (int freq : raw) {
//            mean += freq;
//            mean2 += freq * freq;
//            count += 1;
//        }
//        mean = mean / (count + 0.0);
//        mean2 = mean2 / (count + 0.0);
//        double sd = Math.sqrt(mean2 - mean * mean);
//        double result = mean - sd * 3;
//        System.out.println("Mean: " + mean + "SD: " + sd);
//        return result;
//    }
//
//
//    /**
//     * Read file for first pass and delete outlier nodes from dictionary
//     *
//     * @param files array of file lists
//     * @param max maximum number of records (not used)
//     * @param phoneStart time start counting
//     * @param maxDate maximum date to read
//     * @param thre threshold for max one-directional communications
//     * @param per percentile to consider as outlier for indeg/outdeg/sum/ndeg
//     * @param hardThre integer, how many one-directions calls consider as outlier (without the other direction)
//     * @param indep Boolean, if true, don't count pairs if only one of them is in the dictionary (only count both in or both not)
//     * @throws ParseException
//     * @throws NumberFormatException
//     * @throws IOException
//     */
//    public void checkOutlier(String[] files, int max, String phoneStart, String maxDate, int thre, double per, int hardThre, boolean indep) throws ParseException, NumberFormatException, IOException {
//        int nextindex = this.allSize;
//        int beforeindex = nextindex;
//        double startTime = GlobalHelper.parseDate(phoneStart);
//        double maxTime = GlobalHelper.parseDate(maxDate);
//        double time;
//        double counter = startTime;
//        String line;
//        System.out.println("First-batch check outlier: phone from " + phoneStart + "to" + maxDate);
//
//        for (int i = 0; i < files.length; i++) {
//            String file = files[i];
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            while ((line = br.readLine()) != null) {
//                String[] field = line.split("\\|");
//
//                if (field[4].charAt(0) != '-') {
//                    time = GlobalHelper.parseTime(field);
//                    if (time < startTime) {
//                        continue;
//                    }
//                    if (time >= maxTime | nextindex > max) {
//                        break;
//                    }
//                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
//
//                    long s = Long.parseLong(sender);
//                    long r = Long.parseLong(receiver);
//                    /*
//                     * if independent, unless both are present or both are both not
//                     * otherwise ignore
//                     */
//                    if (indep) {
//                        if (this.dict.containsKey(s) != this.dict.containsKey(r)) {
//                            continue;
//                        }
//                    }
//
//                    if (this.dict.get(s) == null) {
//                        this.dict.put(s, nextindex);
//                        this.allMotif.put(nextindex, new NodeMotif(nextindex));
//                        nextindex++;
//                    }
//                    if (this.dict.get(r) == null) {
//                        this.dict.put(r, nextindex);
//                        this.allMotif.put(nextindex, new NodeMotif(nextindex));
//                        nextindex++;
//                    }
//                    int sid = this.dict.get(s);
//                    int rid = this.dict.get(r);
//
//                    // update neighbors
//                    this.allMotif.get(sid).sendto(rid);
//                    this.allMotif.get(rid).recfrom(sid);
//
//                    // update degree counts (frequency)
//                    this.allMotif.get(sid).outFreq++;
//                    this.allMotif.get(rid).inFreq++;
//                    // print a dot for each new day
//                    if (time - counter > 24) {
//                        System.out.printf(".");
//                        counter = time;
//                    }
//                }
//            }
//            this.allSize = nextindex;
//            br.close();
//        }
//        // calcualte quantiles of in-deg, out-deg and their sums
//        ArrayList<Integer> indegs = new ArrayList<Integer>();
//        ArrayList<Integer> outdegs = new ArrayList<Integer>();
//        ArrayList<Integer> alldegs = new ArrayList<Integer>();
//        ArrayList<Integer> infreqs = new ArrayList<Integer>();
//        ArrayList<Integer> outfreqs = new ArrayList<Integer>();
//        ArrayList<Integer> allfreqs = new ArrayList<Integer>();
//
//        for (int node : this.allMotif.keySet()) {
//            if (this.allMotif.get(node) == null) {
//                System.out.println("!");
//                continue;
//            }
//            // since allMotif contains ID from previous periods, might have empty motifs
//            if(this.allMotif.get(node).inFreq + this.allMotif.get(node).outFreq == 0){
//                continue;
//            }
//
//            indegs.add(this.allMotif.get(node).rList.size());
//            outdegs.add(this.allMotif.get(node).sList.size());
//            alldegs.add(this.allMotif.get(node).nList.size());
//
//            infreqs.add(this.allMotif.get(node).inFreq);
//            outfreqs.add(this.allMotif.get(node).outFreq);
//            allfreqs.add(this.allMotif.get(node).inFreq + this.allMotif.get(node).outFreq);
//
//            // test
////            System.out.println(this.allMotif.get(node).rList.size() + " " +
////                                this.allMotif.get(node).sList.size()+ " " +
////                                this.allMotif.get(node).nList.size()+ " " +
////                                this.allMotif.get(node).inFreq+ " " +
////                                this.allMotif.get(node).outFreq);
//        }
//        int indegQuantile = VectorUtil.percentile(indegs, per);
//        int outdegQuantile = VectorUtil.percentile(outdegs, per);
//        int alldegQuantile = VectorUtil.percentile(alldegs, per);
//        int infreqQuantile = VectorUtil.percentile(infreqs, per);
//        int outfreqQuantile = VectorUtil.percentile(outfreqs, per);
//        int allfreqQuantile = VectorUtil.percentile(allfreqs, per);
//
//
//        //update dictionary
//        Iterator<Map.Entry<Long, Integer>> iter = this.dict.entrySet().iterator();
//        int countRemove = 0;
//        while (iter.hasNext()) {
//            Map.Entry<Long, Integer> entry = iter.next();
//            int id = entry.getValue();
//            if (this.allMotif.get(id) == null) {
//                System.out.println("!");
//                continue;
//            }
//            NodeMotif temp = this.allMotif.get(id);
//            // remove nodes with one-direction only communication and too large
//            if (temp.inFreq + temp.outFreq > thre & temp.inFreq * temp.outFreq == 0) {
//                iter.remove();
//                this.allMotif.remove(id);
//                countRemove++;
//                continue;
//            }
//            // remove nodes with too many calls
//            if (temp.inFreq > infreqQuantile
//                    | temp.outFreq > outfreqQuantile
//                    | temp.inFreq + temp.outFreq > allfreqQuantile
//                    | temp.rList.size() > indegQuantile
//                    | temp.sList.size() > outdegQuantile
//                    | temp.nList.size() > alldegQuantile) {
//                iter.remove();
//                this.allMotif.remove(id);
//                countRemove++;
//                continue;
//            }
//            // thin frequencies by hard threshold, keeping only links stronger than it
//            temp.thinFreq(hardThre);
//            if (temp.sList.size() + temp.rList.size() == 0) {
//                iter.remove();
//                this.allMotif.remove(id);
//                countRemove++;
//                continue;
//            }
//
//            // rest nodes, remove all neighbour lists (since some of the nodes will be removed)
//            this.allMotif.get(id).reset();
//
//        }
//        System.out.println("Freq: " + infreqQuantile + " " + outfreqQuantile + " " + allfreqQuantile);
//        System.out.println("Deg: " + indegQuantile + " " + outdegQuantile + " " + alldegQuantile);
//
//        System.out.println("Finished deleting outlier, deleted:    " + countRemove);
//        System.out.println("Total new nodes read before deletion:  " + (nextindex - 1 - beforeindex));
//        System.out.println("Total nodes after deleting outlier:    " + this.dict.size());
//
//
//
//        /**
//         * following commented codes reformulate hard threshold by replacing with mean-3sd,
//         * sd usually too large and thus not used
//         */
//        //        // second layer of filter
//        //        double threFreq = 0.0;
//        //        ArrayList<Integer> tempFreq = new ArrayList<Integer>();
//        //        for (int node : this.allMotif.keySet()) {
//        //            if (this.allMotif.get(node).nListFreq != null) {
//        //                for (int i : this.allMotif.get(node).nListFreq.keySet()) {
//        //                    tempFreq.add(this.allMotif.get(node).nListFreq.get(i));
//        //                }
//        //            }
//        //        }
//        //        BufferedWriter brtemp = new BufferedWriter(new FileWriter("/data/rwanda_anon/richardli/freqs.txt"));
//        //        for (int i = 0; i < tempFreq.size(); i++) {
//        //            brtemp.write(tempFreq.get(i) + ",");
//        //        }
//        //        brtemp.close();
//        //        threFreq = this.freqProcess(tempFreq);
//        //
//        //        System.out.println("Mimimum number of communication between two nodes is " + threFreq);
//        //
//        //        // update dictionary the second time
//        //        if (threFreq > hardThre) hardThre = (int) threFreq;
//        //        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
//        //        countRemove = 0;
//        //        while (iter2.hasNext()) {
//        //            int id = iter2.next().getValue();
//        //            if (this.allMotif.get(id) == null) {
//        //                System.out.println("!");
//        //                continue;
//        //            }
//        //            NodeMotif temp = this.allMotif.get(id);
//        //            temp.thinFreq(hardThre);
//        //            if (temp.sList.size() + temp.rList.size() == 0) {
//        //                iter2.remove();
//        //                this.allMotif.remove(id);
//        //                countRemove++;
//        //                continue;
//        //            }
//        //            this.allMotif.get(id).reset();
//        //        }
//        //        System.out.println("Finished deleting outlier from thinning edges, deleted:    " + countRemove);
//    }
//
//    /**
//     * Sample node (could be done by outcome variable)
//     *
//     *  @param len how many samples to take (upper limit)
//     *  @param y sample only nodes with certain outcome; if no outcome-specific requirement, set y = Integer.MAX_Value
//     *  @param indep Boolean, if only sampling non-connected egos
//     */
//    public void sampleNode(int len, int y, boolean indep) {
//        Set<Integer> population = new HashSet<Integer>();
//        Set<Integer> nodes_appeared = new HashSet<Integer>();
//
//        if (y == Integer.MAX_VALUE) {
//            population.addAll(this.dict.values());
//        } else {
//            for (int i : this.allMotif.keySet()) {
//                if (allMotif.get(i).y == y) population.add(i);
//            }
//        }
//
//        if (population.size() == 0) {
//            return;
//        }
//
//        // simple random sample of IDs
//        Random random = new Random();
//        List<Integer> orderlist = new LinkedList<Integer>();
//
//        // perform independent sample if needed
//        if (indep) {
//            for (int i : population) {
//                if (nodes_appeared.contains(i) |
//                        Sets.intersection(nodes_appeared, this.allMotif.get(i).sList).size() > 0 |
//                        Sets.intersection(nodes_appeared, this.allMotif.get(i).rList).size() > 0) {
//                    continue;
//                } else {
//                    orderlist.add(i);
//                    nodes_appeared.add(i);
//                    nodes_appeared.addAll(this.allMotif.get(i).rList);
//                    nodes_appeared.addAll(this.allMotif.get(i).sList);
//                }
//            }
//        } else {
//            orderlist = new LinkedList<Integer>(population);
//        }
//
//        Collections.shuffle(orderlist, random);
//        if (orderlist.size() <= len) {
//            len = orderlist.size();
//        }
//        List<Integer> randlist = orderlist.subList(0, len);
//        Set<Integer> shuffle = new HashSet<Integer>(randlist);
//        this.sample.addAll(shuffle);
//    }
//
//    public static void main(String[] args) throws IOException, ParseException {
//
////				/*
////				 * for testing purpose...
////				 */
////						String mmfile = "/Users/zehangli/data/rwanda_anon/CDR/0701-motif-Call.pai.sordate2.txt";
////						String phonefile = "/Users/zehangli/data/rwanda_anon/CDR/0701-motif-Call.pai.sordate.txt";
////						NodeSample fulldata = new NodeSample();
////						// read MM file and update full data
////						fulldata.initMM(mmfile, Integer.MAX_VALUE, "070101", "080101");
////						fulldata.sampleNode(Integer.MAX_VALUE, 1);
////						fulldata.checkOutlier(phonefile, Integer.MAX_VALUE, "080101", 1000, 1.0);
////						fulldata.readPhone(phonefile, Integer.MAX_VALUE, "080101");
////						fulldata.sampleNode(Integer.MAX_VALUE, 0);
////
////						for(int i : fulldata.allMotif.keySet()){
////							fulldata.allMotif.get(i).organize();
////						}
////
////						for(int i : fulldata.sample){
////							fulldata.allMotif.get(i).motifCount_wlabel(fulldata.allMotif);
////							System.out.println(fulldata.allMotif.get(i).id);
////							System.out.println(fulldata.allMotif.get(i).label);
////							System.out.println(fulldata.allMotif.get(i).y);
////							System.out.println(fulldata.allMotif.get(i).motif.toString());
////						}
//
//
//        // specify files to read
//        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON-new.all.txt";
//        // specify date to avoid changing too much
//        String fileDates[] = new String[2];
//        fileDates[0] = "0705";  // 0705, 0805, 0809
//        fileDates[1] = "0706";  // 0706, 0806, 0810
//        String endDate = "0707"; //  0707, 0807, 0811
//
//        String phonefile[] = new String[2];
//        phonefile[0] = "/data/rwanda_anon/CDR/"+ fileDates[0] + "-Call.pai.sordate.txt";
//        phonefile[1] = "/data/rwanda_anon/CDR/"+ fileDates[1] + "-Call.pai.sordate.txt";
//
//
//        // specify file header to output
//        String outputHeader = "/data/rwanda_anon/richardli/" + fileDates[0] + "week";
//
//        // parse start and end time
//        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
//        long t1 = format.parse(fileDates[0] + "01|00:00:00").getTime();
//        long t2 = format.parse(endDate + "01|00:00:00").getTime();
//        int period = 7;
//
//        // set calendar
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(format.parse(fileDates[0] + "01|00:00:00"));
//
//        // count number of days
//        int nday = ((int) ((t2 - t1) / 1000 / (3600) / 24));
//        System.out.printf("%d days in the period\n", nday);
//        int nPeriod = (int) (nday / (period + 0.0));
//        System.out.printf("%d periods in the period\n", nPeriod);
//
//        // initialize mm file reader outside the loop
//        NodeSampleWeek fullData = new NodeSampleWeek();
//
//        for (int i = 0; i < nPeriod; i++) {
//
//            String output =  outputHeader + i + ".txt";
//
//            // define phoneEnd as the time when we consider as future MM sign-up
//            // define MMEnd as the max time in the future we are looking at
//
//            // set phone start date to be current calendar date, and move forward a period
//            String phoneStart = format.format(cal.getTime()).substring(0, period - 1);
//
//            // set phone end date as current calendar date, and move forward a priod
//            cal.add(Calendar.DATE, period);
//            String phoneEnd = format.format(cal.getTime()).substring(0, period - 1);
//
//            // set MM end date as current calendar date
//            cal.add(Calendar.DATE, period);
//            String MMEnd = format.format(cal.getTime()).substring(0, period - 1);
//            System.out.print("Checking status of sign-up from " + phoneEnd + " to " + MMEnd + "\n");
//
//            // reset calendar to previous period again
//            cal.add(Calendar.DATE, period * (-1));
//
//            // read MM file and update full data
//            fullData.streamMM(mmfile, Integer.MAX_VALUE, phoneStart, phoneEnd, MMEnd);
//            System.out.print("Checking status of sign-up done\n");
//
//            // set parameter, hard threshold and independent sampling
//            int hardThre = 2;
//            boolean indep = false;
//
//            // check outlier  // TODO: outliers now are checked for each period, maybe better to check once for all
//            fullData.checkOutlier(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, 1000, 0.99, hardThre, indep);
//            // stream phone data  //TODO: phone files always starts with the first one, going through previous dates is a waste
//            fullData.streamPhone(phonefile, Integer.MAX_VALUE, phoneStart, phoneEnd, hardThre);
//
//            // get all data without sampling
//            fullData.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
//            System.out.println("Sample of nodes in the sample now:        " + fullData.sample.size());
//
//            // sample Y = 1 nodes
//            //     fullData.sampleNode(Integer.MAX_VALUE, 1, indep);
//            //     System.out.println("Sample of nodes signed up in this period: " + fullData.sample.size());
//            // sample Y = 0 nodes
//            //     fullData.sampleNode(Integer.MAX_VALUE, 0, indep);
//
//            // get all the data organized. Note this is necessary as those not in the sample could be reached by friendship map
//            for (int j : fullData.allMotif.keySet()) {
//                fullData.allMotif.get(j).organize();
//            }
//
//            // count motifs
//            int tempCount = 0;
//            for (int j : fullData.sample) {
//                if (fullData.allMotif.get(j) == null) {
//                    continue;
//                }
//                fullData.allMotif.get(j).motifCount_wlabel(fullData.allMotif);
//                tempCount++;
//                if (tempCount % 10000 == 0) System.out.printf("-");
//
//            }
//
//            // output to file and remove motif counts
//            BufferedWriter sc = new BufferedWriter(new FileWriter(output));
//            for (int j : fullData.sample) {
//                if (fullData.allMotif.get(j) == null) {
//                    continue;
//                }
//                fullData.allMotif.get(j).printTo(sc, 121);
//                // wipe out fulldata.allMotif
//                fullData.allMotif.get(j).swipe();
//            }
//            sc.close();
//
//
//        }
//
//    }
}
