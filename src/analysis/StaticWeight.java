package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import data.DocumentData;
import data.Info;

public class StaticWeight {

    public static void main(String[] args) throws FileNotFoundException, Exception {
        String[] fileprefix = {"0701", "0702", "0703", "0704", "0705", "0706"};//,
        //	  	, "0707", "0708",
        //		"0709", "0710", "0711", "0712", "0801", "0802","0803" };
        int maxCount = Integer.parseInt(args[0]);
        if (maxCount == 0) {
            maxCount = Integer.MAX_VALUE;
        }

        for (int i = 0; i < fileprefix.length; i++) {
            String calldata = "/data/rwanda_anon/CDR/" + fileprefix[i] + "-Call.pai.sordate.txt";
            String output = new String();
            String translate = new String();
            if (maxCount == Integer.MAX_VALUE) {
                output = "/data/rwanda_anon/richardli/mtd/Weighted" + fileprefix[i] + ".txt";
                translate = "/data/rwanda_anon/richardli/mtd/Translate" + fileprefix[i] + ".txt";
            } else {
                output = "/data/rwanda_anon/richardli/mtd/Weightedpartial" + fileprefix[i] + ".txt";
                translate = "/data/rwanda_anon/richardli/mtd/Translatepartial" + fileprefix[i] + ".txt";
            }
            boolean direct = false;
            HashMap<Long, HashMap<Long, Info>> dyads = DocumentData.ReadInData(calldata, false, direct, Double.MAX_VALUE);
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(output)));
            int count = 0;
            // since the community finding algorithm might only read id starting from 0
            HashMap<Long, Integer> id = new HashMap<Long, Integer>();
            int countID = 0;
            int thisSender;
            int thisReceiver;

            for (long sender : dyads.keySet()) {
                // get sender id translated to new numbering system
                if (id.containsKey(sender)) {
                    thisSender = id.get(sender);
                } else {
                    thisSender = countID;
                    id.put(sender, countID);
                    countID++;
                }
                // count how many edges in total related to this sender
                int sendercount = 0;
                for (long receiver : dyads.get(sender).keySet()) {
                    sendercount += dyads.get(sender).get(receiver).time.size();
                }
                System.out.println(Long.toString(sender) + " : " + Integer.toString(sendercount));

                // loop through receivers
                for (long receiver : dyads.get(sender).keySet()) {
                    if (id.containsKey(receiver)) {
                        thisReceiver = id.get(receiver);
                    } else {
                        thisReceiver = countID;
                        id.put(receiver, countID);
                        countID++;
                    }
                    double weight = (dyads.get(sender).get(receiver).time.size() + 0.0) / (sendercount + 0.0);
                    out.write(Integer.toString(thisSender) + " " + Integer.toString(thisReceiver) + " " + Double.toString(weight) + "\n");
                    count++;
                }
                if (count > maxCount) break;
            }
            System.out.printf("Converted %d edges", count);
            out.close();
            BufferedWriter outtrans = new BufferedWriter(new FileWriter(new File(translate)));
            for (long ids : id.keySet()) {
                outtrans.write(Integer.toString(id.get(ids)) + "," + Long.toString(ids) + "\n");
            }
            outtrans.close();
        }
    }
}
