package model;

import java.util.Random;

import util.Dimsum;
import util.Gamma_func;
import util.MapWrapper;
import util.Multinomial;
import util.VecOperation;
import cern.jet.random.tdouble.Beta;
import cern.jet.random.tdouble.Binomial;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;

public class SparseCoding {
	public double[][] main(
			double a, double b, double c, double d, double epsilon,
			int T, int thin, int burn, int seed, 
			int[][] motif, double[][] dict){
		//	public static void main(String[] args){
		//		double a = 2.0;
		//		double b = 2.0;
		//		double c = 1.0;
		//		double d = 1.0; 
		//		double epsilon = 100;
		//		int T = 10;
		//		int thin = 1;
		//		int burn = 0;
		//		int seed = 1; 
		//		int[][] motif = new int[50][10];
		//		double[][] dict = new double[50][10];
		//		for(int i = 0; i < motif.length; i++){
		//			for(int j = 0; j < motif[0].length; j++){
		//				motif[i][j] = i * 10 + j ;
		//				dict[i][j] = i/(j+0.0) + Math.log(j + 5);
		//			}
		//		}

		int N = motif.length;
		int M = motif[0].length;
		int P = dict.length;

		DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
		Random rand = new Random();		
		//Normal rngN=new Normal(0.0, 1.0, rngEngine);
		Gamma rngG=new Gamma(1.0, 1.0, rngEngine);
		Binomial rngB = new Binomial(1, 0.5, rngEngine);
		Beta rngBe = new Beta(1, 1, rngEngine);
		Multinomial rngM = new Multinomial(P);
		Dimsum dsum = new Dimsum();
		Gamma_func ga = new Gamma_func();

		// sparse representation of xsub
		// in the form of Map<person, Map< 
		MapWrapper[][] xsub = new MapWrapper[N][M];
		//int[][][] xsub = new int[N][M][P];
		double[][] alpha_now = new double[N][P];
		int[][] z_now = new int[N][P];
		double[] gamma_now = new double[P];
		//double[][] gamma_out = new double[T-burn][P];
		//double[][][] alpha_out = new double[T-burn][N][P];
		double[][] alpha_out = new double[N][P];
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
					if(prob_sum != 0){	    			 
						//xsub[i][j] = rngM.Sample(prob, prob_sum, motif[i][j]);
						xsub[i][j].addAll(rngM.SampleAsList(prob, prob_sum, motif[i][j]));

					}	
				}
			}
			/*
			 * sample z
			 */
			for(int i=0; i < N; i++){
				for(int p=0; p<P; p++){
					double term = gamma_now[p] * Math.pow(b, a)/ga.gamma(a) 
							* Math.pow(alpha_now[i][p], a-1)*Math.exp(-1 * b * alpha_now[i][p]);
					double prob = term / (term + (1 - gamma_now[p]) * Math.exp(-1 * epsilon * alpha_now[i][p]));
					if(prob * (1 - prob) == 0){
						z_now[i][p] = (int) prob;
					}else{
						z_now[i][p] = rngB.nextInt(1, prob);
					}
				}
			}
			/*
			 * sample gamma
			 */
			int[] zsum = dsum.apply_sum(2, z_now);
			for(int p = 0; p < P; p++){
				gamma_now[p] = rngBe.nextDouble(c + zsum[p],  d + N - zsum[p]);
			}
			//System.out.println(Arrays.toString(gamma_now));
			/*
			 * sample alpha
			 */
			for(int i = 0; i < N; i++){
				for(int p = 0; p < P; p++){		
					double shape = dsum.pick13_sum(i, p, xsub) + (a-1) * z_now[i][p] + 1;
					double rate = dsum.sum(dict[p]) + b * z_now[i][p] + epsilon * (1-z_now[i][p]);
					alpha_now[i][p] = rngG.nextDouble(shape,rate);
				}
			}

			System.out.printf(".");
			if(t % n_report == 0){
				double spar = dsum.sum(zsum) / (N * P + 0.0);
				long now   = System.currentTimeMillis();
				System.out.printf("\n-- %d --", t);
				System.out.printf("Time -- %.2fmin --", (double) (now - start)/1000/60);
				System.out.printf("Sparse -- %.6f\n", spar);
			}
			if(t > burn){
				//gamma_out[t-burn] = gamma_now.clone();
				if(t % thin == 0){
					System.out.printf("Itr %d sampled\n", t);
					alpha_out = VecOperation.Add(alpha_out, alpha_now);
				}
			}
		}
		alpha_out = VecOperation.Multi(alpha_out, 1/(T-burn+0.0));
		//return(gamma_out);
		return(alpha_out);
	}
}
