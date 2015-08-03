package model;

import java.util.Random;

import util.MathUtil;
import util.MapWrapper;
import util.VectorUtil;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.Normal;


public class AlphaSampler {
	int N;
	int P;
	double[][] alpha_now = new double[N][P];
	int[][] accept = new int[N][P];
	
	public AlphaSampler(int N, int P, double[][] alpha_now){
		this.N = N;
		this.P = P;
		this.alpha_now = alpha_now;
		this.accept = new int[N][P];
	}
	/* 
	 *  Unsupervised Gibbs sampler
	 */
	public double[][] unsupervised(MapWrapper[][] xsub, int[][] z_now, double[][] dict, 
			double a, double b, double epsilon, Gamma rngG){
		for(int i = 0; i < N; i++){
			for(int p = 0; p < P; p++){		
				double shape = VectorUtil.pick13_sum(i, p, xsub) + (a-1) * z_now[i][p] + 1;
				double rate = VectorUtil.vectorSum(dict[p]) + b * z_now[i][p] + epsilon * (1-z_now[i][p]);
				this.alpha_now[i][p] = rngG.nextDouble(shape,rate);
			}
		}
		return(this.alpha_now);
	}
	
	/* 
	 *  Supervised MH sampler
	 */
	public double loglik_first(double xsub_sumj, int z_now_ip, double dict_sumj,
							   double alpha_ip, double a, double b, double epsilon, double beta_p, int y_i){
		
		double loglik = (xsub_sumj + (a-1)*z_now_ip + 1)*Math.log(alpha_ip);
		loglik -= alpha_ip *(dict_sumj + b * z_now_ip + epsilon * (1 - z_now_ip) + beta_p * y_i);
		return(loglik);
	}
	
	public double loglik_second(double alpha_beta){
		double loglik = 0.0;
		loglik -= Math.log(1 + Math.exp(alpha_beta));
		return(loglik);
	}
	
	/*
	 * Auxilary variable sampler
	 */
//	public double[][] Aux(double sigma, Normal rngN, Random rand,
//			MapWrapper[][] xsub, int[][] z_now, double[][] dict, 
//			double a, double b, double epsilon, 
//			int[] y, double beta0, double[] beta){
//		double beta_sum = 0;
//		for(int i = 0; i < beta.length; i++){beta_sum += beta[i];}
//		Dimsum dsum = new Dimsum();
//		TruncGamma gaSampler = new TruncGamma();
//		for(int i = 0; i < this.N; i++){
//			// calculate alpha * beta + beta0
//			double alpha_beta_prev = beta0;
//			for(int p = 0; p< this.P; p++){
//				alpha_beta_prev += this.alpha_now[i][p] * beta[p];
//			}
//			
//			for(int p = 0; p < this.P; p++){
//				// propose by lognormal distribution
//				double loglik_u = loglik_second(alpha_beta_prev, y[i], beta[p], this.alpha_now[i][p]);
//				double u = rand.nextDouble() * Math.exp(loglik_u);
//				double trunc_temp = Math.log(1.0/u - 1);
//				double trunc_min = 0;
//				double trunc_max = 1;
//				
//				if(y[i] == 1){
//					trunc_temp = -1 * trunc_temp - beta0;
//					if(beta_sum > 0){ trunc_min = trunc_temp / beta_sum;
//					}else if(beta_sum < 0){ trunc_max = trunc_temp / beta_sum;}
//				}else{
//					trunc_temp = trunc_temp - beta0;
//					if(beta_sum > 0){ trunc_max = trunc_temp / beta_sum;
//					}else if(beta_sum < 0){ trunc_min = trunc_temp / beta_sum;}
//				}
//				alpha_beta_prev -= this.alpha_now[i][p] * beta[p];
//				double first = (dsum.pick13_sum(i, p, xsub) + (a-1)*z_now[i][p]) + 1;
//				double second = (dsum.sum(dict[p]) + b * z_now[i][p] + epsilon * (1 - z_now[i][p]));
//				this.alpha_now[i][p] = gaSampler.truncGamma(first, second, trunc_min, trunc_max, rand);
//				alpha_beta_prev += this.alpha_now[i][p] * beta[p];
//				}
//			}
//		return(this.alpha_now);	
//	}
	/*
	 * MH sampler
	 * 
	 */
	public double[][] mh(double sigma, Normal rngN, Random rand,
			MapWrapper[][] xsub, int[][] z_now, double[][] dict, 
			double a, double b, double epsilon, 
			int[] y, double beta0, double[] beta){
		double[][] alpha_prev = new double[this.N][this.P];
		for(int i = 0; i < this.N; i++){
			for(int j = 0; j < this.P; j++){
				alpha_prev[i][j] = this.alpha_now[i][j];
			}
		}
		for(int i = 0; i < this.N; i++){
			// calculate alpha * beta + beta0
			double alpha_beta_prev = beta0;
			for(int p = 0; p< this.P; p++){
				alpha_beta_prev += alpha_prev[i][p] * beta[p];
			}
			
			for(int p = 0; p < this.P; p++){
				// propose by lognormal distribution
				double temp = rngN.nextDouble(Math.log(alpha_prev[i][p]), sigma);
				this.alpha_now[i][p] = Math.exp(temp);
				// update proposed alpha*beta+beta0
				double alpha_beta_now = alpha_beta_prev + (alpha_now[i][p] - alpha_prev[i][p]) * beta[p]; 
				double u = rand.nextDouble();
				// Update the ratio calculation here!
				double loglik1 = 0.0;
				double loglik2 = 0.0;
				loglik1 += loglik_first(VectorUtil.pick13_sum(i, p, xsub), z_now[i][p], VectorUtil.vectorSum(dict[p]),
						                alpha_prev[i][p], 
						                a, b, epsilon, beta[p], y[i]); 
				loglik2 += loglik_first(VectorUtil.pick13_sum(i, p, xsub), z_now[i][p], VectorUtil.vectorSum(dict[p]),
										alpha_now[i][p], 
										a, b, epsilon, beta[p], y[i]); 
				loglik1 += loglik_second(alpha_beta_prev);
				loglik2 += loglik_second(alpha_beta_now);
				double jacobian = 1;
				//double jacobian = alpha_prev[i][p] / alpha_now[i][p];
				if(Math.log(u) > loglik1 - loglik2 + Math.log(jacobian)){
					this.alpha_now[i][p] = alpha_prev[i][p];
//					System.out.println(accept[i][p]);
				}else{
					accept[i][p] ++;
					// update alpha*beta+beta0
					alpha_beta_prev = alpha_beta_now;
				}
			}
		}
		return(this.alpha_now);		
	}
	
	/*
	 * Supervised Laplace sampler
	 */
	public double[][] Laplace(Normal rngN, 
			MapWrapper[][] xsub, int[][] z_now, double[][] dict, 
			double a, double b, double epsilon, 
			int[] y, double beta0, double[] beta) throws Exception{
		
//		double[][] alpha_prev = new double[this.N][this.P];
//		for(int i = 0; i < this.N; i++){
//			for(int j = 0; j < this.P; j++){
//				alpha_prev[i][j] = this.alpha_now[i][j];
//			}
//		}
		double[][] constraint = new double[2][1];
		constraint[0][0] = -10;
		constraint[1][0] = 10;
		for(int i = 0; i < this.N; i++){				
			for(int p = 0; p < this.P; p++){
				// calculate alpha * beta + beta0
				double alpha_beta_prev = beta0;
				for(int pp = 0; pp< this.P; pp++){
					alpha_beta_prev += this.alpha_now[i][pp] * beta[pp];
				}	
				double x_sumj = VectorUtil.pick13_sum(i, p, xsub);
				double d_sumj = VectorUtil.vectorSum(dict[p]);
				Laplace lap = new Laplace();
				lap.initiate(x_sumj, d_sumj,  z_now[i][p],
						this.alpha_now[i][p], alpha_beta_prev, 
						a, beta0, epsilon, y[i], beta[p]);
				double[] prev = new double[1];
				prev[0] = Math.log(this.alpha_now[i][p]);
				double[] tempMean = lap.findArgmin(prev, constraint);
                double thisMean = 0;
                if(tempMean != null){
                    thisMean = tempMean[0];
                    double tempH = lap.calculateHessian(thisMean);
                    //System.out.printf("mean%f-sd%f\n", tempMean[0], -1/tempH);
                    this.alpha_now[i][p] = Math.exp(rngN.nextDouble(thisMean, -1/tempH));
                }else{
                    this.alpha_now[i][p] = 0.000001;
                }
			}
		}	
		return(this.alpha_now);
	}
	
}
