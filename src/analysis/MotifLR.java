package analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.AdjMatrix;
import data.Allmotif;


public class MotifLR {
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

    private static int bincount(String[] field, double unit) throws ParseException {
        StringBuilder timeString = new StringBuilder();
        timeString.append(field[2]);
        timeString.append("|");
        timeString.append(field[3]);
        Date timenow = format.parse(timeString.toString());
        double timedouble = (double) (timenow.getTime() - start) / 1000 / (60 * 60);
        int currentbin = (int) (timedouble / unit);
        return (currentbin);
    }

    // to run: java7lib analysis/MotifLR 60 24 anything anything 0701 0702 0703 0704 0705 0706
    // or java7lib analysis/MotifLR 60 168 anything anything MMphone
    //  minDuration(s), unit, NA, NA, data

    public static void main(String[] args) throws IOException, ParseException {
        int numMonth = args[4].equals("MMphone") ? 15 : (args.length - 4);
        //String model = args[0];
        //boolean save = Boolean.parseBoolean(args[0]);
        int maxRead = 0;
        //int lag = Integer.parseInt(args[2]);
        int minDuration = Integer.parseInt(args[0]);
        double unit = Integer.parseInt(args[1]);
        boolean readtwofile = false;
        String countPath = "/data/rwanda_anon/richardli/indiv-" + args[0] + "-" + args[1] + "-" + args[4] + "-" + args[args.length - 1] + ".txt";
        //String countPathIndiv = "/data/rwanda_anon/richardli/motif-"+ model +"-indiv"+ args[1] + "-"+ args[2] + "-"+ args[3] + "-"+ args[4] + "-" +args[args.length - 1] + ".txt";


        //BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(countPath)));


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

        // adjust for different starting point of MM
        int startbin = 0;
        if (args[4].equals("MM")) startbin = -95;
        bincount += startbin;


        AdjMatrix A0 = new AdjMatrix();
        AdjMatrix A1 = new AdjMatrix();
        AdjMatrix A1MM = new AdjMatrix();
        //Allmotif indivCount = new Allmotif();

        BufferedReader mm = new BufferedReader(new BufferedReader(new FileReader(MMpath)));
        for (String filename : dataPath) {
            BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(filename)));
            String line;

            while ((line = sc.readLine()) != null && (count <= maxRead || maxRead == 0)) {
                String[] field = line.split("\\|");
                if (field[4].charAt(0) != '-') {
                    // get Time
                    int currentbin = bincount(field, unit);
                    // if bin # is the same
                    if (currentbin == bincount) {
                        // if call duration is shorter than min
                        if (minDuration > 0 && Integer.parseInt(field[4]) < minDuration) {
                            continue;
                        }
                        // add to adjacency matrix
                        A1.add(field[0], field[1], 1, maxRead);

                        // if the bin # has been increased more than one day
                    } else if (currentbin > bincount + 1) {
                        // for now, delete the last unit read and proceed
                        A1 = new AdjMatrix(A1);
                        bincount = currentbin;
                        // reset startbin to this one
                        startbin = currentbin;
                    }
                    //  if the bin # has been increased,
                    //  if bin # is smaller than previous bin # do nothing (when starting point differs)
                    else if (currentbin == bincount + 1) {

                        System.out.println("Finish reading bin " + bincount);
                        A1.printinf();

                        if (readtwofile) {
                            String line2;
                            A1MM = new AdjMatrix(A1);

                            int currentbin2 = currentbin;
                            while ((line2 = mm.readLine()) != null && currentbin2 <= currentbin) {
                                String[] field2 = line2.split("\\|");
                                if (field2[4].charAt(0) != '-') {
                                    // get Time
                                    currentbin2 = bincount(field2, unit);
                                    if (currentbin2 == currentbin) {
                                        A1MM.add(field2[0], field2[1], 1, maxRead);
                                    }
                                }
                            }
                            A1MM.printinf();
                        }

                        // if already 2nd day beyond, print out the counts
                        if (currentbin > 1 + startbin) {

                            if (!readtwofile) {
                                A0.tieBoth(A1, currentbin, countPath);
                                //A0.tieBoth(A1, currentbin, indivCount);
                                //A0.tieyes(A1, currentbin, indivCount);
//								for(int i = 0; i < 4; i ++ ){
//									System.out.println(Arrays.toString(A0.motifcount.get(i)));															
//								}
                                System.out.println("---------------------------------------");
                                //A0.tieno(A1, currentbin, indivCount);
//								for(int i = 0; i < 4; i ++ ){
//									System.out.println(Arrays.toString(A0.motifcount.get(i)));															
//								}				
//								//out.write(A0.toString());

                            } else {
                                A1.tieBoth(A1MM, currentbin, countPath);
//								A1.tieBoth(A1MM, currentbin, indivCount);
                                //A1.tieyes(A1MM, currentbin, indivCount);
//								for(int i = 0; i < 4; i ++ ){
//									System.out.println(Arrays.toString(A1.motifcount.get(i)));															
//								}
                                System.out.println("---------------------------------------");
                                //A1.tieno(A1MM, currentbin, indivCount);
//								for(int i = 0; i < 4; i ++ ){
//									System.out.println(Arrays.toString(A1.motifcount.get(i)));															
//								}		
                                //out.write(A1.toString());

                            }

							/*
                             *    write node-specifics
							 */

                        }
                        A0.clone(A1);
                        A1 = new AdjMatrix(A0);
                        bincount = currentbin;
                    }


                }

            }
            sc.close();
        }
//		indivCount.print(countPath, bincount);
        mm.close();
        //out.close();
        //out2.close();
    }

}
