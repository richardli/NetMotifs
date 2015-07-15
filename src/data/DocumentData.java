package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DocumentData {
    // read in txt data from a specific path
    // Initilizating constant variable
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

    // maxRead = 0 -> no limit on maxRead
    // NaiveRead -> read data into just <sender, receiver, time> Array list
    public static ArrayList<Triple> NaiveRead(String filename, boolean iscall, int maxRead)
            throws ParseException, NumberFormatException, IOException {
        ArrayList<Triple> dyads = new ArrayList<Triple>();
        int count = 0;
        //		while(sc.hasNextLine() && count < maxRead){
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
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
                Triple thisline = new Triple(senderID, receiverID, timedouble);
                dyads.add(thisline);
            }

        }
        sc.close();
        return dyads;
    }

    // read data into format: HashMap<sender, HashMap<receiver, all time>>
    //
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String filename, boolean iscall, boolean direct, double maxTime)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        boolean reachMax = false;
        while ((line = sc.readLine()) != null & !reachMax) {
            reachMax = ParseLine(line, dyads, iscall, direct, maxTime);
        }
        sc.close();
        return dyads;
    }

    // read data into format: HashMap<sender, HashMap<receiver, all time>>
    // read multiple files
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String[] filename, boolean iscall, boolean direct, double maxTime)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        for (String file : filename) {
            BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(file)));
            String line;
            boolean reachMax = false;
            while ((line = sc.readLine()) != null & !reachMax) {
                reachMax = ParseLine(line, dyads, iscall, direct, maxTime);
            }
            sc.close();
        }
        return dyads;
    }


    //	read data into format: HashMap<sender, HashMap<receiver, all time>>
    //	specify start and lasting time
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String filename, boolean iscall, boolean direct, double startTime, double duration)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        boolean reachMax = false;
        while ((line = sc.readLine()) != null & !reachMax) {
            reachMax = ParseLine(line, dyads, iscall, direct, startTime, duration);
        }
        sc.close();
        return dyads;
    }

    // read data into format: HashMap<sender, HashMap<receiver, all time>>
    // read multiple files
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String[] filename, boolean iscall, boolean direct, double startTime, double duration)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        for (String file : filename) {
            BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(file)));
            String line;
            boolean reachMax = false;
            while ((line = sc.readLine()) != null & !reachMax) {
                reachMax = ParseLine(line, dyads, iscall, direct, startTime, duration);
            }
            sc.close();
        }
        return dyads;
    }


    // read in data only with ID in the two lists
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String filename, boolean iscall,
                                                                ArrayList<Long> first, ArrayList<Long> second, boolean direct, double maxTime)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        boolean reachMax = false;
        while ((line = sc.readLine()) != null & !reachMax) {
            reachMax = ParseLine(line, dyads, iscall, first, second, direct, maxTime);
        }
        sc.close();
        return dyads;
    }

    // read in data only with ID in one list, and the cluster assignment of the list
    public static HashMap<Long, HashMap<Long, Info>> ReadInData(String filename, boolean iscall,
                                                                ArrayList<Long> first, HashMap<Long, Integer> clusterAssignment, boolean direct, double maxTime)
            throws FileNotFoundException, Exception {
        HashMap<Long, HashMap<Long, Info>> dyads = new HashMap<Long, HashMap<Long, Info>>();
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        boolean reachMax = false;
        while ((line = sc.readLine()) != null & !reachMax) {
            reachMax = ParseLine(line, dyads, iscall, first, clusterAssignment, direct, maxTime);
        }
        sc.close();
        return dyads;
    }


    // read data into format: HashMap<sender, HashMap<receiver, all time>>
    //
    public static int ReadInDataMulti(String filename, HashMap<Long, HashMap<Long, Info>> dyads, boolean iscall, int MaxRead, boolean direct)
            throws FileNotFoundException, Exception {
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
        String line;
        int countRead = 0;
        int countEdge = 0;
        while ((line = sc.readLine()) != null & countRead < MaxRead) {
            ParseLine(line, dyads, iscall, direct, Double.MAX_VALUE);
            countRead++;
        }
        sc.close();
        return countEdge;
    }

    // parse line into format of ReadInData function
    // return 0 if no new edge, 1 if new edge
    private static boolean ParseLine(String line, HashMap<Long, HashMap<Long, Info>> dyads, boolean iscall, boolean direct, double max)
            throws ParseException {
        String[] field = line.split("\\|");
        if (field[4].charAt(0) != '-') {
            // get Time
            StringBuilder timeString = new StringBuilder();
            timeString.append(field[2]);
            timeString.append("|");
            timeString.append(field[3]);
            Date timenow = format.parse(timeString.toString());
            System.out.println(timenow);
            double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);

            if (timedouble >= max) return true;

            if (iscall) timedouble = timedouble + (double) Integer.parseInt(field[4]) / (60 * 60);

            // get ID
            String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
            String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
            long senderID = Long.parseLong(sender);
            long receiverID = Long.parseLong(receiver);
            // if(!iscall) int value = Integer.parseInt(lineSplit[4]) ;

            if (dyads.get(senderID) != null) {
                if (dyads.get(senderID).get(receiverID) != null) {
                    dyads.get(senderID).get(receiverID).add(timedouble);
                } else {
                    Info firstContact = new Info(timedouble);
                    dyads.get(senderID).put(receiverID, firstContact);
                }
            } else {
                Info firstContact = new Info(timedouble);
                HashMap<Long, Info> toadd = new HashMap<Long, Info>();
                toadd.put(receiverID, firstContact);
                dyads.put(senderID, toadd);
            }

        }
        return false;
    }

    // parse line into format of ReadInData function
    // return 0 if no new edge, 1 if new edge
    private static boolean ParseLine(String line, HashMap<Long, HashMap<Long, Info>> dyads, boolean iscall, boolean direct, double startTime, double duration)
            throws ParseException {
        String[] field = line.split("\\|");
        if (field[4].charAt(0) != '-') {
            // get Time
            StringBuilder timeString = new StringBuilder();
            timeString.append(field[2]);
            timeString.append("|");
            timeString.append(field[3]);
            Date timenow = format.parse(timeString.toString());
            System.out.println(timenow);
            double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);

            if (timedouble < startTime) return false;
            if (timedouble >= startTime + duration) return true;

            if (iscall) timedouble = timedouble + (double) Integer.parseInt(field[4]) / (60 * 60);

            // get ID
            String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
            String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
            long senderID = Long.parseLong(sender);
            long receiverID = Long.parseLong(receiver);
            // if(!iscall) int value = Integer.parseInt(lineSplit[4]) ;

            if (dyads.get(senderID) != null) {
                if (dyads.get(senderID).get(receiverID) != null) {
                    dyads.get(senderID).get(receiverID).add(timedouble);
                } else {
                    Info firstContact = new Info(timedouble);
                    dyads.get(senderID).put(receiverID, firstContact);
                }
            } else {
                Info firstContact = new Info(timedouble);
                HashMap<Long, Info> toadd = new HashMap<Long, Info>();
                toadd.put(receiverID, firstContact);
                dyads.put(senderID, toadd);
            }

        }
        return false;
    }

    // parse line into format of ReadInData function
    // only read in data contained in the two lists
    // return 0 if no new edge, 1 if new edge
    private static boolean ParseLine(String line, HashMap<Long, HashMap<Long, Info>> dyads, boolean iscall,
                                     ArrayList<Long> first, ArrayList<Long> second, boolean direct, double max)
            throws ParseException {
        String[] field = line.split("\\|");
        if (field[4].charAt(0) != '-') {
            // get Time
            StringBuilder timeString = new StringBuilder();
            timeString.append(field[2]);
            timeString.append("|");
            timeString.append(field[3]);
            Date timenow = format.parse(timeString.toString());
            System.out.println(timenow);
            double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);
            if (timedouble > max) {
                return true;
            }

            if (iscall) timedouble = timedouble + (double) Integer.parseInt(field[4]) / (60 * 60);

            // get ID
            String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
            String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
            long senderID = Long.parseLong(sender);
            long receiverID = Long.parseLong(receiver);

            // if undirected, sender should have smller value
            if ((!direct) & senderID > receiverID) {
                long temp = senderID;
                senderID = receiverID;
                receiverID = temp;
            }
            // if(!iscall) int value = Integer.parseInt(lineSplit[4]) ;

			/*
             * Take communications as long as between the two group, not directional
			 * 
			 */
            if ((first.contains(senderID) & second.contains(receiverID)) |
                    (second.contains(senderID) & first.contains(receiverID))) {
                // first add to sender's map
                if (dyads.get(senderID) != null) {
                    if (dyads.get(senderID).get(receiverID) != null) {
                        dyads.get(senderID).get(receiverID).add(timedouble);
                    } else {
                        Info firstContact = new Info(timedouble);
                        dyads.get(senderID).put(receiverID, firstContact);
                    }
                } else {
                    Info firstContact = new Info(timedouble);
                    HashMap<Long, Info> toadd = new HashMap<Long, Info>();
                    toadd.put(receiverID, firstContact);
                    dyads.put(senderID, toadd);
                }
            }
        }
        return false;
    }

    //parse line into format of ReadInData function
    //only read in data contained the first list and add group infomation to the Info
    //return 0 if no new edge, 1 if new edge
    private static boolean ParseLine(String line, HashMap<Long, HashMap<Long, Info>> dyads, boolean iscall,
                                     ArrayList<Long> first, HashMap<Long, Integer> clusterAssignment, boolean direct, double max)
            throws ParseException {
        String[] field = line.split("\\|");
        if (field[4].charAt(0) != '-') {
            // get Time
            StringBuilder timeString = new StringBuilder();
            timeString.append(field[2]);
            timeString.append("|");
            timeString.append(field[3]);
            Date timenow = format.parse(timeString.toString());
            System.out.println(timenow);
            double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);
            if (timedouble > max) return true;

            if (iscall) timedouble = timedouble + (double) Integer.parseInt(field[4]) / (60 * 60);

            // get ID
            String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
            String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
            long senderID = Long.parseLong(sender);
            long receiverID = Long.parseLong(receiver);
            // if undirected, sender should have smller value
            if ((!direct) & senderID > receiverID) {
                long temp = senderID;
                senderID = receiverID;
                receiverID = temp;
            }
            // if(!iscall) int value = Integer.parseInt(lineSplit[4]) ;

			/*
			 * Take communications as long as between the two group, not directional
			 * 
			 */
            int groupToadd;
            if (first.contains(senderID)) {
                groupToadd = clusterAssignment.get(receiverID);
            } else if (first.contains(receiverID)) {
                groupToadd = clusterAssignment.get(senderID);
                ;
            } else {
                return (false);
            }

            if (dyads.get(senderID) != null) {
                if (dyads.get(senderID).get(receiverID) != null) {
                    dyads.get(senderID).get(receiverID).add(timedouble);
                } else {
                    Info firstContact = new Info(timedouble, groupToadd);
                    dyads.get(senderID).put(receiverID, firstContact);
                }
            } else {
                Info firstContact = new Info(timedouble, groupToadd);
                HashMap<Long, Info> toadd = new HashMap<Long, Info>();
                toadd.put(receiverID, firstContact);
                dyads.put(senderID, toadd);
            }
        }
        return false;
    }
}
