package analysis;

import com.google.common.collect.Sets;
import data.NodeMotif;
import util.VectorUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Updated by zehangli on 7/14/15
 *
 */
public class NodeSample {
//    /**
//     * dict             : Map[Original Long ID, newly assigned integer ID]
//     * sample           : Set[selected integer ID]
//     * orderedSample    : ArrayList[selected integer ID]
//     * allMotif         : Map[interID, NodeMotif object]
//     */
//    public HashMap<Long, Integer> dict = new HashMap<Long, Integer>();
//    public HashSet<Integer> sample = new HashSet<Integer>();
//    public ArrayList<Integer> orderedSample = new ArrayList<Integer>();
//    public HashMap<Integer, NodeMotif> allMotif = new HashMap<Integer, NodeMotif>();
//
//    /** helper function to parse time
//     *
//     * @param field: string vector in the format of ["060101", "00:00:00"]
//     * @return Number of hours passed since 2006-01-01
//     * @throws ParseException
//     */
//    private double parseTime(String[] field) throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
//        long time0 = format.parse("060101|00:00:00").getTime();
//        StringBuilder timeString = new StringBuilder();
//        timeString.append(field[2]);
//        timeString.append("|");
//        timeString.append(field[3]);
//        long timeNow = format.parse(timeString.toString()).getTime();
//        return ((double) ((timeNow - time0) / 1000 / (3600)));
//    }
//
//    /** helper function to parse time
//     *
//     * @param date: string in the format of "060101"
//     * @return Number of hours passed since 2006-01-01
//     * @throws ParseException
//     */
//    private double parseDate(String date) throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
//        date = date + "|00:00:00";
//        long time0 = format.parse("060101|00:00:00").getTime();
//        long timeNow = format.parse(date).getTime();
//        return ((double) ((timeNow - time0) / 1000 / 3600));
//    }
//
//
//    /**
//     * Initialize with MM files
//     *
//     * @param file          : the file name for MM transfer history
//     * @param max           : max number of Nodes to consider (including both MM and phone)
//     * @param MMstart       : start time of mobile money period, e.g. 070101
//     * @param PhoneEnd      : end time of phone motif counting, e.g. 070701 -> phone counts to 070630 midnight
//     * @param maxDate       : end time of mobile money Motif counting, e.g. 080101 -> MM counts to 071230 midnight
//     * @throws IOException
//     * @throws ParseException
//     *
//     *
//     * In the above example,
//     *      sign up before 070101: Y = -1, label = 1
//     *      sign up 070101 to 070630: Y = -1, label = 1
//     *      sign up 070701 to 071230: Y = 1, label = 0;
//     *      sign up after 080101: ignore
//     */
//
//    public void initMM(String file, int max, String MMstart, String PhoneEnd, String maxDate) throws IOException, ParseException {
//        int nextindex = this.dict.size();
//        // time starting reading Phone
//        double startTime = parseDate(MMstart);
//        // time starting checking MM sign-up
//        double midTime = parseDate(PhoneEnd);
//        // time stop reading data
//        double maxTime = parseDate(maxDate);
//        double time = 0.0;
//        String line;
//
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        while ((line = br.readLine()) != null) {
//            String[] field = line.split("\\|");
//            if (field[4].charAt(0) != '-') {
//                time = parseTime(field);
//                if (time > maxTime | nextindex > max) {
//                    break;
//                }
//                String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                long s = Long.parseLong(sender);
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
//                }
//            }
//        }
//        br.close();
//        System.out.println("Finish reading MM files");
//        System.out.println("Number of nodes read: " + nextindex);
//        System.out.println("Last transaction time: " + time);
//    }
//
//    // read phone file
//    public void readPhone(String[] files, int max, String maxDate, int threa) throws ParseException, NumberFormatException, IOException {
//        double maxTime = parseDate(maxDate);
//        double time = 0.0;
//        double counter = Double.MIN_VALUE;
//        String line;
//        for (int i = 0; i < files.length; i++) {
//            String file = files[i];
//            //	@SuppressWarnings("resource")
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            while ((line = br.readLine()) != null) {
//                String[] field = line.split("\\|");
//                if (field[4].charAt(0) != '-') {
//                    time = parseTime(field);
//                    if (time > maxTime) {
//                        br.close();
//                        return;
//                    }
//                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
//
//                    long s = Long.parseLong(sender);
//                    long r = Long.parseLong(receiver);
//
//                    // since the data has been passed once for outlier check
//                    if (this.dict.get(s) == null | this.dict.get(r) == null) {
//                        continue;
//                    }
//                    // update neighborhood
//                    int sid = this.dict.get(s);
//                    int rid = this.dict.get(r);
//                    this.allMotif.get(sid).sendto(rid);
//                    this.allMotif.get(rid).recfrom(sid);
//                    // print a dot for each new day
//                    if (time - counter > 24) {
//                        System.out.printf(".");
//                        counter = time;
//                    }
//                }
//            }
//            br.close();
//            System.out.println("\nFinish reading phone files" + file);
//            System.out.println("Last transaction time: " + time);
//        }
//        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
//        int countremove = 0;
//        while (iter2.hasNext()) {
//            Map.Entry<Long, Integer> entry = iter2.next();
//            int id = entry.getValue();
//            NodeMotif temp = this.allMotif.get(id);
//            temp.thinFreq(threa);
//            if (temp.sList.size() + temp.rList.size() == 0) {
//                iter2.remove();
//                this.allMotif.remove(id);
//                countremove++;
//                continue;
//            }
//        }
//        System.out.println("Number of nodes deleted again: " + countremove);
//
//    }
//
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
//    // thres: integer, how many one-directions calls consider as outlier (without the other direction)
//    // per: double, precentile to consider as outlier for indeg/outdeg/sum/ndeg
//    public void checkOutlier(String[] files, int max, String maxDate, int thres, double per, int hardThrea, boolean indep) throws ParseException, NumberFormatException, IOException {
//        int nextindex = this.dict.size();
//        int beforeindex = nextindex;
//        double maxTime = parseDate(maxDate);
//        double time = 0.0;
//        double counter = Double.MIN_VALUE;
//        String line;
//        for (int i = 0; i < files.length; i++) {
//            String file = files[i];
//            //@SuppressWarnings("resource")
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            while ((line = br.readLine()) != null) {
//                String[] field = line.split("\\|");
//                if (field[4].charAt(0) != '-') {
//                    time = parseTime(field);
//                    if (time > maxTime | nextindex > max) {
//                        br.close();
//                        return;
//                    }
//                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
//                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
//
//                    long s = Long.parseLong(sender);
//                    long r = Long.parseLong(receiver);
//                /*
//				 * if independent, unless both are present or both are both not
//				 * otherwise ignore
//				 */
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
//        for (int node : this.dict.values()) {
//            indegs.add(this.allMotif.get(node).rList.size());
//            outdegs.add(this.allMotif.get(node).sList.size());
//            alldegs.add(this.allMotif.get(node).nList.size());
//
//            infreqs.add(this.allMotif.get(node).inFreq);
//            outfreqs.add(this.allMotif.get(node).outFreq);
//            allfreqs.add(this.allMotif.get(node).inFreq + this.allMotif.get(node).outFreq);
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
//        int countremove = 0;
//        while (iter.hasNext()) {
//            Map.Entry<Long, Integer> entry = iter.next();
//            int id = entry.getValue();
//            NodeMotif temp = this.allMotif.get(id);
//            if (temp.inFreq + temp.outFreq > thres & temp.inFreq * temp.outFreq == 0) {
//                iter.remove();
//                this.allMotif.remove(id);
//                countremove++;
//                continue;
//            }
//            if (temp.inFreq > infreqQuantile | temp.outFreq > outfreqQuantile
//                    | temp.inFreq + temp.outFreq > allfreqQuantile
//                    | temp.rList.size() > indegQuantile
//                    | temp.sList.size() > outdegQuantile
//                    | temp.nList.size() > alldegQuantile) {
//                iter.remove();
//                this.allMotif.remove(id);
//                countremove++;
//                continue;
//            }
//        }
//        System.out.println("Freq: " + infreqQuantile + " " + outfreqQuantile + " " + allfreqQuantile);
//        System.out.println("Deg: " + indegQuantile + " " + outdegQuantile + " " + alldegQuantile);
//
//        System.out.println("Finished deleting outlier, deleted:    " + countremove);
//        System.out.println("Total new nodes read before deletion:  " + (nextindex - 1 - beforeindex));
//        System.out.println("Total nodes after deleting outlier:    " + this.dict.size());
//
//        // second layer of filter
//        double threaFreq = 0.0;
//        ArrayList<Integer> tempFreq = new ArrayList<Integer>();
//        for (int node : this.dict.values()) {
//            if (this.allMotif.get(node).nListFreq != null) {
//                for (int i : this.allMotif.get(node).nListFreq.keySet()) {
//                    tempFreq.add(this.allMotif.get(node).nListFreq.get(i));
//                }
//            }
//        }
////		BufferedWriter brtemp = new BufferedWriter(new FileWriter("/data/rwanda_anon/richardli/freqs.txt"));
////		for(int i = 0; i < tempFreq.size(); i++){
////			brtemp.write(tempFreq.get(i) + ",");
////		}
////		brtemp.close();
//        threaFreq = this.freqProcess(tempFreq);
//
//        System.out.println("Mimimum number of communication between two nodes is " + threaFreq);
//
//        // update dictionary the second time
//        if (threaFreq > 3) hardThrea = (int) threaFreq;
//
//        Iterator<Map.Entry<Long, Integer>> iter2 = this.dict.entrySet().iterator();
//        countremove = 0;
//        while (iter2.hasNext()) {
//            Map.Entry<Long, Integer> entry = iter2.next();
//            int id = entry.getValue();
//            NodeMotif temp = this.allMotif.get(id);
//            temp.thinFreq(hardThrea);
//            if (temp.sList.size() + temp.rList.size() == 0) {
//                iter2.remove();
//                this.allMotif.remove(id);
//                countremove++;
//                continue;
//            }
//            this.allMotif.get(id).reset();
//        }
//        System.out.println("Finished deleting outlier from thinning edegs, deleted:    " + countremove);
//    }
//	/* sample only those with y value equal the input
//	 *  if not sample by outcome, then input y = Integer.MAX_Value
//	 *  if want indep nodes, set indep = TRUE
//	 */
//
//    public void sampleNode(int len, int y, boolean indep, int nseed, int nlayer) {
//        ArrayList<Integer> population = new ArrayList<Integer>();
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
//        // sample ego networks if needed
//        if (nseed != 0) {
//            int[] egos = new int[nseed];
//            for (int i = 0; i < nseed; i++) {
//                egos[i] = population.get(random.nextInt(population.size() - 1));
//            }
//            for (int i : egos) {
//                orderlist.add(i);
//                for (int j = 0; j < nlayer; j++) {
//                    Set<Integer> nextlayer = new HashSet<Integer>();
//                    for (int node : nodes_appeared) {
//                        nextlayer.addAll(this.allMotif.get(node).rList);
//                        nextlayer.addAll(this.allMotif.get(node).sList);
//                    }
//                    orderlist.addAll(nextlayer);
//                }
//            }
//        }
//
//        // perform independent sample if needed
//        if (indep) {
//            Collections.shuffle(population, random);
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
//        this.orderedSample.addAll(shuffle);
//    }
//
//    public void sampleNodePair(int len, int y) {
//        ArrayList<Integer> population = new ArrayList<Integer>();
//        Set<Integer> nodes_appeared = new HashSet<Integer>();
//
//        if (y == Integer.MAX_VALUE) {
//            population.addAll(this.sample);
//        } else {
//            for (int i : this.sample) {
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
//        // perform independent sample and add one of the neighbor
//        Collections.shuffle(population, random);
//        for (int i : population) {
//            if (nodes_appeared.contains(i) |
//                    Sets.intersection(nodes_appeared, this.allMotif.get(i).sList).size() > 0 |
//                    Sets.intersection(nodes_appeared, this.allMotif.get(i).rList).size() > 0) {
//                continue;
//            } else {
//                orderlist.add(i);
//                ArrayList<Integer> nlist = new ArrayList<Integer>(this.allMotif.get(i).nList);
//                Collections.shuffle(nlist, random);
//                orderlist.add(nlist.get(0));
//                nodes_appeared.add(i);
//                nodes_appeared.addAll(this.allMotif.get(i).rList);
//                nodes_appeared.addAll(this.allMotif.get(i).sList);
//            }
//        }
//
//        if (orderlist.size() <= len) {
//            len = orderlist.size();
//        }
//        List<Integer> randlist = orderlist.subList(0, len);
//        this.sample.addAll(randlist);
//        this.orderedSample.addAll(randlist);
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
//        String mmfile = "/data/rwanda_anon/CDR/me2u.ANON.all.txt";
//        String phonefile[] = new String[6];
//        phonefile[0] = "/data/rwanda_anon/CDR/0701-Call.pai.sordate.txt";
//        phonefile[1] = "/data/rwanda_anon/CDR/0702-Call.pai.sordate.txt";
//        phonefile[2] = "/data/rwanda_anon/CDR/0703-Call.pai.sordate.txt";
//        phonefile[3] = "/data/rwanda_anon/CDR/0704-Call.pai.sordate.txt";
//        phonefile[4] = "/data/rwanda_anon/CDR/0705-Call.pai.sordate.txt";
//        phonefile[5] = "/data/rwanda_anon/CDR/0706-Call.pai.sordate.txt";
//
//        String output = "/data/rwanda_anon/richardli/motif6month-pair.txt";
//        // define MMstart as the time when we consider as future MM sign-up
//        // define MMend as the max time in the future we are looking at
//        String MMstart = "070101";
////		String MMstart = "070601";
//        String PhoneEnd = "070701";
//        String MMend = "080101";
////		String MMend = "070901";
//
//        NodeSample fulldata = new NodeSample();
//        // read MM file and update full data
//        fulldata.initMM(mmfile, Integer.MAX_VALUE, MMstart, PhoneEnd, MMend);
//
//        // read phone file first time for outlier
//        int hardThrea = 3;
//        fulldata.checkOutlier(phonefile, Integer.MAX_VALUE, MMend, 1000, 0.99, hardThrea, false);
//        // read phone file
//        fulldata.readPhone(phonefile, Integer.MAX_VALUE, MMend, hardThrea);
//
//        // same thing after sampling independent nodes
//        boolean indep = false;
//        fulldata.sampleNode(Integer.MAX_VALUE, Integer.MAX_VALUE, indep, 0, 0);
//        //fulldata.sampleNode(Integer.MAX_VALUE, 1, indep, 0, 0);
//        System.out.println("Sample of nodes signed up in this period: " + fulldata.sample.size());
//        // sample nodes without signing up
//        //fulldata.sampleNode(Integer.MAX_VALUE, 0, indep, 0, 0);
//        System.out.println("Sample of nodes in the sample now:        " + fulldata.sample.size());
//
//        // sample ordered pairs
//        fulldata.sampleNodePair(Integer.MAX_VALUE, Integer.MAX_VALUE);
//        System.out.println("Sample of nodes in the sample now:        " + fulldata.orderedSample.size());
//
//        // get all the data organized. Note this is necessary as those not in the sample could be reached by friendship map
//        for (int i : fulldata.allMotif.keySet()) {
//            fulldata.allMotif.get(i).organize();
//        }
//        // put sampled data to sample
//        int tempcount = 0;
//
//        for (int i : fulldata.sample) {
//            fulldata.allMotif.get(i).motifCount_wlabel(fulldata.allMotif);
//            tempcount++;
//            if (tempcount % 1000 == 0) System.out.printf(".");
//        }
//
//        // output to file
//        BufferedWriter sc = new BufferedWriter(new FileWriter(output));
//        for (int i : fulldata.orderedSample) {
//            fulldata.allMotif.get(i).printTo(sc, 121);
//        }
//        sc.close();
//
//
//    }
}
