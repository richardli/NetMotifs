package model;

import cern.jet.random.tdouble.Gamma;

/**
 * Created by zehangli on 7/15/16.
 */
public class PoissonModel2_NeighbourImpulse {
    double[][] theta;
    int M;
    double a, b;

    PoissonModel2_NeighbourImpulse(int M, double a, double b){
        this.theta = new double[M][M];
        this.M = M;
        this.a = a;
        this.b = b;
    }

    public void init_theta(Gamma rngG){
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                this.theta[j][jj] = rngG.nextDouble(a, b);
            }
        }
    }

    public void resample_theta(int[][] sum_i_xsub, double[][] sum_i_MotifStar, Gamma rngG, boolean verbose, long start){
        double theta_verbose = 0;
        for(int j = 0; j < M; j++){
            for(int jj = 0; jj < M; jj++){
                double sum_i_x = (double) sum_i_xsub[j][jj];
                double sum_i_x_tilde = sum_i_MotifStar[j][jj];
                this.theta[j][jj] = rngG.nextDouble(sum_i_x + this.a, sum_i_x_tilde + this.b);
                theta_verbose += this.theta[j][jj];
            }
        }
        if(verbose) {
            System.out.print("finish theta: ");
            System.out.printf("%.2fmin, Mean %.6f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                    (theta_verbose + 0.0) / (M * M + 0.0));
        }

    }


}
