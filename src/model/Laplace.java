package model;

import weka.core.Optimization;

class Laplace extends Optimization {
   // Provide the objective function
   double A = 0.0;
   double B = 0.0;
   double C = 0.0;
   double D = 0.0;
   
   double E0 = 0.0;
   double E1 = 0.0;
   double E2 = 0.0;
   
   
	public void initiate(
			double x_sumj, double d_sumj, 
			int z, double alpha_prev, double alpha_beta_prev,
			double a, double b, double epsilon, 
			int y, double betap){
		
		this.A = x_sumj + (a - 1) * z + 1;
		this.B = -1 * (d_sumj + b * z + epsilon * (1 - z) + betap * y);
		this.C = alpha_beta_prev - betap * alpha_prev;
		this.D = betap;	
	}
   
	protected double objectiveFunction(double[] x) {
	   double f = 0.0;
	   f = x[0] * this.A + this.B * Math.exp(x[0]);
	   //System.out.printf("%.4f, %.4f, %.4f\n", this.C , this.D , Math.exp(x[0]));
	   f -= Math.log(1 + Math.exp(this.C + this.D * Math.exp(x[0])));
	   return(-1*f) ;
   }
 
   // Provide the first derivatives
   protected double[] evaluateGradient(double[] x) {
	   double f[] = new double[1];
//	   f[0] = y * Math.pow(y,  this.A - 1) * Math.exp(this.B * y) 
//			   * ((this.A + (this.B-this.D)*y) * Math.exp(this.C + this.D * y) + this.A + this.B * y);
//	   f[0] /= Math.pow(Math.exp(this.C+this.D*y)+1, 2);
//	   // it calculates the derivative of loglik
//	   f[0] /= Math.exp(objectiveFunction(x));
//	   f[0] = -1 * f[0];
	   f[0] = this.A + 1 + (this.B-this.D) * Math.exp(x[0]);
	   f[0] -= this.D * Math.exp(x[0])/ (1 + Math.exp(this.C + this.D * Math.exp(x[0])));
	   f[0] = -1 * f[0];
	   return(f);
   }
   protected double[] evaluateHessian(double[] x, int index) {
	   double[] hessian = new double[1];
	   hessian[0] = calculateHessian(x[0]);
	   return(hessian);
   }
   public double calculateHessian(double x) {
	   double f = 0.0;
	   double exd = this.D * Math.exp(x);
	   double y = Math.exp(this.C + exd);
	   //System.out.printf("betap %.4f, ex %.4f, exd %.4f, y %.4f", this.D, Math.exp(x), exd, y);
	   f += (this.B - this.D) * Math.exp(x);
	   if(Double.isInfinite(y)){
		   f -= 0;
	   }else{
		   f -= exd / (y+1) *  ((exd - 1) * y - 1) /(y+1);
	   }
//	   this.E0 = Math.exp(this.C + this.D*y)+1;
//	   this.E1 = this.B * this.B * this.E0 * this.E0 
//			   - 2 * this.B * this.D * (this.E0 - 1) * this.E0
//			   + this.D * this.D * (this.E0 - 1) * (this.E0 - 2);
//	   this.E2 = 2 * this.A * this.E0 *(this.B * this.E0 - this.D * (this.E0 - 1))
//			   + this.B * this.E0 * this.E0 
//			   - this.D * (this.E0 - 1) * (this.E0 - 2);
//	   //System.out.printf("A_%f B_%f C_%f D_%f", this.A, this.B, this.C, this.D);
//	   //System.out.printf("E0_%f E1_%f E2_%f", this.E0, this.E1, this.E2);
//	   f = 1/Math.pow(this.E0, 3) * Math.pow(y, this.A) * Math.exp(this.B * y) 
//			   * (this.E1 * y * y 
//					   - this.E2 * y + this.A * this.A * this.E0 * this.E0
//					   - this.A * this.E0 * this.E0);
	   return(f) ;
   }

@Override
public String getRevision() {
	// TODO Auto-generated method stub
	return null;
}
 }