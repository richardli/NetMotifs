package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for logistic regression using stochastic gradient descent
 *
 * @author zehang li
 * Last update: 07-27-2015
 *
 */
public class LogisticRegression{
	int N;
	int M;
	int[] Y; 
	double[][] X;
	double beta0;
	double[] beta;
	double[] prob;
	double step;
	double lambda;
	double tolerance;
	int maxItr = 5000;
	double beta_change = Double.MAX_VALUE;
	
//	public double[] main(int N, int M, int[] Y, double[][] X) {
//		LogisticRegression fit = new LogisticRegression(N, M, Y, X);
//		fit.fit();
//		System.out.println(fit.coef());
//		return(fit.coef());
//	}

	
	public LogisticRegression(int N, int M, int[] Y, double[][] X){
		this.N = N;
		this.M = M;
		this.Y = Y;
		this.X = X;
		this.beta0 = 0;
		this.beta = new double[M];
		this.step = 0.001;
		this.lambda = 0.0;
		this.tolerance = 0.0001;
	}
	
	public LogisticRegression(int N, int M, int[] Y, double[][] X, 
			double step, double lambda, double tolerance){
		this.N = N;
		this.M = M;
		this.Y = Y;
		this.X = X;
		this.beta0 = 0;
		this.beta = new double[M];
		this.step = step;
		this.lambda = lambda;
		this.tolerance = tolerance;
	}
	
	public boolean evaluateCondition(double logLikelihood, double prevLogLikelihood, int i) {
		return (Math.abs(logLikelihood - prevLogLikelihood) > tolerance && 
				i < this.maxItr) ? true : false;  
		} 
	
	public double estimateProbs(double[] one) {    
		double sum = this.beta0;   
		for (int i = 0; i < this.beta.length; i++)    
			sum += this.beta[i] * one[i];   
		double exponent = Math.exp(-sum);   
		double probPositive = 1 / (1 + exponent);   
		if (probPositive == 0)    probPositive = 0.00001;   
		else if (probPositive == 1)    probPositive = 0.9999;    
		return probPositive;  
	}   
	
	public void adjustWeights(double[] one, double probPositive, int label) {                 
		//for the intercept   
		this.beta0  += this.step * (((double) label) - probPositive);    
		for (int i = 0; i < this.beta.length; i++) {    
			this.beta[i] += this.step * one[i] * (((double)label) - probPositive);   
			}  
	}    
	
	public double calculateLogL(double[] probs) {	 
		double logl = 0;
		double[] label = new double[N];
		for(int i = 0 ; i < probs.length; i++){
			label[i] = (probs[i] >= 0.5 ? 1 : 0);
			logl += label[i] * Math.log(probs[i]) + (1-label[i]) * Math.log(1-probs[i]);
		}
		return logl; 
  }
  
	public void fit(){
		double prevLogLik = Double.MIN_VALUE;
		double[] probs = new double[N];
		for(int i = 0; i < N; i++){
			probs[i] = estimateProbs(X[i]);
		}		
		double LogLik = calculateLogL(probs);
 		int itr = 0;
 				
		while(evaluateCondition(LogLik, prevLogLik, itr)){
			for(int i = 0; i < N; i++){
				probs[i] = estimateProbs(X[i]);
				adjustWeights(X[i], probs[i], Y[i]);
			}
			itr++;
			prevLogLik = LogLik;
			LogLik = calculateLogL(probs);
		}
		System.out.printf("%d used for regression, beta0 %.4f, beta1 %.4f\n", itr, this.beta0, this.beta[0]);
 		this.prob = probs;
	}
   
	public double intercept(){
		return(beta0);
	}
	
	public double[] beta(){
		return(beta);
	}
	
	public double[] coef(){
		double[] coef = new double[M+1];
		coef[0] = beta0;
		for(int i = 0; i < M; i++){
			coef[i+1] = beta[i];
		}
		return(coef);
	}
	
	public double[] getProb(){
		return(this.prob);
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		String path = "/Users/zehangli/test.txt";
		String betapath = "/Users/zehangli/testcoef.txt";
		int N = 1000;
		int M = 20;
		int[] Y = new int[N];
		double[][] X = new double[N][M];
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		int i = 0;
		while((line = br.readLine()) != null){
			String[] fields = line.split(",");
			Y[i] = Integer.parseInt(fields[0]);
			for(int j = 0; j < M; j++){
				X[i][j] = Double.parseDouble(fields[j+1]);
			}
			i++;
		}
		br.close();

		LogisticRegression fit = new LogisticRegression(N, M, Y, X);
		fit.fit();
		double[] coef = fit.coef();
		BufferedWriter bw = new BufferedWriter(new FileWriter(betapath));
		StringBuffer sb = new StringBuffer();

		for(int j =0; j < M+1 ; j++){
			System.out.printf("%.2f, ", coef[j]);
			sb.append(coef[j]+"\n"); 
		}
		bw.write(sb.toString());
		bw.close();
		return;
	}
	
}
