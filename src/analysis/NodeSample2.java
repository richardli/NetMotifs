package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import util.MathUtil;

import com.google.common.collect.Sets;

import data.NodeMotif;

public class NodeSample2 {
    public HashMap<Long, Integer> dict = new HashMap<Long, Integer>();
    public HashSet<Integer> sample = new HashSet<Integer>();
    public HashMap<Integer, NodeMotif> allmotif = new HashMap<Integer, NodeMotif>();

    // helper function to parse time
    private double parseTime(String[] field) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long time0 = format.parse("060101|00:00:00").getTime();
        StringBuilder timeString = new StringBuilder();
        timeString.append(field[2]);
        timeString.append("|");
        timeString.append(field[3]);
        long timenow = format.parse(timeString.toString()).getTime();
        return ((double) ((timenow - time0) / 1000 / (3600)));
    }

    // helper function to parse time
    private double parseDate(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        date = date + "|00:00:00";
        long time0 = format.parse("060101|00:00:00").getTime();
        long timenow = format.parse(date).getTime();
        return ((double) ((timenow - time0) / 1000 / 3600));
    }


    // initialize with MM file
    // for example, if we want to count motif from 0701 to 0706, see results during 0707 to 0712
    // sign up before 070101: Y = -1, label = 1 (might need more Y values later for this)
    // sign up 070101 - 070701: Y = -1, label = 1
    // sign up 070101 - 080101: Y = 1, label = 0;
    // sign up after 080101: ignore
    //
    // so MMstart = 070701
    //    PhoneEnd = 070707
    //	  MMend = 080101
    public void initMM(String file, int max, String MMstart, String PhoneEnd, String maxDate) throws IOException, ParseException {
        int nextindex = this.dict.size();
        // time starting reading Phone
        double startTime = parseDate(MMstart);
        // time starting checking MM sign-up
        double midTime = parseDate(PhoneEnd);
        // time stop reading data
        double maxTime = parseDate(maxDate);
        double time = 0.0;
        String line;

        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
            String[] field = line.split("\\|");
            if (field[4].charAt(0) != '-') {
                time = parseTime(field);
                if (time > maxTime | nextindex > max) {
                    break;
                }
                String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                long s = Long.parseLong(sender);
                if (this.dict.get(s) == null) {
                    this.dict.put(s, nextindex);
                    if (time < startTime) {
                        this.allmotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
                    } else if (time < midTime) {
                        this.allmotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
                    } else {
                        this.allmotif.put(nextindex, new NodeMotif(nextindex, time, 1, 0));
                    }
                    nextindex++;
                }
            }
        }
        br.close();
        System.out.println("Finish reading MM files");
        System.out.println("Number of nodes read: " + nextindex);
        System.out.println("Last transaction time: " + time);
    }

    // read phone file
    public void readPhone(String[] files, int max, String maxDate, int threa) throws ParseException, NumberFormatException, IOException {
        double maxTime = parseDate(maxDate);
        double time = 0.0;
        double counter = Double.MIN_VALUE;
        String line;
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            //	@SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = parseTime(field);
                    if (time > maxTime) {
                        br.close();
                        return;
                    }
                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");

                    long s = Long.parseLong(sender);
                    long r = Long.parseLong(receiver);

                    // since the data has been passed once for outlier check
                    if (this.dict.get(s) == null | this.dict.get(r) == null) {
                        continue;
                    }
                    // update neighborhood
                    int sid = this.dict.get(s);
                    int rid = this.dict.get(r);
                    this.allmotif.get(sid).sendto(rid);
                    this.allmotif.get(rid).recfrom(sid);
                    // print a dot for each new day
                    if (time - counter > 24) {
                        System.out.printf(".");
                        counter = time;
                    }
                }
            }
            br.close();
            System.out.println("\nFinish reading phone files" + file);
            System.out.println("Last transaction time: " + time);
        }
        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
        int countremove = 0;
        while (iter2.hasNext()) {
            Map.Entry<Long, Integer> entry = iter2.next();
            int id = entry.getValue();
            NodeMotif temp = this.allmotif.get(id);
            temp.thinFreq(threa);
            if (temp.sList.size() + temp.rList.size() == 0) {
                iter2.remove();
                this.allmotif.remove(id);
                countremove++;
                continue;
            }
        }
        System.out.println("Number of nodes deleted again: " + countremove);

    }

    public double freqProcess(ArrayList<Integer> raw) {

        double mean = 0.0;
        double mean2 = 0.0;
        int count = 0;
        for (int freq : raw) {
            mean += freq;
            mean2 += freq * freq;
            count += 1;
        }
        mean = mean / (count + 0.0);
        mean2 = mean2 / (count + 0.0);
        double sd = Math.sqrt(mean2 - mean * mean);
        double result = mean - sd * 3;
        System.out.println("Mean: " + mean + "SD: " + sd);
        return result;
    }


    // thres: integer, how many one-directions calls consider as outlier (without the other direction)
    // per: double, precentile to consider as outlier for indeg/outdeg/sum/ndeg
    public void checkOutlier(String[] files, int max, String maxDate, int thres, double per, int hardThrea, boolean indep) throws ParseException, NumberFormatException, IOException {
        int nextindex = this.dict.size();
        int beforeindex = nextindex;
        double maxTime = parseDate(maxDate);
        double time = 0.0;
        double counter = Double.MIN_VALUE;
        String line;
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            //@SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = parseTime(field);
                    if (time > maxTime | nextindex > max) {
                        br.close();
                        return;
                    }
                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");

                    long s = Long.parseLong(sender);
                    long r = Long.parseLong(receiver);
                /*
				 * if independent, unless both are present or both are both not
				 * otherwise ignore
				 */
                    if (indep) {
                        if (this.dict.containsKey(s) != this.dict.containsKey(r)) {
                            continue;
                        }
                    }

                    if (this.dict.get(s) == null) {
                        this.dict.put(s, nextindex);
                        this.allmotif.put(nextindex, new NodeMotif(nextindex));
                        nextindex++;
                    }
                    if (this.dict.get(r) == null) {
                        this.dict.put(r, nextindex);
                        this.allmotif.put(nextindex, new NodeMotif(nextindex));
                        nextindex++;
                    }
                    int sid = this.dict.get(s);
                    int rid = this.dict.get(r);

                    // update neighbors
                    this.allmotif.get(sid).sendto(rid);
                    this.allmotif.get(rid).recfrom(sid);

                    // update degree counts (frequency)
                    this.allmotif.get(sid).outFreq++;
                    this.allmotif.get(rid).inFreq++;
                    // print a dot for each new day
                    if (time - counter > 24) {
                        System.out.printf(".");
                        counter = time;
                    }
                }
            }
            br.close();
        }
        // calcualte quantiles of in-deg, out-deg and their sums
        ArrayList<Integer> indegs = new ArrayList<Integer>();
        ArrayList<Integer> outdegs = new ArrayList<Integer>();
        ArrayList<Integer> alldegs = new ArrayList<Integer>();
        ArrayList<Integer> infreqs = new ArrayList<Integer>();
        ArrayList<Integer> outfreqs = new ArrayList<Integer>();
        ArrayList<Integer> allfreqs = new ArrayList<Integer>();

        for (int node : this.dict.values()) {
            indegs.add(this.allmotif.get(node).rList.size());
            outdegs.add(this.allmotif.get(node).sList.size());
            alldegs.add(this.allmotif.get(node).nList.size());

            infreqs.add(this.allmotif.get(node).inFreq);
            outfreqs.add(this.allmotif.get(node).outFreq);
            allfreqs.add(this.allmotif.get(node).inFreq + this.allmotif.get(node).outFreq);
        }
        int indegQuantile = MathUtil.percentile(indegs, per);
        int outdegQuantile = MathUtil.percentile(outdegs, per);
        int alldegQuantile = MathUtil.percentile(alldegs, per);
        int infreqQuantile = MathUtil.percentile(infreqs, per);
        int outfreqQuantile = MathUtil.percentile(outfreqs, per);
        int allfreqQuantile = MathUtil.percentile(allfreqs, per);


        //update dictionary
        Iterator<Map.Entry<Long, Integer>> iter = this.dict.entrySet().iterator();
        int countremove = 0;
        while (iter.hasNext()) {
            Map.Entry<Long, Integer> entry = iter.next();
            int id = entry.getValue();
            NodeMotif temp = this.allmotif.get(id);
            if (temp.inFreq + temp.outFreq > thres & temp.inFreq * temp.outFreq == 0) {
                iter.remove();
                this.allmotif.remove(id);
                countremove++;
                continue;
            }
            if (temp.inFreq > infreqQuantile | temp.outFreq > outfreqQuantile
                    | temp.inFreq + temp.outFreq > allfreqQuantile
                    | temp.rList.size() > indegQuantile
                    | temp.sList.size() > outdegQuantile
                    | temp.nList.size() > alldegQuantile) {
                iter.remove();
                this.allmotif.remove(id);
                countremove++;
                continue;
            }
        }
        System.out.println("Freq: " + infreqQuantile + " " + outfreqQuantile + " " + allfreqQuantile);
        System.out.println("Deg: " + indegQuantile + " " + outdegQuantile + " " + alldegQuantile);

        System.out.println("Finished deleting outlier, deleted:    " + countremove);
        System.out.println("Total new nodes read before deletion:  " + (nextindex - 1 - beforeindex));
        System.out.println("Total nodes after deleting outlier:    " + this.dict.size());

        // second layer of filter
        double threaFreq = 0.0;
        ArrayList<Integer> tempFreq = new ArrayList<Integer>();
        for (int node : this.dict.values()) {
            if (this.allmotif.get(node).nListFreq != null) {
                for (int i : this.allmotif.get(node).nListFreq.keySet()) {
                    tempFreq.add(this.allmotif.get(node).nListFreq.get(i));
                }
            }
        }
        BufferedWriter brtemp = new BufferedWriter(new FileWriter("/data/rwanda_anon/richardli/freqs.txt"));
        for (int i = 0; i < tempFreq.size(); i++) {
            brtemp.write(tempFreq.get(i) + ",");
        }
        brtemp.close();
        threaFreq = this.freqProcess(tempFreq);

        System.out.println("Mimimum number of communication between two nodes is " + threaFreq);

        // update dictionary the second time
        if (threaFreq > 3) hardThrea = (int) threaFreq;

        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
        countremove = 0;
        while (iter2.hasNext()) {
            Map.Entry<Long, Integer> entry = iter2.next();
            int id = entry.getValue();
            NodeMotif temp = this.allmotif.get(id);
            temp.thinFreq(hardThrea);
            if (temp.sList.size() + temp.rList.size() == 0) {
                iter2.remove();
                this.allmotif.remove(id);
                countremove++;
                continue;
            }
            this.allmotif.get(id).reset();
        }
        System.out.println("Finished deleting outlier from thinning edegs, deleted:    " + countremove);
    }
	/* sample only those with y value equal the input
	 *  if not sample by outcome, then input y = Integer.MAX_Value
	 *  if want indep nodes, set indep = TRUE
	 */

    public void sampleNode(int len, int y, boolean indep) {
        Set<Integer> population = new HashSet<Integer>();
        Set<Integer> nodes_appeared = new HashSet<Integer>();

        if (y == Integer.MAX_VALUE) {
            population.addAll(this.dict.values());
        } else {
            for (int i : this.allmotif.keySet()) {
                if (allmotif.get(i).y == y) population.add(i);
            }
        }

        if (population.size() == 0) {
            return;
        }

        // simple random sample of IDs
        Random random = new Random();
        List<Integer> orderlist = new LinkedList<Integer>();

        // perform independent sample if needed
        if (indep) {
            for (int i : population) {
                if (nodes_appeared.contains(i) |
                        Sets.intersection(nodes_appeared, this.allmotif.get(i).sList).size() > 0 |
                        Sets.intersection(nodes_appeared, this.allmotif.get(i).rList).size() > 0) {
                    continue;
                } else {
                    orderlist.add(i);
                    nodes_appeared.add(i);
                    nodes_appeared.addAll(this.allmotif.get(i).rList);
                    nodes_appeared.addAll(this.allmotif.get(i).sList);
                }
            }
        } else {
            orderlist = new LinkedList<Integer>(population);
        }

        Collections.shuffle(orderlist, random);
        if (orderlist.size() <= len) {
            len = orderlist.size();
        }
        List<Integer> randlist = orderlist.subList(0, len);
        Set<Integer> shuffle = new HashSet<Integer>(randlist);
        this.sample.addAll(shuffle);
    }

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


        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON.all.txt";
        String phonefile[] = new String[1];
        phonefile[0] = "/data/rwanda_anon/CDR/0701-Call.pai.sordate.txt";
//		phonefile[1] = "/data/rwanda_anon/CDR/0702-Call.pai.sordate.txt";
//		phonefile[2] = "/data/rwanda_anon/CDR/0703-Call.pai.sordate.txt";
//		phonefile[3] = "/data/rwanda_anon/CDR/0704-Call.pai.sordate.txt";
//		phonefile[4] = "/data/rwanda_anon/CDR/0705-Call.pai.sordate.txt";
//		phonefile[5] = "/data/rwanda_anon/CDR/0706-Call.pai.sordate.txt";

        String output = "/data/rwanda_anon/richardli/motifweekly.txt";
        // define MMstart as the time when we consider as future MM sign-up
        // define MMend as the max time in the future we are looking at
        String MMstart = "070101";
        String PhoneEnd = "070701";
        String MMend = "080101";

        NodeSample2 fulldata = new NodeSample2();
        // read MM file and update full data
        fulldata.initMM(mmfile, Integer.MAX_VALUE, MMstart, PhoneEnd, MMend);

        // read phone file first time for outlier
        int hardThrea = 3;
        fulldata.checkOutlier(phonefile, Integer.MAX_VALUE, MMend, 1000, 0.99, hardThrea, false);
        // read phone file
        fulldata.readPhone(phonefile, Integer.MAX_VALUE, MMend, hardThrea);

        // get all the nodes signed up in the given period in the sample
        //fulldata.sampleNode(Integer.MAX_VALUE, 1, false);
        //System.out.println("Number of nodes signed up in this period: " + fulldata.sample.size());
        // sample nodes without signing up
        //fulldata.sampleNode(Integer.MAX_VALUE, 0, false);
        //System.out.println("Number of nodes in the sample now:        " + fulldata.sample.size());

        // same thing after sampling independent nodes
        boolean indep = false;
        fulldata.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
        fulldata.sampleNode(Integer.MAX_VALUE, 1, indep);
        System.out.println("Sample of nodes signed up in this period: " + fulldata.sample.size());
        // sample nodes without signing up
        fulldata.sampleNode(Integer.MAX_VALUE, 0, indep);
        System.out.println("Sample of nodes in the sample now:        " + fulldata.sample.size());


        // get all the data organized. Note this is necessary as those not in the sample could be reached by friendship map
        for (int i : fulldata.allmotif.keySet()) {
            fulldata.allmotif.get(i).organize();
        }
        // put sampled data to sample
        int tempcount = 0;

        for (int i : fulldata.sample) {
            fulldata.allmotif.get(i).motifCount_wlabel(fulldata.allmotif);
            tempcount++;
            if (tempcount % 1000 == 0) System.out.printf(".");

        }

        // output to file
        BufferedWriter sc = new BufferedWriter(new FileWriter(output));
        for (int i : fulldata.sample) {
            fulldata.allmotif.get(i).printto(sc, 121);
        }
        sc.close();


    }
}
