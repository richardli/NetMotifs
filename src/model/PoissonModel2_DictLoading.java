package model;

import cern.jet.random.tdouble.Beta;
import cern.jet.random.tdouble.Binomial;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.Normal;
import util.MapWrapper;
import util.MathUtil;
import util.TruncGamma;
import util.VectorUtil;
import weka.core.Debug;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by zehangli on 7/15/16.
 */
public class PoissonModel2_DictLoading {

    double[][] alpha;
    int[][] z;
    double[] gamma;
    int N;
    int P;
    double a_sl, b_sl, a_sp, b_sp, a_hyper, b_hyper;

    PoissonModel2_DictLoading(int N, int P,
                              double a_sl, double b_sl,
                              double a_sp, double b_sp,
                              double a_hyper, double b_hyper){
        this.alpha = new double[N][P];
        this.z = new int[N][P];
        this.gamma = new double[P];
        this.N = N;
        this.P = P;
        this.a_sl = a_sl; this.b_sl = b_sl;
        this.a_sp = a_sp; this.b_sp = b_sp;
        this.a_hyper = a_hyper; this.b_hyper = b_hyper;
    }

    public void init_alpha(Gamma rngG){
        for(int i = 0; i < N; i++){
            for(int p = 0; p < P; p++){
                this.alpha[i][p] = rngG.nextDouble(a_sl, b_sl);
            }
        }
    }

    public void init_gamma(Random rand){
        for(int p = 0; p < P; p++){
            this.gamma[p] = 0.5;
        }
//        for(int p = 0; p < P; p++){
//            this.gamma[p] = rand.nextDouble();
//        }
    }

    public void resample_z(Binomial rngB, boolean verbose, long start){
        double zsum_verbose = 0;

        for(int i=0; i < N; i++){
            for(int p=0; p<P; p++){
                //if(Math.pow(this.alpha[i][p], this.a_sl-1) == Double.NaN) {
                // System.out.printf("%.4f, %.4f\n", this.alpha[i][p], this.a_sl-1);
                //} // <----------------------
                double firstTerm = this.gamma[p] * Math.pow(this.b_sl, this.a_sl) /
                                    MathUtil.gamma(this.a_sl)
                                    * Math.pow(this.alpha[i][p], this.a_sl-1)
                                    * Math.exp(-1 * b_sl * this.alpha[i][p]);
                if(Double.isInfinite(firstTerm)){
                    // this could happen if alpha is exactly 0 from truncation
                    firstTerm = 0;
                }

                double secondTerm = (1-this.gamma[p]) * this.b_sp * Math.exp(-this.alpha[i][p] * this
                        .b_sp);
                double prob = firstTerm / (firstTerm + secondTerm);

                if(firstTerm == 0 & secondTerm == 0){
                    System.out.printf("Both probs = 0 here: i=%d, p=%d, gamma=%.4f, alpha=%.4f, a=%.2f, b=%.2f\n",
                            i, p, this.gamma[p], this.alpha[i][p], a_sl, b_sl);
                    prob = 0.5;
                }
                if(prob * (1 - prob) == 0){
                    this.z[i][p] = (int) prob;
                }else{
                    this.z[i][p] = rngB.nextInt(1, prob);
                }
                zsum_verbose += this.z[i][p];
            }
        }
        if(verbose) {
            System.out.print("finish Z: ");
            System.out.printf("%.2fmin, Sparsity %.6f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                    (zsum_verbose + 0.0) / (N * P + 0.0));
        }
    }
    public double alpha_lik(){
        double lik = 0;

        return(lik);
    }


    public void resample_alpha(boolean supervised, MapWrapper[][] xsub, double[][] dict, Gamma rngG,
                               Random rand, PoissonModel2_RegCoef beta_model,
                               boolean verbose, long start){
        if(!supervised){
            double alpha_verbose = 0;
            for(int i = 0; i < N; i++) {
                for(int p = 0; p < this.P; p++) {
                    resample_alpha_single(i, p, xsub, dict, rngG, rand);
                    alpha_verbose += this.alpha[i][p];
                }
            }
            if(verbose) {
                System.out.print("finish alpha: ");
                System.out.printf("%.2fmin, Mean %.2f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (alpha_verbose + 0.0) / (P * N + 0.0));
            }

        }else{
            double alpha_verbose = 0;
            // pre-calculate the current cross products
            double[] beta_cross_prod = new double[N];
            for(int i = 0; i < N; i++) {
                beta_cross_prod[i] = beta_model.beta0;
                for (int p = 0; p < P; p++) {
                    beta_cross_prod[i] += this.alpha[i][p] * beta_model.beta[p];
                }
            }

            double alpha_before;

            for(int p = 0; p < this.P; p++) {
                for(int i = 0; i < this.N; i++){
                    alpha_before = this.alpha[i][p];
                    double conditional_beta0 = beta_cross_prod[i] - this.alpha[i][p] * beta_model.beta[p];
                    // use P. Damien, J. Wakefield and S. Walker method
                    double u = rand.nextDouble() ;
                    double v = rand.nextDouble() ;
                    double a = 0;
                    double b = 0;
                    // update the range of u and v
                    if(beta_model.y[i] == 1){
                        u *= 1.0 / (1.0 + Math.exp(-beta_cross_prod[i]));
                        v = 1;
                        a = beta_model.beta[p] == 0 ?
                                0 : (Math.log(1 / u - 1) - conditional_beta0) / beta_model.beta[p];
                        b = Double.MAX_VALUE;
                        if(a == Double.MAX_VALUE){
                            System.out.println("min trunc is too large");
                        }

                    }else if(beta_model.y[i] == 0){
                        u = 1;
                        v *= 1.0 / (1.0 + Math.exp(beta_cross_prod[i]));
                        a = 0;
                        b = beta_model.beta[p] == 0 ?
                                Double.MAX_VALUE : (Math.log(1 / v - 1) - conditional_beta0) / beta_model.beta[p];
                    }

                    a = Math.max(a, 0);
                    b = Math.max(b, 0);
                    resample_alpha_single_truncated(i, p, xsub, dict, rngG, rand, a, b);
                    // now since alpha[i][p] is updated, the beta_cross_prod[i] needs to be adjusted too
                    beta_cross_prod[i] += (this.alpha[i][p] - alpha_before) * beta_model.beta[p];
                    alpha_verbose += this.alpha[i][p];

                }
            }
            if(verbose) {
                System.out.print("finish alpha: ");
                System.out.printf("%.2fmin, Mean %.2f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (alpha_verbose + 0.0) / (P * N + 0.0));
            }
        }

    }


    /** This step also updates beta **/
    public void resample_alpha_ESS(boolean supervised, MapWrapper[][] xsub, double[][] dict, Gamma rngG,
                                   Random rand, Normal rngN, PoissonModel2_RegCoef beta_model,
                                   boolean verbose, long start) {
        if (!supervised) {
            resample_alpha(supervised, xsub, dict, rngG, rand, beta_model, verbose, start);
        }else{
            double alpha_verbose = 0;
            double[][] alpha_prior = new double[N][P];
            for (int i = 0; i < N; i++) {
                for (int p = 0; p < this.P; p++) {
                    alpha_prior[i][p] = resample_alpha_laplace(i, p, xsub, dict, rngG, rand);
                }
            }
            // elliptical slice sampling
            double[][] alpha_new = beta_model.ElipticalSliceSamplerJoint(rand, rngN, alpha_prior, this.alpha, start,
                    verbose);
//            System.out.println(Arrays.toString(alpha_prior[0]));
//            System.out.println(Arrays.toString(alpha[0]));
//            System.out.println(Arrays.toString(alpha_new[0]));
            int count = 0;
            for (int i = 0; i < N; i++) {
                for (int p = 0; p < this.P; p++) {
                    // todo: this truncation step does change the likelihood space, danger??
                    if(alpha_new[i][p] < 0) count++;
                    this.alpha[i][p] = Math.max(alpha_new[i][p], 0);
                }
            }
            if (verbose) {
                System.out.print("finish alpha: ");
                System.out.printf("%.2fmin, Mean %.2f\n", (double) (System.currentTimeMillis() - start) / 1000 / 60,
                        (alpha_verbose + 0.0) / (P * N + 0.0));
            }
        }
    }
    public void resample_alpha_single(int i, int p, MapWrapper[][] xsub, double[][] dict, Gamma rngG, Random rand) {
        resample_alpha_single_truncated(i, p, xsub, dict, rngG, rand, 0, Double.MAX_VALUE);
    }

    public void resample_alpha_single_truncated(int i, int p, MapWrapper[][] xsub, double[][] dict, Gamma rngG,
                                                  Random rand, double min, double max){
        double alpha_verbose = 0;
        // use only index 0 to P-1, as they are the slots for dict decomposition
        double shape = VectorUtil.pick13_sum(i, p, xsub, 0, P - 1) + (this.a_sl-1) * this.z[i][p] + 1;
        double rate = VectorUtil.vectorSum(dict[p]) + this.b_sl * this.z[i][p] + this.b_sp * (1-this.z[i][p]);
        if(min == 0 & max == Double.MAX_VALUE){
            this.alpha[i][p] = rngG.nextDouble(shape,rate);
        }else{
            this.alpha[i][p] = TruncGamma.truncGamma(shape, rate, min, max, rand);
        }
        return;
    }
    public double resample_alpha_laplace(int i, int p, MapWrapper[][] xsub, double[][] dict, Gamma rngG,
                                                Random rand){
        double alpha_sample = 0;
        // use only index 0 to P-1, as they are the slots for dict decomposition
        double shape = VectorUtil.pick13_sum(i, p, xsub, 0, P - 1) + (this.a_sl-1) * this.z[i][p] + 1;
        double rate = VectorUtil.vectorSum(dict[p]) + this.b_sl * this.z[i][p] + this.b_sp * (1-this.z[i][p]);
        double mu = shape/rate;
        double sd = Math.sqrt(shape / rate / rate);
//        System.out.printf("%.7f %.7f\n", mu, sd);
        double sample = rand.nextGaussian() * sd + mu;
        while(sample <= 0) sample = rand.nextGaussian() * sd + mu;
        return(sample);
    }


    public void resample_gamma(Beta rngBe, boolean verbose, long start){
          // This seems to be a bad idea

//        int[] zsum = VectorUtil.apply_sum(2, this.z);
//        double gamma_verbose = 0;
//        for(int p = 0; p < P; p++){
//            this.gamma[p] = rngBe.nextDouble(this.a_hyper + zsum[p],  this.b_hyper + this.N - zsum[p]);
//            gamma_verbose += this.gamma[p];
//        }
//        if(verbose) {
//            double spar = VectorUtil.vectorSum(zsum) / (N * P + 0.0);
//            System.out.print("finish gamma: ");
//            System.out.printf("%.2fmin, Mean %.2f, Sparsity: %.4f \n",
//                        (double) (System.currentTimeMillis() - start) / 1000 / 60,
//                        (gamma_verbose + 0.0) / (P + 0.0),
//                        spar);
//        }
    }
}
