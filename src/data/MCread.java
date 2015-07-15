package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import analysis.Markov;

public class MCread {
    public static SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
    // start and end date
    public static final long start;
    public static final double endT;

    static {
        long startTemp = 0;
        double endTTemp = 0.0;
        try {
            startTemp = format.parse("070101|00:00:00").getTime();
            long end = format.parse("080401|00:00:00").getTime();
            endTTemp = (double) (end - startTemp) / 1000 / (60 * 60);
        } catch (ParseException e) {
            System.out.println("Error Initializing time");
        }
        start = startTemp;
        endT = endTTemp;
    }

    public static int NaiveReadintoMC(String filename, HashMap<Long, Markov> nodes, boolean iscall, int maxRead) throws ParseException, NumberFormatException, IOException {
        int count = 0;
        //while(sc.hasNextLine() && count < maxRead){
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        int indexT = 0;
        while ((line = sc.readLine()) != null && count <= maxRead) {
            if (maxRead > 0) count++;
            String[] field = line.split("\\|");
            if (field[4].charAt(0) != '-') {
                // get Time
                StringBuilder timeString = new StringBuilder();
                timeString.append(field[2]);
                timeString.append("|");
                timeString.append(field[3]);
                Date timenow = format.parse(timeString.toString());
                double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);
                System.out.println(timenow);
                if (iscall) timedouble = timedouble + (double) Integer.parseInt(field[4]) / (60 * 60);

                // get ID
                String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
                long senderID = Long.parseLong(sender);
                long receiverID = Long.parseLong(receiver);
                indexT = (int) (timedouble / (24 * 7));

                // the following exchange step to save space when saving
                if (senderID > receiverID) {
                    long temp = senderID;
                    senderID = receiverID;
                    receiverID = temp;
                }

                // add to nodes
                if (nodes.containsKey(senderID)) {
                    nodes.get(senderID).add(receiverID, indexT);
                } else {
                    Markov node = new Markov(senderID, receiverID, indexT);
                    nodes.put(senderID, node);
                }
            }
        }
        sc.close();
        return (indexT);
    }
}
