package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import util.MathUtil;
import data.NodeMotif;

/**
 * Count Motifs for each week
 *
 * Updated by zehangli 07/14/15
 */

public class NodeSampleWeek {
    // dictionary of IDs
    public HashMap<Long, Integer> dict = new HashMap<Long, Integer>();
    // initialize size of nodes
    int allSize = 0;
    // a set of integers as sample
    public HashSet<Integer> sample = new HashSet<Integer>();
    // HashMap of NodeMotifs
    public HashMap<Integer, NodeMotif> allMotif = new HashMap<Integer, NodeMotif>();


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
    public void streamMM(String file, int max, String MMstart, String PhoneEnd, String maxDate) throws IOException, ParseException {
        int nextindex = this.allSize;
        // time starting reading Phone
        double startTime = GlobalHelper.parseDate(MMstart);
        // time starting checking MM sign-up
        double midTime = GlobalHelper.parseDate(PhoneEnd);
        // time stop reading data
        double maxTime = GlobalHelper.parseDate(maxDate);
        double time = 0.0;

        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));


        while ((line = br.readLine()) != null) {
            String[] field = line.split("\\|");
            if (field[4].charAt(0) != '-') {
                time = parseTime(field);
                // this is not correct, comment out.
                //				if(time < startTime){continue;}
                if (time > maxTime | nextindex > max) {
                    break;
                }
                String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                long s = Long.parseLong(sender);
                if (this.dict.get(s) == null) {
                    this.dict.put(s, nextindex);
                    if (time < startTime) {
                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
                    } else if (time < midTime) {
                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, -1, 1));
                    } else {
                        this.allMotif.put(nextindex, new NodeMotif(nextindex, time, 1, 0));
                    }
                    nextindex++;
                } else {
                    // if node already in the file, update label and y
                    int index = this.dict.get(s);
                    // if before mid time point, it has to be y = -1
                    if (time < midTime) {
                        this.allMotif.get(index).label = 1;
                        this.allMotif.get(index).y = -1;
                        // otherwise, need to see if y = 1 already before.
                        // if y = 1, might need to change to -1, since the prediction period is changed
                    } else if (this.allMotif.get(index).y == 1 & this.allMotif.get(index).t < midTime) {
                        this.allMotif.get(index).label = 1;
                        this.allMotif.get(index).y = -1;
                        // otherwise if y = 0, since time > mid time now, change y into 1
                    } else if (this.allMotif.get(index).y == 0) {
                        this.allMotif.get(index).label = 0;
                        this.allMotif.get(index).y = 1;
                    }
                }
            }
        }
        this.allSize = nextindex;
        br.close();
        System.out.println("Finish reading MM files for current period");
        System.out.println("Number of nodes now: " + nextindex);
        System.out.println("Last transaction time: " + time);
    }

    // read phone file
    public void streamPhone(String[] file, int max, String mmStart, String maxDate, int threa) throws ParseException, NumberFormatException, IOException {

        double startTime = parseDate(mmStart);
        double maxTime = parseDate(maxDate);
        double time;
        double counter = Double.MIN_VALUE;
        String line;
        System.out.println("Read phone from " + mmStart + "to" + maxDate);
        for (int i = 0; i < file.length; i++) {
            BufferedReader br = new BufferedReader(new FileReader(file[i]));
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = parseTime(field);
                    if (time < startTime) {
                        continue;
                    }
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
                    this.allMotif.get(sid).sendto(rid);
                    this.allMotif.get(rid).recfrom(sid);
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
        //	comment out for now, not sure what is going on here...
        //			Iterator<Map.Entry<Long,Integer>> iter2 = this.dict.entrySet().iterator();
        //			int countremove = 0;
        //			while(iter2.hasNext()){
        //				Map.Entry<Long,Integer> entry = iter2.next();
        //				int id = entry.getValue();
        //				NodeMotif temp = this.allMotif.get(id);
        //				temp.thinFreq(threa);
        //				if(temp.sList.size() + temp.rList.size() == 0){
        //					iter2.remove();
        //					this.allMotif.remove(id);
        //					countremove ++;
        //					continue;
        //				}
        //			}
        //			System.out.println("Number of nodes deleted again: " + countremove);
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
    public void checkOutlier(String[] files, int max, String mmStart, String maxDate, int thres, double per, int hardThrea, boolean indep) throws ParseException, NumberFormatException, IOException {
        int nextindex = this.allSize;
        int beforeindex = nextindex;
        double startTime = parseDate(mmStart);
        double maxTime = parseDate(maxDate);
        double time = 0.0;
        double counter = Double.MIN_VALUE;
        String line;
        System.out.println("First-batch check outlier: phone from " + mmStart + "to" + maxDate);
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    time = parseTime(field);
                    if (time < startTime) {
                        continue;
                    }
                    if (time > maxTime | nextindex > max) {
                        break;
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
                        this.allMotif.put(nextindex, new NodeMotif(nextindex));
                        nextindex++;
                    }
                    if (this.dict.get(r) == null) {
                        this.dict.put(r, nextindex);
                        this.allMotif.put(nextindex, new NodeMotif(nextindex));
                        nextindex++;
                    }
                    int sid = this.dict.get(s);
                    int rid = this.dict.get(r);

                    // update neighbors
                    this.allMotif.get(sid).sendto(rid);
                    this.allMotif.get(rid).recfrom(sid);

                    // update degree counts (frequency)
                    this.allMotif.get(sid).outFreq++;
                    this.allMotif.get(rid).inFreq++;
                    // print a dot for each new day
                    if (time - counter > 24) {
                        System.out.printf(".");
                        counter = time;
                    }
                }
            }
            this.allSize = nextindex;
            br.close();
        }
        // calcualte quantiles of in-deg, out-deg and their sums
        ArrayList<Integer> indegs = new ArrayList<Integer>();
        ArrayList<Integer> outdegs = new ArrayList<Integer>();
        ArrayList<Integer> alldegs = new ArrayList<Integer>();
        ArrayList<Integer> infreqs = new ArrayList<Integer>();
        ArrayList<Integer> outfreqs = new ArrayList<Integer>();
        ArrayList<Integer> allfreqs = new ArrayList<Integer>();

        for (int node : this.allMotif.keySet()) {
            if (this.allMotif.get(node) == null) {
                System.out.println("!");
                continue;
            }
            indegs.add(this.allMotif.get(node).rList.size());
            outdegs.add(this.allMotif.get(node).sList.size());
            alldegs.add(this.allMotif.get(node).nList.size());

            infreqs.add(this.allMotif.get(node).inFreq);
            outfreqs.add(this.allMotif.get(node).outFreq);
            allfreqs.add(this.allMotif.get(node).inFreq + this.allMotif.get(node).outFreq);
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
            if (this.allMotif.get(id) == null) {
                System.out.println("!");
                continue;
            }
            NodeMotif temp = this.allMotif.get(id);
            if (temp.inFreq + temp.outFreq > thres & temp.inFreq * temp.outFreq == 0) {
                iter.remove();
                this.allMotif.remove(id);
                countremove++;
                continue;
            }
            if (temp.inFreq > infreqQuantile | temp.outFreq > outfreqQuantile
                    | temp.inFreq + temp.outFreq > allfreqQuantile
                    | temp.rList.size() > indegQuantile
                    | temp.sList.size() > outdegQuantile
                    | temp.nList.size() > alldegQuantile) {
                iter.remove();
                this.allMotif.remove(id);
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
        for (int node : this.allMotif.keySet()) {
            if (this.allMotif.get(node).nListFreq != null) {
                for (int i : this.allMotif.get(node).nListFreq.keySet()) {
                    tempFreq.add(this.allMotif.get(node).nListFreq.get(i));
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
        if (threaFreq > hardThrea) hardThrea = (int) threaFreq;

        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
        countremove = 0;
        while (iter2.hasNext()) {
            int id = iter2.next().getValue();
            if (this.allMotif.get(id) == null) {
                System.out.println("!");
                continue;
            }
            NodeMotif temp = this.allMotif.get(id);
            temp.thinFreq(hardThrea);
            if (temp.sList.size() + temp.rList.size() == 0) {
                iter2.remove();
                this.allMotif.remove(id);
                countremove++;
                continue;
            }
            this.allMotif.get(id).reset();
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
            for (int i : this.allMotif.keySet()) {
                if (allMotif.get(i).y == y) population.add(i);
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
                        Sets.intersection(nodes_appeared, this.allMotif.get(i).sList).size() > 0 |
                        Sets.intersection(nodes_appeared, this.allMotif.get(i).rList).size() > 0) {
                    continue;
                } else {
                    orderlist.add(i);
                    nodes_appeared.add(i);
                    nodes_appeared.addAll(this.allMotif.get(i).rList);
                    nodes_appeared.addAll(this.allMotif.get(i).sList);
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
        String phonefile[] = new String[2];
        phonefile[0] = "/data/rwanda_anon/CDR/0805-Call.pai.sordate.txt";
        phonefile[1] = "/data/rwanda_anon/CDR/0806-Call.pai.sordate.txt";
//		phonefile[2] = "/data/rwanda_anon/CDR/0703-Call.pai.sordate.txt";
//		phonefile[3] = "/data/rwanda_anon/CDR/0704-Call.pai.sordate.txt";
//		phonefile[4] = "/data/rwanda_anon/CDR/0705-Call.pai.sordate.txt";
//		phonefile[5] = "/data/rwanda_anon/CDR/0706-Call.pai.sordate.txt";

        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long t1 = format.parse("080501|00:00:00").getTime();
        long t2 = format.parse("080701|00:00:00").getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(format.parse("080501|00:00:00"));

        int nday = ((int) ((t2 - t1) / 1000 / (3600) / 24));
        System.out.printf("%d days in the period\n", nday);
        int nweek = (int) nday / 7;
        System.out.printf("%d weeks in the period\n", nweek);

        // initialize mm file reader outside the loop
        NodeSampleWeek fulldata = new NodeSampleWeek();

        for (int i = 0; i < nweek; i++) {
            String output = "/data/rwanda_anon/richardli/0501week" + i + ".txt";
            // define MMstart as the time when we consider as future MM sign-up
            // define MMend as the max time in the future we are looking at
            String Phonestart = format.format(cal.getTime()).substring(0, 6);
            cal.add(Calendar.DATE, 7);
            String MMstart = format.format(cal.getTime()).substring(0, 6);
            String PhoneEnd = MMstart;
            cal.add(Calendar.DATE, 7);
            String MMend = format.format(cal.getTime()).substring(0, 6);
            System.out.print("Steaming MM data from " + MMstart + " to " + MMend);
            // read MM file and update full data
            fulldata.streamMM(mmfile, Integer.MAX_VALUE, MMstart, PhoneEnd, MMend);
            System.out.print("Steaming MM" + MMstart + " to " + MMend + " done\n");
            int hardThrea = 2;
            fulldata.checkOutlier(phonefile, Integer.MAX_VALUE, Phonestart, PhoneEnd, 1000, 0.99, hardThrea, false);
            fulldata.streamPhone(phonefile, Integer.MAX_VALUE, Phonestart, PhoneEnd, hardThrea);

            boolean indep = false;
            fulldata.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep);
            fulldata.sampleNode(Integer.MAX_VALUE, 1, indep);
            System.out.println("Sample of nodes signed up in this period: " + fulldata.sample.size());
            // sample nodes without signing up
            fulldata.sampleNode(Integer.MAX_VALUE, 0, indep);
            System.out.println("Sample of nodes in the sample now:        " + fulldata.sample.size());
            // get all the data organized. Note this is necessary as those not in the sample could be reached by friendship map
            for (int j : fulldata.allMotif.keySet()) {
                fulldata.allMotif.get(j).organize();
            }
            int tempcount = 0;
            for (int j : fulldata.sample) {
                if (fulldata.allMotif.get(j) == null) {
                    continue;
                }
                fulldata.allMotif.get(j).motifCount_wlabel(fulldata.allMotif);
                tempcount++;
                if (tempcount % 10000 == 0) System.out.printf("-");

            }

            // output to file
            BufferedWriter sc = new BufferedWriter(new FileWriter(output));
            for (int j : fulldata.sample) {
                if (fulldata.allMotif.get(j) == null) {
                    continue;
                }
                fulldata.allMotif.get(j).printto(sc, 121);
                // wipe out fulldata.allMotif
                fulldata.allMotif.get(j).swipe();
            }
            sc.close();


        }

    }
}
