package model;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import util.MathUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Implementation of http://arxiv.org/pdf/1310.5438v2.pdf
 * Code adapted from https://github.com/jdrugo/vb_logit/blob/master/vb_logit_fit.m
 *
 * Created by zehangli on 6/2/16.
 */
public class LogisticRegressionVB {

    double[][] V;
    double[] beta;

    /** Default a0 = 1e-2, b0 = 1e-4 **/
    public void fit(double[][] X0, int[] Y){
        this.fit(X0, Y, 0.01, 0.0001);
    }

    /** Y takes value 1 or 0 **/
    public void fit(double[][] X0, int[] Y, double a0, double b0){

        double[][] X = add_intercept(X0);

        int N = Y.length;
        int D = X[0].length;
        // transform Y into 1 or -1
        for(int i = 0; i < N; i++){Y[i] = (Y[i] == 0) ? -1 : Y[i];}

        int max_iter = 500;
        double an = a0 + 0.5 * (D + 0.0);
        double gammaln_an_an = MathUtil.logGamma(an) + an;
        double[][] t_w = new double[D][1];

        // t_w = 0.5 * sum(bsxfun(@taimes, X, Y), 1)'
        for(int i = 0; i < N; i++){
            for(int j = 0; j < D; j++){
              t_w[j][0] += X[i][j] * Y[i] * 0.5;
            }
        }
        RealMatrix t_w_mat = new Array2DRowRealMatrix(t_w);

        double[] lam_xi = new double[N];
        Arrays.fill(lam_xi, 1/8.0);
        double E_a = a0 / b0;


        // lam_xi = ones(N, 1) / 8;
        // invV = E_a * eye(D) + 2 * X' * bsxfun(@times, X, lam_xi);
        RealMatrix Xmat = new Array2DRowRealMatrix(X);
        RealMatrix invV = Xmat.preMultiply(Xmat.transpose());
        for(int i = 0; i < D; i++){
            for(int j = 0; j < D; j++){
                invV.setEntry(i, j, invV.getEntry(i, j) / 8.0 * 2);
                if(i == j){
                    invV.setEntry(i, j, invV.getEntry(i, j) + E_a);
                }
            }
        }

        RealMatrix V = new LUDecomposition(invV).getSolver().getInverse();
        RealMatrix w = t_w_mat.preMultiply(V);
        double bn = w.preMultiply(w.transpose()).getEntry(0, 0) + V.getTrace();
        bn = bn * 0.5 + b0;
        double winvVw = w.preMultiply(invV.preMultiply(w.transpose())).getEntry(0, 0);
        double logdet = Math.log(new LUDecomposition(invV).getDeterminant());
        double L_last = -1 * N * Math.log(2) + 0.5 * (winvVw - logdet) - an/bn*b0 - an * Math.log(bn) + gammaln_an_an;

        for(int itr = 0; itr < max_iter; itr ++){
            // xi = sqrt(sum(X .* (X * (V + w * w')), 2))
            // lam_xi = lam(xi)
            RealMatrix temp = (w.transpose()).preMultiply(w);
            temp = temp.add(V);
            temp = temp.preMultiply(Xmat);
            double[] xi = new double[N];
            for(int i = 0; i < N; i++){
                for(int j = 0; j < D; j++){
                    xi[i] += X[i][j] * temp.getEntry(i, j);
                }
                xi[i] = Math.sqrt(xi[i]);
                lam_xi[i] = lam(xi[i]);
            }

            // update posterior parameters
            bn = b0 + 0.5 *  w.preMultiply(w.transpose()).getEntry(0, 0) + V.getTrace();
            E_a = an / bn;

            // recompute posterior parameters of W
            RealMatrix X_times_lam_xi = new Array2DRowRealMatrix(X);
            for(int i = 0; i < N; i++){
                for(int j = 0; j < D; j++){
                    X_times_lam_xi.setEntry(i, j, X[i][j] * lam_xi[i]);
                }
            }
            invV = X_times_lam_xi.preMultiply(Xmat.transpose());
            invV = invV.scalarMultiply(2.0);
            for(int i = 0; i < D; i++){
                invV.setEntry(i, i, invV.getEntry(i, i) + E_a);
            }
            double logdetV = -1 * Math.log(new LUDecomposition(invV).getDeterminant());
            V = new LUDecomposition(invV).getSolver().getInverse();
            w = t_w_mat.preMultiply(V);

            //    % variational bound, ingnoring constant terms for now
            //     L = - sum(log(1 + exp(- xi))) + sum(lam_xi .* xi .^ 2) ...
            //         + 0.5 * (w' * invV * w + logdetV - sum(xi)) ...
            //         - E_a * b0 - an * log(bn) + gammaln_an_an;
            double L = 0;
            for(int i = 0; i < N; i++){
                L -= Math.log(1 + Math.exp(-1 * xi[i]));
                L += lam_xi[i] * xi[i] * xi[i];
                L -= 0.5 * xi[i];
            }
            L += 0.5 * w.preMultiply(invV.preMultiply(w.transpose())).getEntry(0, 0);
            L -= E_a * b0;
            L -= an * Math.log(bn);
            L += gammaln_an_an;

            if(L_last > L | Math.abs(L_last - L) < Math.abs(0.0001 * L)){
                break;
            }
            L_last = L;

            if(itr == max_iter) {
                System.out.println("Maximum iterations reached before convergence");
            }
        }


        this.beta = w.transpose().getData()[0];
        this.V = V.getData();
    }

    /** Compuate lam(x) = 1 / (2 * x) * (sigmoid(x) - 0.5)**/
    public static double lam(double x){
        return(1 / (2 * x) * (sigmoid(x) - 0.5));
    }

    public static double sigmoid(double x){
        return(1.0 / (1.0 + Math.exp(x * (-1))));
    }

    public static double[][] add_intercept(double[][] X0){
        double[][] X = new double[X0.length][X0[0].length + 1];
        for(int i = 0; i < X0.length; i++){
            X[i][0] = 1;
            for(int j = 0; j < X0[0].length; j++){
                X[i][j + 1] = X0[i][j];
            }
        }
        return(X);
    }
    // todo: finish calculating log likelihood
    public double calculateLogL() {
       return 0;
    }

    public static void main(String[] args) throws IOException {
        // http://www.ats.ucla.edu/stat/data/binary.csv
        String path = "/Users/zehangli/LRtest.txt";
        int N = 400;
        int M = 3;
        int[] Y = new int[N];
        double[][] X = new double[N][M];
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
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
        double[][] cov = fit.getCovMatrix(0, true);
        System.out.println("IRLS");
        for(int j =0; j < M+1 ; j++){
            System.out.printf("%.4f, ", coef[j]);
            System.out.printf("sd %.8f \n", Math.sqrt(cov[j][j]));
        }

        LogisticRegressionVB fit2 = new LogisticRegressionVB();
        fit2.fit(X, Y);
        System.out.println("\nVB");
        for(int j =0; j < M+1 ; j++){
            System.out.printf("%.4f, ", fit2.beta[j]);
            System.out.printf("sd %.4f \n", Math.sqrt(fit2.V[j][j]));
        }


        return;
    }

}
