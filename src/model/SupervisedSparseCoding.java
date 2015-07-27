package model;

import java.util.Random;

import util.Dimsum;
import util.Gamma_func;
import util.MapWrapper;
import util.Multinomial;
import util.VecOperation;
import util.WriteArray;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import cern.jet.random.tdouble.Beta;
import cern.jet.random.tdouble.Binomial;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;

	public class SupervisedSparseCoding {

		public double[][] main(
				double a, double b, double c, double d, double epsilon,
				int T, int thin, int burn, int seed, 
				int[][] motif, double[][] dict, int[] y, double tau_a, double tau_b, 
				double stepHM, String path) throws Exception{
//				public static void main(String[] args) throws IOException{
//					double a = 2.0;
//					double b = 2.0;
//					double c = 1.0;
//					double d = 1.0; 
//					double epsilon = 100;
//					int T = 100;
//					int thin = 1;
//					int burn = 0;
//					int seed = 1; 
//					int[][] motif = new int[50][10];
//					double[][] dict = new double[20][10];
//					for(int i = 0; i < motif.length; i++){
//						for(int j = 0; j < motif[0].length; j++){
//							motif[i][j] = i * 10 + j ;
//						}
//					}
//					for(int i = 0; i < dict.length; i++){
//						for(int j = 0; j < dict[0].length; j++){		
//							dict[i][j] = i/(j+0.1) + Math.log(j + 5);
//						}
//					}
//					int[] y = new int[50];
//					for(int i = 0; i < 15; i++) y[i] = 1;
//					double tau_a = 1.0;
//					double tau_b = 1.0;
//					double stepHM = 1.0;
					
			int N = motif.length;
			int M = motif[0].length;
			int P = dict.length;

			DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
			Random rand = new Random();	
			// Normal uses sd not variance
			Normal rngN=new Normal(0.0, 1.0, rngEngine);
			Gamma rngG=new Gamma(1.0, 1.0, rngEngine);
			Binomial rngB = new Binomial(1, 0.5, rngEngine);
			Beta rngBe = new Beta(1, 1, rngEngine);
			Multinomial rngM = new Multinomial(P);
			Dimsum dsum = new Dimsum();
			Gamma_func ga = new Gamma_func();

			// sparse representation of xsub
			// in the form of Map<person, Map< 
			MapWrapper[][] xsub = new MapWrapper[N][M];
			int[][] z_now = new int[N][P];
			double[] gamma_now = new double[P];
			
			double[][] alpha_now = new double[N][P];
			double[][] alpha_out = new double[N][P];
			double[][] ratio_out = new double[N][P];
			double beta0_now = 0.0;
			double[] beta_now = new double[P];
			double[] beta_out = new double[P+1];
	
			String alpha_path = "trace_alpha.txt";
			String beta_path = path;
			WriteArray wa = new WriteArray();
			boolean first = true;

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
			AlphaSampler sampler = new AlphaSampler(N, P, alpha_now);

			for(int p = 0; p < P; p++){
				gamma_now[p] = rand.nextDouble();
			}
			double tau = rngG.nextDouble(tau_a , tau_b);
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
						//System.out.printf("%f %f %f", gamma_now[p], ga.gamma(a), alpha_now[i][p]);
						double term = gamma_now[p] * Math.pow(b, a)/ga.gamma(a) 
								* Math.pow(alpha_now[i][p], a-1); //*Math.exp(-1 * b * alpha_now[i][p]);
						//System.out.printf(" -[%f]- ", term);
						double prob = term / (term + (1 - gamma_now[p]) * epsilon * Math.exp((b - epsilon) * alpha_now[i][p]));
						//System.out.printf(" -[%f]- \n", prob);
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
				
				// sample beta
				LogisticRegressionIRLS logistic = new LogisticRegressionIRLS(N, P, y, alpha_now);
				logistic.fit(tau, true);
				double[] betam = logistic.coef();
				System.out.printf("beta0 mean %.4f, beta1 mean %.4f, , beta2 mean %.4f\n", betam[0], betam[1], betam[2]);
				double[][] var = logistic.getCovMatrix(tau, true);
				MultivariateNormalDistribution rngMN = new MultivariateNormalDistribution(betam, var);
				double[] betaNew = rngMN.sample();
//				for(double n: betaNew) {
//				    System.out.printf("%.4f   ", n);
//				}				
				beta0_now = betaNew[0]; //rngN.nextDouble(beta0m,  1/Math.sqrt(var0));
				for(int itr = 0; itr < P; itr++){
					beta_now[itr] = betaNew[itr+1]; //rngN.nextDouble(betam[itr], 1/Math.sqrt(var[itr]));
				}
				// sample tau
				double sumSquare = beta0_now * beta0_now;
				for(int ii = 0; ii < P; ii ++){
					sumSquare += beta_now[ii] * beta_now[ii];
				}
				tau = rngG.nextDouble(tau_a + (P+1)/2, tau_b + sumSquare/2);
							
				// todo: update alpha part
				/*
				 * sample alpha
				 */
				// alpha_now = sampler.unsupervised(xsub, z_now, dict, a, b, epsilon, rngG);

//				alpha_now = sampler.mh(stepHM, rngN, rand, xsub, z_now, dict,
//						               a, b, epsilon, y, beta0_now, beta_now);
				alpha_now = sampler.Laplace(rngN, xsub, z_now, dict,
			               a, b, epsilon, y, beta0_now, beta_now);
				System.out.printf(".");
				if(t % n_report == 0){
					double spar = ((double)dsum.sum(zsum)) / (N * P + 0.0);
					long now   = System.currentTimeMillis();
					double ratio = ((double) dsum.sum(sampler.accept)) / (N * P * (t+1) + 0.0);
					System.out.printf("\n-- %d --", t);
					System.out.printf("Time -- %.2fmin --", (double) (now - start)/1000/60);
					System.out.printf("Sparse -- %.6f, Accept -- %.4f\n", spar, ratio);
				}
				if(t > burn){
					//gamma_out[t-burn] = gamma_now.clone();
					if(t % thin == 0){
						double[] beta_all = new double[P+1];
						beta_all[0] = beta0_now;
						for(int ii = 0; ii < P; ii++){
							beta_all[ii+1] = beta_now[ii];
						}
						wa.write(alpha_now, alpha_path, !first);
						wa.write(beta_all, beta_path, !first);
						System.out.printf("Itr %d sampled\n", t);
						alpha_out = VecOperation.Add(alpha_out, alpha_now);
						beta_out = VecOperation.Add(beta_out, beta_all);
						first = false;
					}
				}
			}
			alpha_out = VecOperation.Multi(alpha_out, 1/(T-burn+0.0));
			beta_out = VecOperation.Multi(beta_out, 1/(T-burn+0.0));			
			ratio_out = VecOperation.Multi(sampler.accept, 1/(T-burn+0.0));
			//return;
			return(alpha_out);
		}
	}
