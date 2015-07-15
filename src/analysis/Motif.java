package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import data.AdjMat;

// to run: java7lib analysis/Motif tie 24 1 1 0701 0702 0703 0704 0705 0706
// maxRead, sampleProb, minDuration(s)

public class Motif {
    public static SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
    // start and end date
    public static final long start;
    public static final double endT;

    static {
        long startTemp = 0;
        double endTTemp = 0.0;
        try {
            //startTemp = format.parse("061001|00:00:00").getTime();
            startTemp = format.parse("070101|00:00:00").getTime();
            long end = format.parse("080401|00:00:00").getTime();
            endTTemp = (double) (end - startTemp) / 1000 / (60 * 60);
        } catch (ParseException e) {
            System.out.println("Error Initializing time");
        }
        start = startTemp;
        endT = endTTemp;
    }

    public static void main(String[] args) throws IOException, ParseException {
        int numMonth = args[4].equals("MMphone") ? 15 : (args.length - 4);
        String model = args[0];
        int maxRead = 0;
        int sampleProb = Integer.parseInt(args[2]);
        int minDuration = Integer.parseInt(args[3]);
        double unit = Integer.parseInt(args[1]);
        boolean readtwofile = false;
        String countPath = "/data/rwanda_anon/richardli/motif-" + model + args[1] + "-" + args[2] + "-" + args[3] + "-" + args[4] + "-" + args[args.length - 1] + ".txt";
        String countPathIndiv = "/data/rwanda_anon/richardli/motif-" + model + "-indiv" + args[1] + "-" + args[2] + "-" + args[3] + "-" + args[4] + "-" + args[args.length - 1] + ".txt";

        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(countPath)));
        BufferedWriter out2 = new BufferedWriter(new BufferedWriter(new FileWriter(countPathIndiv)));


        String[] dataPath = new String[numMonth];
        String MMpath = "/data/rwanda_anon/CDR/me2u.ANON.all.txt";

        if (args[4].equals("MM")) {
            dataPath = new String[1];
            dataPath[0] = "/data/rwanda_anon/CDR/me2u.ANON.all.txt";
            numMonth = 1;
        } else if (args[4].equals("MMphone")) {
            readtwofile = true;
            int month = 0;
            for (int i = 1; i <= 9; i++) {
                dataPath[month] = "/data/rwanda_anon/CDR/070" + Integer.toString(i) + "-Call.pai.sordate.txt";
                month++;
            }
            for (int i = 10; i <= 12; i++) {
                dataPath[month] = "/data/rwanda_anon/CDR/07" + Integer.toString(i) + "-Call.pai.sordate.txt";
                month++;
            }
            for (int i = 1; i < 4; i++) {
                dataPath[month] = "/data/rwanda_anon/CDR/080" + Integer.toString(i) + "-Call.pai.sordate.txt";
                month++;
            }
        } else {
            for (int i = 0; i < numMonth; i++) {
                dataPath[i] = "/data/rwanda_anon/CDR/" + args[i + 4] + "-Call.pai.sordate.txt";
            }
        }

        // start reading data
        int count = 0;
        int bincount = 0;
        AdjMat A0 = new AdjMat();
        AdjMat A1 = new AdjMat();
        AdjMat A1MM = new AdjMat();

        BufferedReader mm = new BufferedReader(new BufferedReader(new FileReader(MMpath)));
        for (String filename : dataPath) {
            BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
            String line;

            while ((line = sc.readLine()) != null && (count <= maxRead || maxRead == 0)) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    // get Time
                    StringBuilder timeString = new StringBuilder();
                    timeString.append(field[2]);
                    timeString.append("|");
                    timeString.append(field[3]);
                    Date timenow = format.parse(timeString.toString());
                    double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);
                    int currentbin = (int) (timedouble / unit);


                    if (currentbin == bincount + 1) {
                        System.out.println("Finish reading bin " + bincount);
                        System.out.println(timenow);
                        A1.printinf();

                        if (readtwofile) {
                            String line2;
                            A1MM = new AdjMat(A1);

                            int currentbin2 = currentbin;
                            while ((line2 = mm.readLine()) != null && currentbin2 <= currentbin) {
                                String[] field2 = line2.split("\\|");
                                if (field2[4].charAt(0) != '-') {
                                    // get Time
                                    StringBuilder timeString2 = new StringBuilder();
                                    timeString2.append(field2[2]);
                                    timeString2.append("|");
                                    timeString2.append(field2[3]);
                                    Date timenow2 = format.parse(timeString2.toString());
                                    double timedouble2 = (double) (timenow2.getTime() - start) / 1000 / (60 * 60);
                                    currentbin2 = (int) (timedouble2 / unit);
                                    if (currentbin2 < currentbin) {
                                        continue;
                                    }
                                    // add to MM adj matrix
                                    String sender2 = field2[0].replace("L", "0").replace("F", "1").replace("N", "2");
                                    String receiver2 = field2[1].replace("L", "0").replace("F", "1").replace("N", "2");
                                    long senderID2 = Long.parseLong(sender2);
                                    long receiverID2 = Long.parseLong(receiver2);
                                    if (senderID2 != receiverID2) {
                                        A1MM.add(senderID2, receiverID2, sampleProb, maxRead);
                                    }
                                }
                            }
                            A1MM.printinf();
                        }
                        // if already 2nd day beyond, print out the counts
                        if (currentbin > 1) {

                            int[][] motifcount = new int[2][6];
                            if (!readtwofile) {
                                if (model.equals("tie")) {
                                    motifcount = A0.twotie(A1);
                                } else {
                                    motifcount = A0.notwotie(A1);
                                }
                            } else {
                                if (model.equals("tie")) {
                                    motifcount = A1.twotie(A1MM);
                                } else {
                                    motifcount = A1.notwotie(A1MM);
                                }
                            }
                            System.out.println(Arrays.toString(motifcount[0]));
                            System.out.println(Arrays.toString(motifcount[1]));
                            if (!readtwofile) {
                                // write to summary
                                StringBuilder lineOut = new StringBuilder();
                                lineOut.append(A1.V);
                                lineOut.append(",");
                                lineOut.append(A1.E);
                                lineOut.append(Arrays.toString(motifcount[0]).replace("[", ",").replace("]", ""));
                                lineOut.append(Arrays.toString(motifcount[1]).replace("[", ",").replace("]", ","));
                                lineOut.append(Integer.toString(A0.norm));
                                lineOut.append("\n");
                                out.write(lineOut.toString());
                            } else {
                                StringBuilder lineOut = new StringBuilder();
                                lineOut.append(A1MM.V);
                                lineOut.append(",");
                                lineOut.append(A1MM.E);
                                lineOut.append(Arrays.toString(motifcount[0]).replace("[", ",").replace("]", ""));
                                lineOut.append(Arrays.toString(motifcount[1]).replace("[", ",").replace("]", ","));
                                lineOut.append(Integer.toString(A0.norm));
                                lineOut.append("\n");
                                out.write(lineOut.toString());
                            }
//							// write node-specifics
                            for (int[][] countNode : A0.detail.values()) {
                                out2.write(Arrays.toString(countNode[0]).replace("[", "").replace("]", ""));
                                out2.write(Arrays.toString(countNode[1]).replace("[", ",").replace("]", "\n"));
                            }
                        }
                        A0.clone(A1);
                        A1 = new AdjMat(A0);
                        bincount = currentbin;
                    }
                    // get ID
                    String sender = field[0].replace("L", "0").replace("F", "1").replace("N", "2");
                    String receiver = field[1].replace("L", "0").replace("F", "1").replace("N", "2");
                    long senderID = Long.parseLong(sender);
                    long receiverID = Long.parseLong(receiver);

                    // if call duration is shorter than min
                    if (minDuration > 0 && Integer.parseInt(field[4]) < minDuration) {
                        continue;
                    }

                    // add to adjacency matrix
                    if (senderID != receiverID) {
                        A1.add(senderID, receiverID, sampleProb, maxRead);
                    }


                }

            }
            sc.close();
        }
        mm.close();
        out.close();
        out2.close();
    }

}
