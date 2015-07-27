package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;


public class LogisticRegressionIRLS{
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
	int maxItr = 100;
	double beta_change = Double.MAX_VALUE;
	//	public double[] main(int N, int M, int[] Y, double[][] X) {
	//		LogisticRegression fit = new LogisticRegression(N, M, Y, X);
	//		fit.fit();
	//		System.out.println(fit.coef());
	//		return(fit.coef());
	//	}


	public LogisticRegressionIRLS(int N, int M, int[] Y, double[][] X){
		this.N = N;
		this.M = M;
		this.Y = Y;
		this.X = X;
		this.beta0 = 0;
//		for(int i = 0; i < N; i++) this.beta0 += Y[i];
//		this.beta0 /= N;
//		this.beta0 = -1 * Math.log(1 / this.beta0 - 1);

		this.beta = new double[M];
		this.step = 0.001;
		this.lambda = 0.0;
		this.tolerance = 0.00001;
	}

	public LogisticRegressionIRLS(int N, int M, int[] Y, double[][] X, 
			double step, double lambda, double tolerance){
		this.N = N;
		this.M = M;
		this.Y = Y;
		this.X = X;
		this.beta0 = 0;
//		for(int i = 0; i < N; i++) this.beta0 += Y[i];
//		this.beta0 /= N;
//		this.beta0 = -1 * Math.log(1 / this.beta0 - 1);
		this.beta = new double[M+1];
		this.step = step;
		this.lambda = lambda;
		this.tolerance = tolerance;
	}

	public boolean evaluateCondition(double dev, double prevdev, int i) {
		//System.out.printf("%.4f, %.4f, %.4f\n", dev, prevdev, Math.abs((dev - prevdev)/(Math.abs(prevdev) + 0.1)));
		return ((Math.abs((dev - prevdev)/(Math.abs(prevdev) + 0.1)) > tolerance && 
				i < this.maxItr) ? true : false);  
	} 
	public boolean evaluateCondition( int i) {
		return (this.beta_change > tolerance && 
				i < this.maxItr) ? true : false;  
	} 
	
	public double[][] getCovMatrix(double tau, boolean corr){
		double[] probs = new double[N];
		for(int i = 0; i < N; i++){
			probs[i] = estimateProbs(X[i]);
		}	
		double[][] invHp = getInverseHessian(probs, tau, corr);
		//for(int i = 0; i < this.M+1; i++) invHp[i][i] += tau;
		return(invHp);
	}

	// return inverse Hessian matrix 
	public double[][] getInverseHessian(double[] probs, double tau, boolean corr){
		double[][] H = new double[this.M+1][this.M+1];
		// if there is prior provided
		if(tau > 0){
			for(int i = 0; i < this.M+1; i++){
					H[i][i] += tau;
			}
		}
		// when i = j =0
		for(int k = 0; k < this.N; k++) H[0][0] +=   probs[k] * (1 - probs[k]);

		// when i = 0
		for(int j = 1; j < this.M + 1; j++){
			for(int k = 0; k < this.N; k++){
				H[0][j] +=  this.X[k][j-1] * probs[k] *(1 - probs[k]);
			}
		}

		// when j = 0
		for(int i = 1; i < this.M + 1; i++){
			for(int k = 0; k < this.N; k++){
				H[i][0] +=  this.X[k][i-1] * probs[k] * (1 - probs[k]);
			}
		}

		// when both not 0
		for(int i = 1; i < this.M + 1; i++){
			for(int j = 1; j < this.M + 1; j++){
				for(int k = 0; k < this.N; k++){
					H[i][j] += this.X[k][i-1] * this.X[k][j-1] * probs[k] * (1 - probs[k]);					
				}
			}
		}
		double[][] invH = new double[this.M+1][this.M+1];
		
		if(corr){
			 RealMatrix mH = MatrixUtils.createRealMatrix(H);
			 RealMatrix minvH = new QRDecomposition(mH).getSolver().getInverse();
			 //double det = new LUDecomposition(mH).getDeterminant();
			 //System.out.println(det);
			 invH = minvH.getData();			
		}else{
			for(int i = 0; i < this.M + 1; i++) invH[i][i] = 1/H[i][i];					
		}

		return(invH);
	}

	public double estimateProbs(double[] one) {    
		double sum = this.beta0;   
		for (int i = 0; i < this.beta.length; i++)  sum += this.beta[i] * one[i];   
		double exponent = Math.exp(-sum);   
		double probPositive = 1 / (1 + exponent);   
		if (probPositive == 0)    probPositive = 0.00001;   
		else if (probPositive == 1)    probPositive = 0.9999;    
		return probPositive;  
	}   

	public void adjustWeights(double[] probPositive, double tau, boolean corr) {  
		//this.beta_change = 0;
		
		double[][] invH = getInverseHessian(probPositive, tau, corr);
		
		// calculate re-weighted step
		double[] delta = new double[this.M+1];
		for(int i = 0; i < this.M+1; i++){
			for(int k = 0; k < this.N; k++){
				delta[i] += invH[i][0]  * (probPositive[k] - this.Y[k] + this.beta0 * tau / (this.N+0.0));
				for(int j = 1; j < this.M+1; j++){
					delta[i] += invH[i][j] * (this.X[k][j-1] * (probPositive[k] - this.Y[k])
							                  + this.beta[j-1] * tau / (this.N+0.0) ) ;
				}
			}	
	   }

		//for the intercept  
		this.beta0 -= delta[0];            //+= this.step * (((double) label) - probPositive);    

		for (int i = 0; i < this.beta.length; i++) {   
			this.beta[i] -= delta[i+1];   //+= this.step * one[i] * (((double)label) - probPositive);   
		}  
	}    

	public double calculateLogL(double[] probs) {	 
		double logl = 0;
		for(int i = 0 ; i < probs.length; i++){
			logl += this.Y[i] * Math.log(probs[i]) + (1-this.Y[i]) * Math.log(1-probs[i]);
		}
		return logl; 
	}

	
	// enter the prior tau = 1/sd^2
	public void fit(double tau, boolean corr){
		// double prevLogLik = Double.MIN_VALUE;
		double[] probs = new double[N];
		for(int i = 0; i < N; i++){
			probs[i] = estimateProbs(X[i]);
		}		
		double LogLik = calculateLogL(probs);
		double Deviance = -2 * LogLik;
		double prevDeviance = Double.MIN_VALUE;
		// System.out.println(LogLik);
		int itr = 0;

		while(evaluateCondition(Deviance, prevDeviance,itr)){
			prevDeviance = Deviance;
			adjustWeights(probs, tau, corr);
			for(int i = 0; i < N; i++){
				probs[i] = estimateProbs(X[i]);
			}
			itr++;
			// prevLogLik = LogLik;
			Deviance = -2 * calculateLogL(probs);
		}
		//System.out.printf("%d used for regression, beta0 %.4f, beta1 %.4f\n", itr, this.beta0, this.beta[0]);
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
		for(int i = 0; i < this.M; i++){
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
		int N = 32;
		int M = 2;
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

		LogisticRegressionIRLS fit = new LogisticRegressionIRLS(N, M, Y, X);
		fit.fit(0.0, true);
		double[] coef = fit.coef();
//		BufferedWriter bw = new BufferedWriter(new FileWriter(betapath));
//		StringBuffer sb = new StringBuffer();

		for(int j =0; j < M+1 ; j++){
			System.out.printf("%.2f, ", coef[j]);
//			sb.append(coef[j]+"\n"); 
		}
//		bw.write(sb.toString());
//		bw.close();
		return;
	}

}
