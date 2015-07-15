package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Rmse {
    HashMap<Integer, ArrayList<Double>> pred = new HashMap<Integer, ArrayList<Double>>();
    HashMap<Integer, ArrayList<Double>> rmse = new HashMap<Integer, ArrayList<Double>>();
    HashMap<Integer, Integer[]> confusion = new HashMap<Integer, Integer[]>();
    HashMap<Integer, Long[]> pairIndex = new HashMap<Integer, Long[]>();
    HashMap<String, Integer> groupCode = new HashMap<String, Integer>();
    double mrmse = 0.0;
    int total = 0;
    int trpos = 0;
    int trneg = 0;
    int falpos = 0;
    int falneg = 0;
    double bic = 0.0;

    private void GetPred(HashMap<Integer, Integer[]> hist, HashMap<Integer, Double[]> lambda, HashMap<Integer, Double[][]> qmat) {
        for (int i : hist.keySet()) {
            if (lambda.get(i) == null) {
                continue;
            }
            Integer[] temphist = hist.get(i);
            Double[] templambda = lambda.get(i);
            Double[][] tempqmat = qmat.get(i);
            Integer[] tempconf = new Integer[4];
            for (int j = 0; j < tempconf.length; j++) {
                tempconf[j] = 0;
            }

            int maxlag = templambda.length;
            ArrayList<Double> temprmse = new ArrayList<Double>();
            ArrayList<Double> temppred = new ArrayList<Double>();

            // get squared error list for one pair
            for (int j = maxlag; j < temphist.length; j++) {
                double prob = 0.0;
                for (int k = 0; k < maxlag; k++) {
                    // the previous day to be considered is j-k-1
                    // we are not computing the prob of observing current one,
                    //   we are computing the prob of seeing a communicationg, so add 1.
                    int which = temphist[j - k - 1] * 2 + 1;
                    prob += templambda[k] * tempqmat[k][which];
                }
                temppred.add(prob);
                temprmse.add((prob - temphist[j]) * (prob - temphist[j]));
                if (prob < 0.5 & temphist[j] == 0) {
                    tempconf[0]++;
                    this.trneg++;
                }
                if (prob < 0.5 & temphist[j] == 1) {
                    tempconf[1]++;
                    this.falneg++;
                }
                if (prob > 0.5 & temphist[j] == 0) {
                    tempconf[2]++;
                    this.falpos++;
                }
                if (prob > 0.5 & temphist[j] == 1) {
                    tempconf[3]++;
                    this.trpos++;
                }
                this.total++;
                this.mrmse += (prob - temphist[j]) * (prob - temphist[j]);
            }
            // put into all lists
            this.pred.put(i, temppred);
            this.rmse.put(i, temprmse);
            this.confusion.put(i, tempconf);
        }
        this.mrmse /= (this.total + 0.0);
    }


    // input same as MTD personal
    public Rmse(String[] args) throws IOException {
        int numMonth = args.length - 4;
        if (args[3].equals("MM")) {
            numMonth = 1;
        }
        int maxlag = Integer.parseInt(args[0]);
        int maxPeriod = Integer.parseInt(args[2]);
        double unit = Double.parseDouble(args[args.length - 1]);

        String lambdaPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "lag" + args[0] + "min" + args[1] + "-lambda.txt";
        String qmatPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "lag" + args[0] + "min" + args[1] + "-qmat.txt";
        String likPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "lag" + args[0] + "min" + args[1] + "-lik.txt";
        String histpath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "lag" + args[0] + "min" + args[1] + "-history.txt";
        if (unit != 24) {
            lambdaPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-lambda.txt";
            qmatPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-qmat.txt";
            likPath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-lik.txt";
            histpath = "/data/rwanda_anon/richardli/mtd/personal/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-history.txt";
        }
        HashMap<Integer, Integer[]> hist = new HashMap<Integer, Integer[]>();
        HashMap<Integer, Double[]> lambda = new HashMap<Integer, Double[]>();
        HashMap<Integer, Double[][]> qmat = new HashMap<Integer, Double[][]>();


        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(histpath)));
        String line;
        Integer groupID = 0;
        while ((line = sc.readLine()) != null) {
            String[] field = line.replaceAll("\\s", "").split(",");

            if (field.length > 2) {
                Long[] pair = new Long[2];
                pair[0] = Long.parseLong(field[0]);
                pair[1] = Long.parseLong(field[1]);
                pairIndex.put(groupID, pair);
                groupCode.put(field[0] + field[1], groupID);

                // save history
                Integer[] temphist = new Integer[maxPeriod + 1];
                for (int i = 0; i < maxPeriod + 1; i++) {
                    temphist[i] = Integer.parseInt(field[i + 2]);
                }
                hist.put(groupID, temphist);

                groupID++;
            }
        }
        sc.close();

        sc = new BufferedReader(new BufferedReader(new FileReader(lambdaPath)));
        while ((line = sc.readLine()) != null) {
            String[] field = line.replaceAll("\\s", "").split(",");
            if (field.length > 3) {
                groupID = groupCode.get(field[0] + field[1]);
                // save history
                Double[] templambda = new Double[maxlag];
                for (int i = 0; i < maxlag; i++) {
                    templambda[i] = Double.parseDouble(field[i + 4]);
                }
                lambda.put(groupID, templambda);
            }
        }
        sc.close();

        sc = new BufferedReader(new BufferedReader(new FileReader(qmatPath)));
        while ((line = sc.readLine()) != null) {
            String[] field = line.replaceAll("\\s", "").split(",");
            if (field.length == 2) {
                groupID = groupCode.get(field[0] + field[1]);
                if (lambda.get(groupID) == null) {
                    continue;
                }
                Double[][] tempqmat = new Double[maxlag][4];
                // read next line
                for (int j = 0; j < maxlag; j++) {
                    line = sc.readLine();
                    field = line.replaceAll("\\s", "").split(",");
                    // save history
                    for (int i = 0; i < 4; i++) {
                        tempqmat[j][i] = Double.parseDouble(field[i]);
                    }
                }
                qmat.put(groupID, tempqmat);
            }
        }
        sc.close();

        GetPred(hist, lambda, qmat);

        sc = new BufferedReader(new BufferedReader(new FileReader(likPath)));
        while ((line = sc.readLine()) != null) {
            String[] field = line.replaceAll("\\s", "").split(",");
            if (field.length == 2) {
                this.bic += Double.parseDouble(field[0]);
            }
            this.bic += (3 * maxlag - 1) * this.total * Math.log(maxPeriod - maxlag + 1);
        }
        sc.close();


    }


    public static void main(String[] args) throws IOException {
        Rmse thisone = new Rmse(args);
        int numMonth = args.length - 4;
        if (args[3].equals("MM")) {
            numMonth = 1;
        }
        double unit = Double.parseDouble(args[args.length - 1]);

        String rmsepath = "/data/rwanda_anon/richardli/mtd/personal-pred/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-rmse.txt";
        String predpath = "/data/rwanda_anon/richardli/mtd/personal-pred/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-pred.txt";
        String confpath = "/data/rwanda_anon/richardli/mtd/personal-pred/personal-" + args[3] + "-" + args[numMonth + 2] + "-" + unit + "hr-" + "lag" + args[0] + "min" + args[1] + "-conf.txt";


        BufferedWriter sc = new BufferedWriter(new FileWriter(new File(rmsepath), false));
        for (int i : thisone.rmse.keySet()) {
            Long[] ids = thisone.pairIndex.get(i);
            sc.write(Long.toString(ids[0]) + "," + Long.toString(ids[1]) + "," +
                    thisone.rmse.get(i).toString().replace("[", "").replace("]", "") + "\n");
        }
        sc.close();

        sc = new BufferedWriter(new FileWriter(new File(predpath), false));
        for (int i : thisone.pred.keySet()) {
            Long[] ids = thisone.pairIndex.get(i);
            sc.write(Long.toString(ids[0]) + "," + Long.toString(ids[1]) + "," +
                    thisone.pred.get(i).toString().replace("[", "").replace("]", "") + "\n");
        }
        sc.close();
        sc = new BufferedWriter(new FileWriter(new File(confpath), false));
        for (int i : thisone.pred.keySet()) {
            Long[] ids = thisone.pairIndex.get(i);
            sc.write(Long.toString(ids[0]) + "," + Long.toString(ids[1]) + "," +
                    Arrays.toString(thisone.confusion.get(i)).replace("[", "").replace("]", "") + "\n");
        }
        sc.close();
        System.out.println("Total Dyads:      " + Integer.toString(thisone.rmse.size()));
        System.out.println("Total Prediction: " + Integer.toString(thisone.total));
        System.out.println("Mean RMSE:        " + Double.toString(thisone.mrmse));
        System.out.println("False Positive:   " + Double.toString(thisone.falpos / (thisone.total + 0.0)));
        System.out.println("False Negative:   " + Double.toString(thisone.falneg / (thisone.total + 0.0)));
        System.out.println("BIC:			  " + Double.toString(thisone.bic));

    }
}
