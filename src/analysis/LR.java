package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import data.NeatMotif;

public class LR {
    public class Weights {
        public double w0;
        public int dim1;
        public int dim2;
        public HashMap<Integer, Double> w = new HashMap<Integer, Double>();
        public HashMap<Integer, Integer> accessTime = new HashMap<Integer, Integer>();

        public Weights() {
            w0 = 0.0;
            dim1 = 4;
            dim2 = 16;
            w = new HashMap<Integer, Double>();
            accessTime = new HashMap<Integer, Integer>();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Intercept: " + Double.toString(this.w0) + "\n");
            for (int i = 0; i < dim1; i++) {
                builder.append("Type " + Integer.toString(i) + ":\n");
                for (int j = 0; j < dim2; j++) {
                    double coeftemp = 0.0;
                    if (this.w.get(i * dim2 + j) != null) coeftemp = this.w.get(i * dim2 + j);
                    builder.append(Double.toString(coeftemp));
                    if (j != dim2 - 1) builder.append(",");
                }
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    private double computeProduct(Weights wei, HashMap<Integer, Double> instance) {
        // Fill in your code here
        double expProduct = 0.0;
        expProduct += wei.w0;
        for (int i : instance.keySet()) {
            if (wei.w.get(i) != null) expProduct += wei.w.get(i) * instance.get(i);
        }
        return Math.exp(expProduct);
    }

    private void updateWeight(Weights wei, double gradient, double step, double lambda,
                              HashMap<Integer, Double> instance) {
        wei.w0 += step * gradient;
        for (int i : instance.keySet()) {
            double preWeight = 0.0;
            if (wei.w.get(i) != null) preWeight = wei.w.get(i);
            wei.w.put(i, preWeight + step * gradient);
        }
    }

    private static void performDelayedRegularization(Weights wei, HashMap<Integer, Double> instance,
                                                     int now, double step, double lambda) {
        for (int itr : instance.keySet()) {
            if (wei.accessTime.get(itr) != null) {
                wei.w.put(itr, wei.w.get(itr) *
                        Math.pow((1 - step * lambda),
                                (now - wei.accessTime.get(itr))));
            } else {
                wei.w.put(itr, 0.0);
            }
            wei.accessTime.put(itr, now);


        }
    }

    private static void performDelayedRegularizationLast(Weights wei, int now, double step, double lambda) {

        //System.out.println(wei.accessTime);
        //System.out.println(wei.w);

        for (int itr : wei.accessTime.keySet()) {
            if (wei.accessTime.get(itr) < now) {
                wei.w.put(itr, wei.w.get(itr) *
                        Math.pow((1 - step * lambda),
                                (now - wei.accessTime.get(itr) - 1)));
            }
        }
    }

    public Weights trainReg(String trainRMSE, String data, double step, double lambda, int maxTrain, int numtest) throws NumberFormatException, IOException {


//		String yespath = "/data/rwanda_anon/richardli/motif-tie-indiv0-1-1-0701-0706.txt";
//		String nopath = "/data/rwanda_anon/richardli/motif-notie-indiv0-1-1-0701-0706.txt";


        // training on tie data
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(data)));
        String line;
        int outcome;
        int count = 0;
        LR coef = new LR();
        int thin = 100;
        Weights wei = new Weights();

        int lossTemp = 0;
        ArrayList<Double> AvgLoss = new ArrayList<Double>();
        int Xlength = wei.dim2;

        while ((line = sc.readLine()) != null && (count <= maxTrain)) {
            String[] read = line.split(",");
            HashMap<Integer, Double> features = new HashMap<Integer, Double>();
            // file in the format: sender, receiver, time, Y, type, X
            int type = Integer.parseInt(read[4]);
            outcome = Integer.parseInt(read[3]);
            for (int i = 5; i < read.length; i++) {
                /*
				 * For now do not consider type 0 motif 0 since there's no leading to no tie
				 * 
				 */
                if (type == 0 && i == 5) continue;

                if (Double.parseDouble(read[i]) > 0) features.put(i - 5 + Xlength * type, Double.parseDouble(read[i]));
            }

            double expProduct = coef.computeProduct(wei, features);
            double gradient = outcome - expProduct / (1 + expProduct);
            performDelayedRegularization(wei, features, count, step, lambda);
            int predict = (expProduct > 1) ? 1 : 0;
            //add to loss
            lossTemp += (predict == outcome) ? 0 : 1;
            if (count % thin == 0) {
                AvgLoss.add((double) lossTemp / count);
            }
            coef.updateWeight(wei, gradient, step, lambda, features);
            count++;
        }
        performDelayedRegularizationLast(wei, count, step, lambda);
        if (numtest > 0) {
            int countzero = 0;
            int countright = 0;
            while ((line = sc.readLine()) != null && (count <= maxTrain + numtest)) {
                String[] read = line.split(",");
                HashMap<Integer, Double> features = new HashMap<Integer, Double>();
                // file in the format: sender, receiver, time, Y, type, X
                int type = Integer.parseInt(read[4]);
                outcome = Integer.parseInt(read[3]);
                for (int i = 5; i < read.length; i++) {
					/*
					 * For now do not consider type 0 motif 0 since there's no leading to no tie
					 * 
					 */
                    if (type == 0 && i == 5) continue;

                    if (Double.parseDouble(read[i]) > 0)
                        features.put(i - 5 + Xlength * type, Double.parseDouble(read[i]));
                }

                double expProduct = coef.computeProduct(wei, features);
                int predict = (expProduct > 1) ? 1 : 0;
                // NP-loss
                //add to loss
                countright += (predict == outcome) ? 1 : 0;
                if (outcome == 0) countzero++;
                count++;
            }
            System.out.println("Correct predicting :  " + Integer.toString(countright));
            System.out.println("Baseline predicting:  " + Integer.toString(countzero));
        }
        sc.close();
//		// train data on no tie 
//		sc = new BufferedReader(new BufferedReader(new FileReader(nopath)));
//		outcome = 0;
//	    count = 0;
//		while((line = sc.readLine())!= null && (count <= maxTrain)){
//			String[] read = line.split(",");
//			HashMap<Integer, Double> features = new HashMap<Integer, Double>();
//			for(int i= 0; i < read.length; i++){
//				features.put(i, Double.parseDouble(read[i]));
//			}
//			double expProduct = coef.computeProduct(features);
//			double gradient = outcome - expProduct/(1 + expProduct);
//			int predict = (expProduct > 1) ? 1 : 0;
//			//add to loss
//			lossTemp += (predict == outcome) ? 0 : 1;
//			if(count % thin == 0){
//				AvgLoss.add((double) lossTemp/count);
//			}
//			coef.updateWeight(gradient, step, lambda, features);		
//			count++;
//		}
//		sc.close();

        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(trainRMSE)));
        out.write(AvgLoss.toString().replace("[", "").replace("]", ""));
        out.close();
        return wei;
    }

    public int[] TestReg(Weights wei, String data, double step, double lambda, int start, int maxTest) throws NumberFormatException, IOException {


//		String yespath = "/data/rwanda_anon/richardli/motif-tie-indiv0-1-1-0701-0706.txt";
//		String nopath = "/data/rwanda_anon/richardli/motif-notie-indiv0-1-1-0701-0706.txt";


        // training on tie data
        BufferedReader sc = new BufferedReader(new BufferedReader(new FileReader(data)));
        String line;
        int outcome;
        int count = 0;
        LR coef = new LR();
        int thin = 100;

        int lossTemp = 0;
        int countzero = 0;
        ArrayList<Double> AvgLoss = new ArrayList<Double>();
        int Xlength = wei.dim2;

        while ((line = sc.readLine()) != null && (count <= maxTest + start)) {
            if (count < start) continue;
            String[] read = line.split(",");
            HashMap<Integer, Double> features = new HashMap<Integer, Double>();
            // file in the format: sender, receiver, time, Y, type, X
            int type = Integer.parseInt(read[4]);
            outcome = Integer.parseInt(read[3]);
            for (int i = 5; i < read.length; i++) {
				/*
				 * For now do not consider type 0 motif 0 since there's no leading to no tie
				 * 
				 */
                if (type == 0 && i == 5) continue;

                if (Double.parseDouble(read[i]) > 0) features.put(i - 5 + Xlength * type, Double.parseDouble(read[i]));
            }

            double expProduct = coef.computeProduct(wei, features);
            int predict = (expProduct > 1) ? 1 : 0;
            // NP-loss
            //add to loss
            lossTemp += (predict == outcome) ? 0 : 1;
            if (outcome == 0) countzero++;

            // square error loss
            // lossTemp += (expProduct / (1 + expProduct) - outcome) * (expProduct / (1 + expProduct) - outcome);
            //if(count % thin == 0){
            AvgLoss.add((double) lossTemp / count);
            //}
            count++;
        }
        sc.close();
        int[] countright = new int[2];
        countright[0] = lossTemp;
        countright[1] = countzero;
        return (countright);
    }


    /*
     *  To run:
     *  java7lib analysis/LR indiv-24-1-60-0701-0701.txt 0.01 0.01 5
     */
    public static void main(String[] args) throws NumberFormatException, IOException {
        String train = "/data/rwanda_anon/richardli/trainRmse" + args[3] + "M.txt";
        String data = "/data/rwanda_anon/richardli/" + args[0];
        String deg = data.replace("indiv", "deg");

        double step = Double.parseDouble(args[1]);
        double lambda = Double.parseDouble(args[2]);
        int lag = Integer.parseInt(args[3]);

        int countfeature = 64 * lag;
        NeatMotif neat = new NeatMotif(countfeature);
        int maxPeriod = 0;
        neat.read(data, deg, maxPeriod);
        //OnlineLogisticRegression lr = new OnlineLogisticRegression(2, 64 * lag, new L1());
        //neat.trainLR(lr);


//		LR trainLR = new LR();
//		Weights wei = trainLR.trainReg(train, data, step, lambda, maxTrain, 1000000);
//		data = args[0];
//		//int[] error = trainLR.TestReg(wei, data, step, lambda, maxTrain, 1000000);
//		System.out.println(wei.toString());
//		//System.out.println(Arrays.toString(error));

    }
}
