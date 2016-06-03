package model;

/**
 * Created by zehangli on 12/14/15.
 */

import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.Beta;
import cern.jet.random.tdouble.Binomial;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;
import util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class SparseCodingLN {

    /* Delta sampler */
    private double getLik(
            double[][] lambda, double[][] delta, int[][] motif, double[][] motif2,
            int J, int N, int M){
        double l = 0;
        for(int i = 0; i < N; i++){
            double tmp = 0;
            for(int j = 0; j < M; j++){
                tmp += delta[J][j] * motif2[i][j];
            }
            l += tmp * motif[i][J] - Math.exp(tmp) * lambda[i][J];
        }
        return(l);
    }

    public double[][] DeltaSampler(
            double[][] lambda, double[][] delta, int[][] motif, double[][] motif2, double sd,
            int N, int M, Random rand, Normal rngN){

        double[][] deltaNew = new double[M][M];
        for(int j = 0; j < M; j++){
            double current_lik = getLik(lambda, delta, motif, motif2, j, N, M);

            double test = Math.log(rand.nextDouble()) + current_lik;

            // set up bracket
            double phi = rand.nextDouble() * 2 * Math.PI;
            double phi_min = phi - 2 * Math.PI;
            double phi_max = phi;
            double[] priorSample = new double[M];
            for(int jj = 0; jj < M; jj++){
                priorSample[jj] = rngN.nextDouble(0, sd);
            }
            double new_lik = 0;
            int count = 0;
            while(true){
                double max_dist = 0;
                for(int jj = 0; jj < M; jj++){
                    deltaNew[jj][j] = delta[jj][j] * Math.cos(phi)+ priorSample[jj] * Math.sin(phi);
                    double diff = Math.abs(deltaNew[jj][j] - delta[jj][j]);
                    if(diff > max_dist) max_dist = diff;
                }
                new_lik = getLik(lambda, deltaNew, motif, motif2, j, N, M);
                if(new_lik >= test){
                    // update delta
                    for(int jj = 0; jj < M; jj++){
                        delta[jj][j] = deltaNew[jj][j];
                    }
                    break;
                }else if(max_dist < 1e-4 | count > 100){
                    break;
                }else{
                    if(phi > 0) phi_max = phi;
                    if(phi < 0) phi_min = phi;
                    phi = rand.nextDouble() * (phi_max - phi_min) + phi_min;
                    count ++;
                }
            }
        }
        return(deltaNew);
    }

    /*
     * Add Log Normal term for network correction
     */
    public double[][] main(
            double a, double b, double c, double d, double epsilon,
            int T, int thin, int burn, int seed,
            int[][] motif, double[][] dict,
            double[][] motif2, double sd, String deltafile) throws IOException {


        int N = motif.length;
        int M = motif[0].length;
        int P = dict.length;

        DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
        Random rand = new Random();
        Normal rngN =new Normal(0.0, 1.0, rngEngine);
        Gamma rngG=new Gamma(1.0, 1.0, rngEngine);
        Binomial rngB = new Binomial(1, 0.5, rngEngine);
        Beta rngBe = new Beta(1, 1, rngEngine);
        Multinomial rngM = new Multinomial(P);



        // sparse representation of xsub
        // in the form of Map<person, Map<
        MapWrapper[][] xsub = new MapWrapper[N][M];
        //int[][][] xsub = new int[N][M][P];
        double[][] alpha_now = new double[N][P];
        int[][] z_now = new int[N][P];
        double[] gamma_now = new double[P];
        //double[][] gamma_out = new double[T-burn][P];
        double[][] alpha_out = new double[N][P];
        // double[][] alpha_out = new double[N][P];
        int n_report = T / 100;
        if(T < 100) n_report = 1;

		/*
		 * Initialization
		 */
        for(int i = 0; i < N; i++){
            for(int p = 0; p < P; p++){
                alpha_now[i][p] = rngG.nextDouble(2,2);
            }
        }
        for(int p = 0; p < P; p++){
            gamma_now[p] = rand.nextDouble();
        }

        /* add parameters for log normal error term */
        double[][] W = new double[N][M];
        double[][] delta_now = new double[M][M];
        WriteArray wa = new WriteArray();
        Boolean first = true;

        // initialization
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                delta_now[j][jj] = rngN.nextDouble(0, sd);
            }
        }
        for(int i = 0; i < N; i++){
            for(int j = 0; j < M; j++){
                W[i][j] = Math.exp(VectorUtil.CrossProd(delta_now[j], motif2[i]));
//                if(W[i][j] > 10000){
//                    System.out.println(Arrays.toString(motif2[i]));
//                }
            }
        }


		/*
		 * Start iteration
		 */
        long start = System.currentTimeMillis();

        for(int t = 0; t<T; t++){
			/*
			 * sample xsub
			 */
            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    xsub[i][j] = new MapWrapper();
                    double[] prob = new double[P];
                    double prob_sum = 0;
                    for(int p = 0; p < P; p++){
                        prob[p] = alpha_now[i][p] * dict[p][j];
                        prob_sum += prob[p];
                    }
                    /* add log normal correction */
                    prob_sum *= W[i][j];
                    if(prob_sum != 0){
                        //xsub[i][j] = rngM.Sample(prob, prob_sum, motif[i][j]);
                        xsub[i][j].addAll(rngM.SampleAsList(prob, prob_sum, motif[i][j]));

                    }
                }
            }
            System.out.println("finish xsub");
			/*
			 * sample z
			 */
            for(int i=0; i < N; i++){
                for(int p=0; p<P; p++){
                    double term = gamma_now[p] * Math.pow(b, a)/ MathUtil.gamma(a)
                            * Math.pow(alpha_now[i][p], a-1)*Math.exp(-1 * b * alpha_now[i][p]);
                    double prob = term / (term + (1 - gamma_now[p]) * epsilon * Math.exp(-1 * epsilon *
                            alpha_now[i][p]));

                    // for numerical stability
                    if(Double.isNaN(prob) & term == 0){
                        prob = 0.5;
                    }

                    if(prob * (1 - prob) == 0){
                        z_now[i][p] = (int) prob;
                    }else{
                        z_now[i][p] = rngB.nextInt(1, prob);
                    }
                }
            }
            System.out.println("finish Z");
			/*
			 * sample gamma
			 */
            int[] zsum = VectorUtil.apply_sum(2, z_now);
            for(int p = 0; p < P; p++){
                gamma_now[p] = rngBe.nextDouble(c + zsum[p],  d + N - zsum[p]);
            }

            System.out.println("finish gamma");

            //System.out.println(Arrays.toString(gamma_now));
			/*
			 * sample alpha
			 */
            double temp = 0;
            for(int i = 0; i < N; i++){
                for(int p = 0; p < P; p++){
                    double shape = VectorUtil.pick13_sum(i, p, xsub) + (a-1) * z_now[i][p] + 1;
                    // double rate = VectorUtil.vectorSum(dict[p]) + b * z_now[i][p] + epsilon * (1-z_now[i][p]);
                    // update for log normal correction
                    double rate = VectorUtil.CrossProd(W[i], dict[p]) + b * z_now[i][p] + epsilon * (1-z_now[i][p]);
                    alpha_now[i][p] = rngG.nextDouble(shape,rate);
                    temp += alpha_now[i][p];
                }
            }
            System.out.printf("alpha mean %10f\n", temp/N/P);

            System.out.println("finish alpha");

            //  sample new delta matrix
            double[][] lambda = new double[N][M];
            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    for(int p = 0; p < P; p++){
                        lambda[i][j] += alpha_now[i][p] * dict[p][j];
                    }
                }
            }
            delta_now = DeltaSampler(lambda, delta_now, motif, motif2, sd, N, M, rand, rngN);
            System.out.println("finish delta");

            /*
             * update W
             */
            double tempW = 0;
            for(int i = 0; i < N; i++){
                for(int j = 0; j < M; j++){
                    W[i][j] = Math.exp(VectorUtil.CrossProd(delta_now[j], motif2[i]));
//                    System.out.printf("W %1f\n", W[i][j]);
                    tempW += W[i][j];
                }
            }
            System.out.printf("W mean %10f\n", tempW/N/M);
//            System.out.println("finish W");

            System.out.printf(".");
            if(t % n_report == 0){
                double spar = VectorUtil.vectorSum(zsum) / (N * P + 0.0);
                long now   = System.currentTimeMillis();
                System.out.printf("\n-- %d --", t);
                System.out.printf("Time -- %.2fmin --", (double) (now - start)/1000/60);
                System.out.printf("Sparse -- %.6f\n", spar);
            }
            if(t > burn){
                //gamma_out[t-burn] = gamma_now.clone();
                if(t % thin == 0){
                    System.out.printf("Itr %d sampled\n", t);
//                    VectorUtil.fillselected(alpha_out, t - burn + 1, alpha_now);
                    alpha_out = VectorUtil.Add(alpha_out, alpha_now);
                    wa.write(delta_now, deltafile, !first);
                    first = false;
                }
            }
        }
        alpha_out = VectorUtil.Multi(alpha_out, 1/(T-burn+0.0));
//		return(gamma_out);
        return(alpha_out);
    }
}
