package model;

import java.io.*;
import java.util.*;

import util.MathUtil;
import util.VectorUtil;
import util.MapWrapper;
import util.Multinomial;

import cern.jet.random.tdouble.Beta;
import cern.jet.random.tdouble.Binomial;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;

/**
 * Created by zehangli on 4/9/16.
 */

public class PoissonModel1{

    /** variables to save **/
    double[][][] alpha_out;
    double[][][] dict_out;
    double[][][] theta_out;
    int[] y;
    int[] predict_label;
    double[] beta;
    String[] id;
    double[] mse;
    double[] mae;
    double[] mmae;
    double[] loglik;
    int N;

    public PoissonModel1(ArrayList<String> id, ArrayList<Integer> outcome, int N){
        this.y = new int[N];
        this.id = new String[N];
        this.N = N;
        for(int i = 0; i < N; i++){
            this.y[i] = outcome.get(i);
            this.id[i] = id.get(i);
        }
    }

    public PoissonModel1(ArrayList<String> id, ArrayList<Integer> outcome, int N,
                         ArrayList<Integer> predLabelArray){
        this.y = new int[N];
        this.predict_label = new int[N];
        this.id = new String[N];
        this.N = N;
        for(int i = 0; i < N; i++){
            this.y[i] = outcome.get(i);
            this.id[i] = id.get(i);
            this.predict_label[i] = predLabelArray.get(i);
        }
    }


    public void saveMetric(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < this.mse.length; i++) {
            bw.write(this.mse[i] + ", " + this.mae[i] + "," + this.mmae[i] + "\n");
        }
        bw.close();
    }

    public void saveY(String file) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for(int i = 0; i < this.y.length; i++){
            bw.write(this.id[i] + ", " + this.predict_label[i] + "\n");
        }
        bw.close();
    }

    public void saveAlpha(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for(int t = 0; t < this.alpha_out.length; t++){
            for(int i = 0; i < this.alpha_out[t].length; i++){
                bw.write(Arrays.toString(this.alpha_out[t][i]).replace("[", "").replace("]", ""));
                bw.write("\n");
            }
        }
        bw.close();
    }
    public void saveAlphaCompact(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        int T = this.alpha_out.length;
        for(int i = 0; i < this.alpha_out[0].length; i++){
            for(int j = 0; j < this.alpha_out[0][i].length; j++){
                this.alpha_out[0][i][j] /= (T + 0.0);
                for(int t = 1; t < T; t++){
                    this.alpha_out[0][i][j] += this.alpha_out[t][i][j] / (T + 0.0);
                }
            }
            bw.write(Arrays.toString(this.alpha_out[0][i]).replace("[", "").replace("]", ""));
            bw.write("\n");
        }
        bw.close();
    }

    public void saveBeta(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(Arrays.toString(this.beta).replace("[", "").replace("]", ""));
        bw.close();
    }

    public void saveTheta(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for(int t = 0; t < this.theta_out.length; t++){
            for(int i = 0; i < this.theta_out[t].length; i++){
                bw.write(Arrays.toString(this.theta_out[t][i]).replace("[", "").replace("]", ""));
                bw.write("\n");
            }
        }
        bw.close();
    }

    /**
     *  Fit the Poisson decomposition model
     *  dict0 Delta test
     * @param args
     * @throws IOException
     */
    /* javalib -Xms10g model/PoissonModel1 0.3 0.3 1 1 0.0001 2 5 1000 1 500 12345 120
    /data/rwanda_anon/richardli/MotifwithNeighbour/ 0809week0 0809No_week0 0809Yes_week0 dict0 Delta
    0809week0_vb_samp true 100000
     */

    public static void main(String[] args) throws IOException {

        int counter = 0;
        double a0 = Double.parseDouble(args[counter]); counter++;
        double b0 = Double.parseDouble(args[counter]);counter++;
//        double c = Double.parseDouble(args[counter]);counter++;
//        double d = Double.parseDouble(args[counter]);counter++;
//        double epsilon = Double.parseDouble(args[counter]);counter++;
        double a1 = Double.parseDouble(args[counter]);counter++;
        double b1 = Double.parseDouble(args[counter]);counter++;
//        int T = Integer.parseInt(args[counter]);counter++;
//        int thin = Integer.parseInt(args[counter]); counter++; // not used, set to 1 for now
//        int burn = Integer.parseInt(args[counter]);counter++;
        int seed = Integer.parseInt(args[counter]);counter++;
        int M = Integer.parseInt(args[counter]);counter++;

        String dir = args[counter];counter++;
        String start = args[counter]; counter++;
        String weekcount = args[counter]; counter++;
        String motifFile = dir + start + weekcount + ".txt";
        String motifFileN0 =  dir + start + "No_" + weekcount + ".txt";
        String motifFileN1 = dir + start + "Yes_" + weekcount + ".txt";

        String weekcount_test = args[counter]; counter++;
        String motifFile_test = dir + start + weekcount_test + ".txt";
        String motifFileN0_test =  dir + start + "No_" + weekcount_test + ".txt";
        String motifFileN1_test = dir + start + "Yes_" + weekcount_test + ".txt";

        String dictFile = dir + args[counter] + ".txt";counter++;
        String deltaFile = dir + args[counter] + ".txt"; counter++;

        // files to save
        String post = args[counter]; counter++;
        String saveFile_alpha = dir + "Jun2016/" + start + weekcount + post  + "_alpha.txt";
        String saveFile_theta = dir + "Jun2016/" + start + weekcount + post + "_theta.txt";
        String saveFile_y = dir + "Jun2016/" + start + weekcount + post + "_y.txt";
        String saveFile_beta = dir + "Jun2016/" + start + weekcount + post + "_beta.txt";
//        String saveFile_metric = dir + "Jun2016/" + args[npar + 7] + "_metric.txt";

        boolean supervised = false;
        int maxRead = Integer.MAX_VALUE;
        if(args.length > counter){
            supervised = Boolean.parseBoolean(args[counter]);
        }
        if(args.length > counter + 1){
            maxRead = Integer.parseInt(args[counter + 1]);
        }

        BufferedReader mbr = new BufferedReader(new FileReader(motifFile));
        BufferedReader mbrN0 = new BufferedReader(new FileReader(motifFileN0));
        BufferedReader mbrN1 = new BufferedReader(new FileReader(motifFileN1));
        BufferedReader mbr_test = new BufferedReader(new FileReader(motifFile_test));
        BufferedReader mbrN0_test = new BufferedReader(new FileReader(motifFileN0_test));
        BufferedReader mbrN1_test = new BufferedReader(new FileReader(motifFileN1_test));
        BufferedReader dbr = new BufferedReader(new FileReader(dictFile));
        String line;

        ArrayList<int[]> motifArray = new ArrayList<int[]>();
        ArrayList<int[]> motifArrayN0 = new ArrayList<int[]>();
        ArrayList<int[]> motifArrayN1 = new ArrayList<int[]>();
        ArrayList<Integer> outcomeArray = new ArrayList<Integer>();
        ArrayList<Integer> predLabelArray = new ArrayList<Integer>();
        ArrayList<String> idArray = new ArrayList<String>();

        ArrayList<double[]> dictArray = new ArrayList<double[]>();
        double[][][][] deltaArray = new double[2][2][M][M];

        /** Motif count for current period **/
        // motif file structure: [ID, Y, time, X_1, ..., X_M]
        int count = 0;
        // since we don't care people who are already MM user,
        // maybe we don't have to model their counts?
        HashSet<Integer> already_user = new HashSet<Integer>();

        while ((line = mbr.readLine()) != null & motifArray.size() < maxRead) {
            String[] field = line.split(" ");
            int y = (int) Double.parseDouble(field[1]);

            // since we don't care people who are already MM user,
            // maybe we don't have to model their counts?
            if(y == -1){
                already_user.add(count);
                count++;
                continue;
            }

            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArray.add(motifTemp);

            idArray.add(field[0]);

            /** Notice:
             * Y = -1  --> currently label is 1
             * Y =  0  --> currently label is 0
             * Y =  1  --> currently label is 0
             * **/

            predLabelArray.add(y);
            y = y ==  1 ? 0:y;
            y = y == -1 ? 1:y;
            outcomeArray.add(y);
            count ++;
        }
        mbr.close();
        System.out.printf("%d non-motif users processed\n", motifArray.size());

        count = 0;
        while ((line = mbrN0.readLine()) != null & motifArrayN0.size() < maxRead) {
            if(already_user.contains(count)){
                count++;
                continue;
            }
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN0.add(motifTemp);
            count ++;
        }
        mbrN0.close();
        System.out.printf("%d non-motif users' 0-neighbour processed\n", motifArrayN0.size());

        count = 0;
        while ((line = mbrN1.readLine()) != null & motifArrayN1.size() < maxRead) {
            if(already_user.contains(count)){
                count++;
                continue;
            }
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN1.add(motifTemp);
            count ++;
        }
        mbrN1.close();
        System.out.printf("%d non-motif users' 1-neighbour  processed\n", motifArrayN0.size());



        /** Motif count for next period **/
        int test_start = motifArray.size();
        HashSet<Integer> already_user_test = new HashSet<Integer>();

        while ((line = mbr_test.readLine()) != null & motifArray.size() < maxRead + test_start) {
            String[] field = line.split(" ");
            int y = (int) Double.parseDouble(field[1]);

            // since we don't care people who are already MM user,
            // maybe we don't have to model their counts?
            if(y == -1){
                already_user_test.add(count);
                count++;
                continue;
            }

            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArray.add(motifTemp);

            idArray.add(field[0]);

            /** Notice:
             * Y = -1  --> currently label is 1
             * Y =  0  --> currently label is 0
             * Y =  1  --> currently label is 0
             * **/

            predLabelArray.add(y);
            y = y ==  1 ? 0:y;
            y = y == -1 ? 1:y;
            outcomeArray.add(y);
            count ++;
        }
        int test_end = motifArray.size() - 1;
        mbr_test.close();
        System.out.printf("%d non-motif users processed for next period\n", motifArray.size());

        count = test_start;
        while ((line = mbrN0_test.readLine()) != null & motifArrayN0.size() < maxRead + test_start) {
            if(already_user_test.contains(count)){
                count++;
                continue;
            }
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN0.add(motifTemp);
            count++;
        }
        mbrN0_test.close();
        System.out.printf("%d non-motif users' 0-neighbour processed for next period\n", motifArrayN0.size());

        count = test_start;
        while ((line = mbrN1_test.readLine()) != null & motifArrayN1.size() < maxRead + test_start) {
            if(already_user_test.contains(count)){
                count++;
                continue;
            }
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN1.add(motifTemp);
            count++;
        }
        mbrN1_test.close();
        System.out.printf("%d non-motif users' 1-neighbour  processed\n", motifArrayN0.size());

        // dict file structure: [D_i,1, ..., D_i,M]
        while ((line = dbr.readLine()) != null) {
            String[] field = line.split(" ");
            double[] dictTemp = new double[M];
            for (int i = 0; i < M; i++) {
                dictTemp[i] = (int) Double.parseDouble(field[i]);
            }
            dictArray.add(dictTemp);
        }
        dbr.close();

        boolean update_theta = true;
        try {
            BufferedReader deltabr = new BufferedReader(new FileReader(deltaFile));
            // delta file structure: [y0, y1, m0 + 1, m1 + 1]
            while ((line = deltabr.readLine()) != null) {
                String[] field = line.split(" ");
                int y0 = Integer.parseInt(field[0]);
                int y1 = Integer.parseInt(field[1]);
                int m0 = Integer.parseInt(field[2]) - 1;
                int m1 = Integer.parseInt(field[3]) - 1;

                deltaArray[y0][y1][m0][m1] = 1;
            }
            deltabr.close();
        }catch (Exception e) {
            update_theta = false;
        }


        int[][] motif = new int[motifArray.size()][M];
        int[][][] motifNeighbour = new int[2][motifArray.size()][M];
        double[][] dict = new double[dictArray.size()][M];

        for(int i = 0; i < motifArray.size(); i++){
            for(int j = 0; j < M; j++){
                motif[i][j] = motifArray.get(i)[j];
                motifNeighbour[0][i][j] = motifArrayN0.get(i)[j];
                motifNeighbour[1][i][j] = motifArrayN0.get(i)[j];
            }
        }

        for(int i = 0; i < dictArray.size(); i++){
            for(int j = 0; j < M; j++){
                dict[i][j] = dictArray.get(i)[j];
            }
        }

        PoissonModel1 model = new PoissonModel1(idArray, outcomeArray, idArray.size(), predLabelArray);

//        model.fitUnsup_Poisson_additive(a0, b0, c, d, epsilon, a1, b1,
//                T, thin, burn, seed,
//                motif, motifNeighbour,
//                dict, deltaArray, model.y, verbose);
        model.Poisson_additive_VB(a0, b0, a1, b1,
                1, 1, false, seed,
                motif, motifNeighbour,
                dict, deltaArray, model.y,
                0.001, 0.001,
                supervised,
                test_start, test_end,
                update_theta);

        model.saveAlphaCompact(saveFile_alpha);
        model.saveTheta(saveFile_theta);
        model.saveY(saveFile_y);
        model.saveBeta(saveFile_beta);
//        model.saveMetric(saveFile_metric);
    }


    /**
     *
     * @param a0 Gamma parameter for slab of \alpha
     * @param b0 Gamma parameter for spike of \alpha
     * @param c Beta parameter for \gamma_p
     * @param d Beta parameter for \gamma_p
     * @param epsilon Controls spike of \alpha (small value)
     * @param a1 Gamma parameter for \theta
     * @param b1 Gamma parameter for \theta
     * @param T Length of chain
     * @param thin thinning of chain
     * @param burn burning of chain
     * @param seed seed of chain
     * @param motif N by M matrix of counts
     * @param motifNeighbour K by N by M array of counts (K: number of node type)
     * @param dict P by M matrix of dict/design matrix
     * @param delta  K by K by N by M array of binary values
     * @param y length N vector of outcome
     * @param verbose boolean whether to print more information at each iteration
     * @return
     */
    public void fitUnsup_Poisson_additive(
            double a0, double b0,
            double c, double d, double epsilon,
            double a1, double b1,
            int T, int thin, int burn, int seed,
            int[][] motif, int[][][] motifNeighbour,
            double[][] dict,
            double[][][][] delta,
            int[] y,
            boolean verbose
            ){


        int N = motif.length;
        int M = motif[0].length;
        int P = dict.length;
        int K = motifNeighbour.length;

        DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
        Random rand = new Random();
        //Normal rngN=new Normal(0.0, 1.0, rngEngine);
        Gamma rngG=new Gamma(1.0, 1.0, rngEngine);
        Binomial rngB = new Binomial(1, 0.5, rngEngine);
        Beta rngBe = new Beta(1, 1, rngEngine);
        Multinomial rngM = new Multinomial(P);


        // sparse representation of xsub
        /* in the form of [person][sub_index](Map<slot_index, count>)
         * i.e., xsub[i][j] = Map<{1, 1}, {2, 10}, ...>
         *     means motif[i][j] gets decomposed into 1 in slot 1, 10 in slot 2, ...
         *     total number of slot: P + M
         */
        MapWrapper[][] xsub = new MapWrapper[N][M];

        // parameters
        double[][] alpha_now = new double[N][P];
        double[][] theta_now = new double[M][M];
        int[][] z_now = new int[N][P];
        double[] gamma_now = new double[P];



        this.mse = new double[T];
        this.mae = new double[T];
        this.mmae = new double[T];
        this.alpha_out = new double[T - burn][N][P];
        this.theta_out = new double[T - burn][M][M];
        int n_report = T / 100;
        if(T < 100) n_report = 1;

		/*
		 * Initialization
		 */
        for(int i = 0; i < N; i++){
            for(int p = 0; p < P; p++){
                alpha_now[i][p] = rngG.nextDouble(a0, b0);
            }
        }
        for(int p = 0; p < P; p++){
            gamma_now[p] = rand.nextDouble();
        }
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                theta_now[j][jj] = rngG.nextDouble(a1, b1);
            }
        }


		/*
		 * Start iteration
		 */
        long start = System.currentTimeMillis();

        /** try memory intensive approach to reduce computational cost **/
        double[][][] MotifStar = new double[N][M][M];
        double[][] sum_i_MotifStar = new double[M][M];
        for(int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                for(int jj = 0; jj < M; jj ++){
                    for(int k = 0; k < K ; k++){
                        MotifStar[i][j][jj] += motifNeighbour[k][i][jj] * delta[y[i]][k][j][jj];
                        sum_i_MotifStar[j][jj] += MotifStar[i][j][jj];
                    }
                }
            }
        }

        int[] motifSum = new int[N];
        int zerocount = 0;
        for(int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                motifSum[i] += motif[i][j];
            }
            if(motifSum[i] == 0){zerocount ++;}
        }


        for(int t = 0; t<T; t++){
			/*
			 * sample xsub:
			 * [person][sub_index](Map<slot_index, count>)
             * i.e., xsub[i][j] = Map<{1, 1}, {2, 10}, ...>
             *     means motif[i][j] gets decomposed into 1 in slot 1, 10 in slot 2, ...
             *     total number of slot: P + M
			 */
            double alpha_verbose1 = 0;
            double alpha_verbose2 = 0;
            int[][] sum_i_xsub = new int[M][M];

            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    xsub[i][j] = new MapWrapper();
                    double[] prob = new double[P + M];
                    double prob_sum = 0;
                    for(int p = 0; p < P; p++){
                        prob[p] = alpha_now[i][p] * dict[p][j];
                        prob_sum += prob[p];
                    }
                    for(int jj = 0; jj < M; jj++){
//                        for(int k = 0; k < K; k++){
//                            prob[P + jj] +=  motifNeighbour[k][i][jj] * delta[y[i]][k][j][jj] * theta_now[j][jj];
//                        }
                        prob[P + jj] += theta_now[j][jj] * MotifStar[i][j][jj];
                        prob_sum += prob[P + jj];
                    }

                    /** calculate SAE and SSE **/
                    this.mse[t] += (motif[i][j] - prob_sum) * (motif[i][j] - prob_sum) / (N * M + 0.0);
                    this.mae[t] += Math.abs(motif[i][j] - prob_sum) / (N * M + 0.0);
                    if(motifSum[i] > 0) {
                        this.mmae[t] += Math.abs(motif[i][j] - prob_sum) / (motifSum[i] + 0.0)
                                / ((N - zerocount) + 0.0);
                    }

                    if(prob_sum != 0){
                        xsub[i][j].addAll(rngM.SampleAsList(prob, prob_sum, motif[i][j]));
                        sum_i_xsub[j] = xsub[i][j].getVector(P, P + M - 1);
                        for(int key : xsub[i][j].x.keySet()){
                            if(key < P){
                                alpha_verbose1 += xsub[i][j].get(key);
                            }else{
                                alpha_verbose2 += xsub[i][j].get(key);
                            }
                        }
                    }
                }
            }
            if(verbose) {
                System.out.print("finish xsub: ");
                System.out.printf("%.2fmin MeanSum_dict_comp %.6f MeanSum_soc_comp %.6f\n",
                                (double) (System.currentTimeMillis() - start) / 1000 / 60,
                                (alpha_verbose1 + 0.0) / (N * M  + 0.0),
                                (alpha_verbose2 + 0.0) / (N * M  + 0.0));
            }

			/*
			 * sample z
			 */
            double zsum_verbose = 0;
            for(int i=0; i < N; i++){
                for(int p=0; p<P; p++){
                    double firstTerm = gamma_now[p] * Math.pow(b0, a0)/MathUtil.gamma(a0)
                            * Math.pow(alpha_now[i][p], a0-1)*Math.exp(-1 * b0 * alpha_now[i][p]);
                    double prob = firstTerm / (firstTerm + (1 - gamma_now[p]) * Math.exp(-1 / epsilon *
                            alpha_now[i][p]) / epsilon);
                    if(prob * (1 - prob) == 0){
                        z_now[i][p] = (int) prob;
                    }else{
                        z_now[i][p] = rngB.nextInt(1, prob);
                    }
                    zsum_verbose += z_now[i][p];
                }
            }
            if(verbose) {
                System.out.print("finish Z: ");
                System.out.printf("%.2fmin, Sparsity %.6f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (zsum_verbose + 0.0) / (N * P + 0.0));
            }

			/*
			 * sample gamma
			 */
            int[] zsum = VectorUtil.apply_sum(2, z_now);
            double gamma_verbose = 0;
            for(int p = 0; p < P; p++){
                gamma_now[p] = rngBe.nextDouble(c + zsum[p],  d + N - zsum[p]);
                gamma_verbose += gamma_now[p];
            }


            if(verbose) {
                System.out.print("finish gamma: ");
                System.out.printf("%.2fmin, Mean %.2f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (gamma_verbose + 0.0) / (P + 0.0));
            }

            /*
			 * sample alpha
			 */
            double alpha_verbose = 0;
            for(int i = 0; i < N; i++){
                for(int p = 0; p < P; p++){
                    // use only index 0 to P-1, as they are the slots for dict decomposition
                    double shape = VectorUtil.pick13_sum(i, p, xsub, 0, P - 1) + (a0-1) * z_now[i][p] + 1;
                    double rate = VectorUtil.vectorSum(dict[p]) + b0 * z_now[i][p] + epsilon * (1-z_now[i][p]);
                    alpha_now[i][p] = rngG.nextDouble(shape,rate);
                    alpha_verbose += alpha_now[i][p];
                }
            }
            if(verbose) {
                System.out.print("finish alpha: ");
                System.out.printf("%.2fmin, Mean %.2f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (alpha_verbose + 0.0) / (P * N + 0.0));
            }

            /*
             * sample theta
             */
            double theta_verbose = 0;
            for(int j = 0; j < M; j++){
                for(int jj = 0; jj < M; jj++){
                    double sum_i_x = (double) sum_i_xsub[j][jj];
                    double sum_i_x_tilde = sum_i_MotifStar[j][jj];
                    theta_now[j][jj] = rngG.nextDouble(sum_i_x + a1, sum_i_x_tilde + b1);
                    theta_verbose += theta_now[j][jj];
                }
            }
            if(verbose) {
                System.out.print("finish theta: ");
                System.out.printf("%.2fmin, Mean %.6f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (theta_verbose + 0.0) / (P * N + 0.0));
            }

            System.out.printf("-- Itr %d MSE: %.6f MAE: %.6f MMAE %.6f\n", t, this.mse[t], this.mae[t], this.mmae[t]);

            if(t % n_report == 0){
                double spar = VectorUtil.vectorSum(zsum) / (N * P + 0.0);
                long now   = System.currentTimeMillis();
                System.out.printf("\n-- %d --", t);
                System.out.printf("Time -- %.2fmin --", (double) (now - start)/1000/60);
                System.out.printf("Sparse -- %.6f\n", spar);
            }
            if(t >= burn){
                //if(t % thin == 0){
                if(t % 1 == 0){
                    System.out.printf("Itr %d sampled\n", t);
                    /** save alpha **/
                    for(int i = 0; i < N; i++){
                        for(int j = 0; j < P; j++){
                            this.alpha_out[t - burn][i][j] = alpha_now[i][j];
                        }
                    }
                    /** save delta **/
                    for(int i = 0; i < M; i++){
                        for(int j = 0; j < M; j++){
                            this.theta_out[t - burn][i][j] = theta_now[i][j];
                        }
                    }

                }
            }
        }
    }

    /**
     *
     * @param a_u Gamma parameter for \alpha
     * @param b_u Gamma parameter for \alpha
     * @param a_theta Gamma parameter for \theta
     * @param b_theta Gamma parameter for \theta
     * @param a_v Gamma parameter for d
     * @param b_v Gamma parameter for d
     * @param update_v whether to update v (dict)
     * @param seed seed of chain
     * @param motif N by M matrix of counts
     * @param motifNeighbour K by N by M array of counts (K: number of node type)
     * @param dict P by M matrix of dict/design matrix
     * @param delta  K by K by N by M array of binary values
     * @param y length N vector of outcome
     * @param delta_likelihood max likelihood change for convergence
     * @param delta_para  max parameter change for convergence
     * @param supervised boolean whether to perform supervised version
     * @return
     */
    public void Poisson_additive_VB(
            double a_u, double b_u,
            double a_theta, double b_theta,
            double a_v, double b_v, boolean update_v,
            int seed,
            int[][] motif, int[][][] motifNeighbour,
            double[][] dict,
            double[][][][] delta,
            int[] y,
            double delta_likelihood,
            double delta_para,
            boolean supervised,
            int test_start, int test_end,
            boolean update_theta
    ){


        int N = motif.length;
        int M = motif[0].length;
        int P = dict.length;
        int K = motifNeighbour.length;

        DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
        Random rand = new Random();
        Gamma rngG=new Gamma(1.0, 1.0, rngEngine);
        Binomial rngB = new Binomial(1, 0.5, rngEngine);
        Beta rngBe = new Beta(1, 1, rngEngine);
        Multinomial rngM = new Multinomial(P);

        /** parameters: E(u), E(v), E(theta) **/
        double[][] u_now = new double[N][P];
        double[][] v_now = new double[M][P];
        for(int p = 0; p < P; p++){
            for(int j = 0; j < M; j++){
                v_now[j][p] = dict[p][j];
            }
        }
        double[][] theta_now = new double[M][M];

        /** variational parameters **/
        double[][] lambda_u_a = new double[N][P];
        double[][] lambda_u_b = new double[N][P];
        double[][] lambda_v_a = new double[M][P];
        double[][] lambda_v_b = new double[M][P];
        double[][] lambda_theta_a = new double[M][M];
        double[][] lambda_theta_b = new double[M][M];


        /** diagnostics and output parameters **/
        this.mse = new double[1];
        this.mae = new double[1];
        this.mmae = new double[1];
        this.loglik = new double[1];
        this.alpha_out = new double[1][N][P];
        this.theta_out = new double[1][M][M];


		/** Random Initialization **/
        double au_nonneg = a_u == 0 ? 1 : a_u;
        double bu_nonneg = b_u == 0 ? 1 : b_u;
        double av_nonneg = a_v == 0 ? 1 : a_v;
        double bv_nonneg = b_v == 0 ? 1 : b_v;
        double ath_nonneg = a_theta == 0 ? 1 : a_theta;
        double bth_nonneg = a_theta == 0 ? 1 : b_theta;
        for(int i = 0; i < N; i++){
            for(int p = 0; p < P; p++){
                u_now[i][p] = rngG.nextDouble(au_nonneg, bu_nonneg);
            }
        }
        if(update_v){
            for(int j = 0; j < M; j++){
                for(int p = 0; p < P; p++){
                    v_now[j][p] = rngG.nextDouble(av_nonneg, bv_nonneg);
                }
            }
        }
        if(update_theta){
            for(int j = 0; j < M; j++){
                for(int jj = 0; jj < M; jj++){
                    theta_now[j][jj] = rngG.nextDouble(ath_nonneg, bth_nonneg);
                }
            }
        }

        /** Pre-process: memory intensive approach to reduce computational cost
         *  After processing MotifStar,
         *  Motif_{ij} ~ u'v + sum_{j'} theta_{jj'} * MotifStar_{ijj'}
         **/
//        double[][][] MotifStar = new double[N][M][M];
//        double[][] sum_i_MotifStar = new double[M][M];
//        for(int i = 0; i < N; i++){
//            for(int j = 0; j < M; j++){
//                for(int jj = 0; jj < M; jj ++){
//                    for(int k = 0; k < K ; k++){
//                        MotifStar[i][j][jj] += motifNeighbour[k][i][jj] * delta[y[i]][k][j][jj];
//                        sum_i_MotifStar[j][jj] += MotifStar[i][j][jj];
//                    }
//                }
//            }
//        }

        HashMap<Integer, Double> MotifStar = new HashMap<Integer, Double>();
        double[][] sum_i_MotifStar = new double[M][M];
        if(update_theta){
            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    for(int jj = 0; jj < M; jj ++){
                        for(int k = 0; k < K ; k++){
                            double temp =  motifNeighbour[k][i][jj] * delta[y[i]][k][j][jj];
                            if(temp > 0){
                                int keytemp = MathUtil.orderhash(M, M, i, j, jj);
                                if(MotifStar.get(keytemp) != null){
                                    MotifStar.put(keytemp, MotifStar.get(keytemp) + temp);
                                }else{
                                    MotifStar.put(keytemp, temp);
                                }
                            }
                            sum_i_MotifStar[j][jj] += temp;
                        }
                    }
                }
            }
        }



        /** Start main algorithm **/
        long start = System.currentTimeMillis();

        /** Initialize lambda_theta_b  **/
        for(int j  = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                lambda_theta_b[j][jj] = b_theta + sum_i_MotifStar[j][jj];
            }
        }

        double lik_change = Double.MAX_VALUE;
        double beta_change = 0;
        int count_outer = 0;
        double loglik_last = -1 * Double.MAX_VALUE;
        this.beta = new double[P + 1];
        System.out.println("start fitting model");

        /** ---------- Outer Loop -----------**/
        while((Math.abs(lik_change) > delta_likelihood |  beta_change > delta_para)
                & count_outer < 500){

            /** Set global row-wise latent variable lambda^{u, a} **/
            for (double[] row: lambda_u_a) Arrays.fill(row, a_u);
            for (double[] row: lambda_u_b) Arrays.fill(row, b_u);

            int count_local = 0;
            /** Update each column j **/
            for(int j = 0; j < M; j++){
                double[][] lambda_u_a_local = new double[N][P];

                double para_change = Double.MAX_VALUE;
                double count_inner = 0;

                /** ---------- Inner Loop -----------**/
                while(para_change > delta_para & count_inner < 100){
                    para_change = 0;

                    /** Within j-th column: Initialize local lambda^{u, a}, lambda^{v, a}, lambda^{theta, a} **/
                    for (double[] row: lambda_u_a_local) Arrays.fill(row, 0);
                    for (double[] row: lambda_v_a) Arrays.fill(row, a_v);
                    for (double[] row: lambda_theta_a) Arrays.fill(row, a_theta);

                    /** Within j-th column: Set lambda^{v, b}  **/
                    if(update_v){
                        double[] sum_i_E_v = new double[P];
                        for(int k = 0; k < P; k++){
                            for(int i = 0; i < N; i++){
                                sum_i_E_v[k] += u_now[i][k];
                            }
                        }
                        for(int k = 0; k < P; k++){
                            lambda_v_b[j][k] = b_u + sum_i_E_v[k];
                        }
                    }
                    /** Within j-th column: Update for each row i **/
                    for(int i = 0; i < N; i++){
                        /** Calculate expectations for xsub **/
                        double[] tildeX = new double[M];
                        if(update_theta) {
                            for (int jj = 0; jj < M; jj++) {
                                int tempkey = MathUtil.orderhash(M, M, i, j, jj);
                                if (MotifStar.get(tempkey) != null) {
                                    tildeX[jj] = MotifStar.get(tempkey);
                                }
                            }
                        }
                        double[] xsub = calculate_Xsub_Mean(P, M, u_now[i], v_now[j], theta_now[j],
                                tildeX, motif[i][j]);

                        /** Update lambda^{u, a}_local, lambda^{v, a}, lambda^{theta, a} **/
                        for(int k = 0; k < P; k++){
                            lambda_u_a_local[i][k] += xsub[k];
                        }
                        if(update_v){
                            for(int k = 0; k < P; k++) {
                                lambda_v_a[j][k] += xsub[k];
                            }
                        }
                        for(int jj = 0; jj < M; jj++){
                            lambda_theta_a[j][jj] += xsub[jj + P];
                        }
                    }
                    /** Within j-th column: Update E(v) and E(theta) **/
                    if(update_v){
                        for(int k = 0; k < P; k++) {
                            double tmp = lambda_v_a[j][k] / lambda_v_b[j][k];
                            para_change += Math.abs(tmp - v_now[j][k]) / (P + 0.0);
                            v_now[j][k] = tmp;
                        }
                    }
                    for(int jj = 0; jj < M; jj++){
                        double tmp = lambda_theta_a[j][jj] / lambda_theta_b[j][jj];
                        para_change += Math.abs(tmp - theta_now[j][jj]) / (M + 0.0);
                        theta_now[j][jj] = tmp;
                    }
                    /** within j-th column: update local beta **/
                    if(supervised) {
                        // TODO: update beta_k | u, v, theta ?
                    }

                    count_inner ++;
                }
                count_local += count_inner;

                /** Within j-th column: Update global lambda^{u, a} **/
                for(int i = 0; i < N; i++) {
                    for (int k = 0; k < P; k++) {
                        lambda_u_a[i][k] += lambda_u_a_local[i][k];
                        lambda_u_b[i][k] += v_now[j][k];
                    }
                }
            }

            /** update E(u) **/
            for(int i = 0; i < N; i++) {
                for (int k = 0; k < P; k++) {
                    u_now[i][k] = lambda_u_a[i][k] / lambda_u_b[i][k];
                }
            }

            /** Calculate likelihood **/
            double loglik_now = 0;
            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    double[] tildeX = new double[M];
                    for(int jj = 0; jj < M; jj++){
                        int tempkey = MathUtil.orderhash(M, M, i, j, jj);
                        if(MotifStar.get(tempkey) != null){
                            tildeX[jj] = MotifStar.get(tempkey);
                        }
                    }
                    loglik_now += get_Pois_loglik(motif[i][j],
                                                u_now[i], v_now[j],
                                                theta_now[j], tildeX);
                }
            }
            // TODO: update global beta?
            if(supervised) {
                // TODO: update beta | u, v, theta ?

            }
            lik_change = loglik_now - loglik_last;
            loglik_last = loglik_now;
            count_outer ++;
            System.out.printf("Iteration: %d, total local update: %d\n", count_outer, count_local);
            System.out.printf("Current log likelihood: %.6f.  Change: %.6f ",
                    loglik_now, lik_change);
            long now   = System.currentTimeMillis();
            System.out.printf("Time elapsed: %.2fmin\n", (double) (now - start)/1000/60);
        }

        /** Update beta with regression only after convergence if unsupervised **/
        if(!supervised){
            SupervisedModel beta_model = new SupervisedModel(u_now, this.predict_label, "IRLS", test_start,
                    test_end);
            beta_model.fit();
            double[] beta_tmp = beta_model.get_coef();
            for(int i = 0; i < P+1; i++){
                this.beta[i] = beta_tmp[i];
            }
            beta_model.test(this.beta);
            System.out.printf("Betas: %.4f, %.4f, %.4f, %.4f, %.4f, \n",
                    this.beta[0], this.beta[1], this.beta[2], this.beta[3], this.beta[4]);
            System.out.printf("Cutoff 0: TP: %d, FP: %d, TN: %d, FN: %d\n",
                    beta_model.tp, beta_model.fp, beta_model.tn,
                    beta_model.fn);
            System.out.printf("Cutoff E: TP: %d, FP: %d, TN: %d, FN: %d\n",
                    beta_model.tp2, beta_model.fp2, beta_model.tn2,
                    beta_model.fn2);
        }
        /** Set output **/
        for(int i = 0; i < N; i++){
            for(int k = 0; k < P; k++){
                this.alpha_out[0][i][k] = u_now[i][k];
            }
        }
        if(update_v){
            for(int j = 0; j < M; j++){
                for(int k = 0; k < P; k++){
                    this.dict_out[0][j][k] = v_now[j][k];
                }
            }
        }
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                this.theta_out[0][j][jj] = theta_now[j][jj];
            }
        }

    }


    public double get_Pois_loglik(int x, double[] u, double[] v, double[] u2, double[] v2){
        double mean = 0;
        for(int i = 0; i < u.length; i++){mean += u[i] * v[i];}
        for(int i = 0; i < u2.length; i++){mean += u2[i] * v2[i];}

        double loglik = 0;
        if(mean == 0) {mean = 0.0001;}

        loglik -=  org.apache.commons.math3.special.Gamma.logGamma(x + 1);
        loglik += x * Math.log(mean);
        loglik -= mean;
        return(loglik);
    }
    public double[] calculate_Xsub_Mean(int P, int M, double[] u_now_i, double[] v_now_j,
                                        double[] theta_now_j,  double[] X_i_j, double sum){
        double[] prob = new double[P + M];
        double prob_sum = 0;
        for(int p = 0; p < P; p++){
            prob[p] = u_now_i[p] * v_now_j[p];
            prob_sum += prob[p];
        }
        for(int jj = 0; jj < M; jj++){
            prob[P + jj] += theta_now_j[jj] * X_i_j[jj];
            prob_sum += prob[P + jj];
        }

        if(prob_sum == 0){
            for(int l = 0; l < P + M; l++){
                prob[l] = sum / (P + M + 0.0);
            }
        }else{
            for(int l = 0; l < P + M; l++){
                prob[l] *= sum / prob_sum;
            }
        }
        return(prob);
    }


}
