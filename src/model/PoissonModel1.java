package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

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

        double a0 = Double.parseDouble(args[0]);
        double b0 = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);
        double d = Double.parseDouble(args[3]);
        double epsilon = Double.parseDouble(args[4]);
        double a1 = Double.parseDouble(args[5]);
        double b1 = Double.parseDouble(args[6]);
        int T = Integer.parseInt(args[7]);
        int thin = Integer.parseInt(args[8]);  // not used, set to 1 for now
        int burn = Integer.parseInt(args[9]);
        int seed = Integer.parseInt(args[10]);
        int M = Integer.parseInt(args[11]);

        int npar = 11;
        String dir = args[npar + 1];
        String motifFile = dir + args[npar + 2] + ".txt";
        String motifFileN0 =  dir + args[npar + 3] + ".txt";
        String motifFileN1 = dir + args[npar + 4] + ".txt";
        String dictFile = dir + args[npar + 5] + ".txt";
        String deltaFile = dir + args[npar + 6] + ".txt";
        String saveFile_alpha = dir + "Jun2016/" + args[npar + 7] + "_alpha.txt";
        String saveFile_theta = dir + "Jun2016/" + args[npar + 7] + "_theta.txt";
        String saveFile_y = dir + "Jun2016/" + args[npar + 7] + "_y.txt";
//        String saveFile_metric = dir + "Jun2016/" + args[npar + 7] + "_metric.txt";

        boolean supervised = false;
        int maxRead = Integer.MAX_VALUE;
        if(args.length > npar + 8){
            supervised = Boolean.parseBoolean(args[npar + 8]);
        }
        if(args.length > npar + 9){
            maxRead = Integer.parseInt(args[npar + 9]);
        }

        BufferedReader mbr = new BufferedReader(new FileReader(motifFile));
        BufferedReader mbrN0 = new BufferedReader(new FileReader(motifFileN0));
        BufferedReader mbrN1 = new BufferedReader(new FileReader(motifFileN1));
        BufferedReader dbr = new BufferedReader(new FileReader(dictFile));
        BufferedReader deltabr = new BufferedReader(new FileReader(deltaFile));
        String line;

        ArrayList<int[]> motifArray = new ArrayList<int[]>();
        ArrayList<int[]> motifArrayN0 = new ArrayList<int[]>();
        ArrayList<int[]> motifArrayN1 = new ArrayList<int[]>();
        ArrayList<Integer> outcomeArray = new ArrayList<Integer>();
        ArrayList<Integer> predLabelArray = new ArrayList<Integer>();
        ArrayList<String> idArray = new ArrayList<String>();

        ArrayList<double[]> dictArray = new ArrayList<double[]>();
        double[][][][] deltaArray = new double[2][2][M][M];


        // motif file structure: [ID, Y, time, X_1, ..., X_M]
        int count = 0;
        while ((line = mbr.readLine()) != null & count < maxRead) {
            String[] field = line.split(" ");
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

            int y = (int) Double.parseDouble(field[1]);
            predLabelArray.add(y);
            y = y ==  1 ? 0:y;
            y = y == -1 ? 1:y;
            outcomeArray.add(y);
            count ++;
        }
        mbr.close();

        count = 0;
        while ((line = mbrN0.readLine()) != null & count < maxRead) {
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN0.add(motifTemp);
            count ++;
        }
        mbrN0.close();

        while ((line = mbrN1.readLine()) != null & count < maxRead) {
            String[] field = line.split(" ");
            int[] motifTemp = new int[M];
            for (int i = 0; i < M; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i + 3]);
            }
            motifArrayN1.add(motifTemp);
            count ++;
        }
        mbrN1.close();

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
                0.00001, 0.00001,
                supervised);

        model.saveAlphaCompact(saveFile_alpha);
        model.saveTheta(saveFile_theta);
        model.saveY(saveFile_y);
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
            boolean supervised
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
        for(int i = 0; i < N; i++){
            for(int p = 0; p < P; p++){
                u_now[i][p] = rngG.nextDouble(a_u, b_u);
            }
        }
        if(update_v){
            for(int j = 0; j < M; j++){
                for(int p = 0; p < P; p++){
                    v_now[j][p] = rngG.nextDouble(a_v, b_v);
                }
            }
        }
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                theta_now[j][jj] = rngG.nextDouble(a_theta, b_theta);
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
        if(supervised){
            this.beta = new double[P + 1];
        }
        System.out.println("start fitting model");

        /** ---------- Outer Loop -----------**/
        while((Math.abs(lik_change) > delta_likelihood |  beta_change > delta_para)
                & count_outer < 1000){

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
                        for(int jj = 0; jj < M; jj++){
                            int tempkey = MathUtil.orderhash(M, M, i, j, jj);
                            if(MotifStar.get(tempkey) != null){
                                tildeX[jj] = MotifStar.get(tempkey);
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
            /** Update beta with regression **/
            if(supervised){
                SupervisedModel beta_model = new SupervisedModel(u_now, this.predict_label, "IRLS");
                beta_model.fit();
                loglik_now += beta_model.get_loglik();
                double[] beta_tmp = beta_model.get_coef();
                beta_change = 0;
                for(int i = 0; i < P+1; i++){
                    beta_change += Math.abs(beta_tmp[i] - this.beta[i]);
                    this.beta[i] = beta_tmp[i];
                }
                System.out.printf("Betas: %.4f, %.4f, %.4f, %.4f, %.4f, \n",
                        this.beta[0], this.beta[1],this.beta[2],this.beta[3],this.beta[4]);
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

        if(prob_sum != 0){
            for(int l = 0; l < P + M; l++){
                prob[l] *= sum / prob_sum;
            }
        }
        return(prob);
    }


}
