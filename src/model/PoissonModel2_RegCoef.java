package model;

import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.Normal;

import java.util.Random;

/**
 * Created by zehangli on 7/20/16.
 */
public class PoissonModel2_RegCoef {
    boolean supervised;
    int N;
    int P;
    int[] y;
    double beta0;
    double[] beta;
    double a_tau;
    double b_tau;
    double tau;

    PoissonModel2_RegCoef(boolean supervised){
        this.supervised = supervised;
    }

    PoissonModel2_RegCoef(boolean supervised, int[] y, int P, double a_tau, double b_tau){
        this.supervised = supervised;
        this.N = y.length;
        this.y = y;
        this.P = P;
        this.a_tau = a_tau;
        this.b_tau = b_tau;
        this.beta = new double[P];
    }
    public void initial_beta(Gamma rngG, Normal rngN){
        if(!supervised) return;

        tau = rngG.nextDouble(a_tau, b_tau);
        beta0 = rngN.nextDouble(0, 1.0/tau);
        for(int i = 0; i < P; i++){
            beta[i] = rngN.nextDouble(0, 1.0/tau);
        }


    }

    public void resample_beta(boolean supervised, Random rand, Normal rngN,
                              PoissonModel2_DictLoading alpha_model,
                              long start, boolean verbose){
        if(!supervised) return;

        this.ElipticalSliceSampler(rand, rngN, alpha_model.alpha, start, verbose);
    }

    public void resample_tau(boolean supervised, Gamma rngG, long start, boolean verbose){
        if(!supervised) return;

        // todo: why not stable??

//        double new_b_tau = b_tau + beta0*beta0/2.0;
//        for(int i = 0; i < P; i++) new_b_tau += beta[i] * beta[i] / 2.0;
//        tau = rngG.nextDouble(a_tau + (P+1.0)/2.0, new_b_tau);
//        if(verbose){
//            System.out.printf("%.2fmin,  %.4f Tau\n",
//                    (double) (System.currentTimeMillis() - start) / 1000 / 60,
//                    tau);
//        }
    }


    public void ElipticalSliceSampler(Random rand, Normal rngN,
                                      double[][] alpha_model,
                                      long start, boolean verbose){

        // get prior sample
        double[] prior_sample = new double[P+1];
        double[] current_sample = new double[P+1];
        current_sample[0] = this.beta0;
        prior_sample[0] = rngN.nextDouble(0, 1.0/tau);
        for(int i = 1; i < P+1; i++){
            prior_sample[i] = rngN.nextDouble(0, 1.0/tau);
            current_sample[i] = this.beta[i-1];
        }

        double current_lik = loglik(alpha_model, current_sample);
        // get acceptance threshold
        double test = Math.log(rand.nextDouble()) + current_lik;

        // set up bracket
        double phi = rand.nextDouble() * 2 * Math.PI;
        double phi_min = phi - 2 * Math.PI;
        double phi_max = phi;
        double[] beta_new = new double[P+1];
        double new_lik;
        int count = 0;
        while(true){
            double max_dist = 0;
            for(int j = 0; j < this.P + 1; j++){
                beta_new[j] = current_sample[j] * Math.cos(phi) + prior_sample[j] * Math.sin(phi);
                double diff = Math.abs(beta_new[j] - current_sample[j]);
                if(diff > max_dist) max_dist = diff;
            }
            new_lik = loglik(alpha_model, beta_new);

            if(new_lik >= test){
                break;
            }else if(max_dist < 1e-9){
                break;
            }else{
                count ++;
                if(phi > 0) phi_max = phi;
                if(phi < 0) phi_min = phi;
                phi = rand.nextDouble() * (phi_max - phi_min) + phi_min;
            }
        }

        if(new_lik >= test){
            this.beta0 = beta_new[0];
            for(int j=1; j < this.P + 1; j++){
            this.beta[j-1] = beta_new[j];
            }
        }
        if(verbose){
            System.out.print("finish Elliptical SS: ");
            System.out.printf("%.2fmin,  %d iterations, %.4f improvement in loglik\n" +
                              "betas: %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f ...\n",
                    (double) (System.currentTimeMillis() - start) / 1000 / 60,
                    count,
                    new_lik - current_lik,
                    beta[0], beta[1], beta[2], beta[3], beta[4], beta[5], beta[6], beta[7] );
        }
    }

    public double[][] ElipticalSliceSamplerJoint(Random rand, Normal rngN,
                                      double[][] alpha_prior,
                                      double[][] alpha_now,
                                      long start, boolean verbose){

        // get prior sample
        double[] prior_sample = new double[P+1];
        for(int i = 0; i < P+1; i++) prior_sample[i] = rngN.nextDouble(0, 1.0/tau);
        double current_lik = loglik(alpha_prior, prior_sample);
        // get acceptance threshold
        double test = Math.log(rand.nextDouble()) + current_lik;

        // set up bracket for alpha
        double phi = rand.nextDouble() * 2 * Math.PI;
        double phi_min = phi - 2 * Math.PI;
        double phi_max = phi;

        double[] beta_new = new double[P+1];
        double[][] alpha_new = new double[N][P];
        double new_lik = 0;
        int count = 0;
        while(count < 1000){
            double max_dist = Math.abs(beta_new[0] - this.beta0);
            boolean out_of_bound = false;
            for(int i = 0; i < this.N; i++){
                for(int j = 0; j < this.P; j++){
                    alpha_new[i][j] = alpha_now[i][j] * Math.cos(phi) + alpha_prior[i][j] * Math.sin(phi);
                    // todo: is breaking out better than just setting alpha_new to be zero?
                    // maybe? since there is a pretty wide range of phi satisfy the positive constraint (0, pi/2)
                    // alpha_new[i][j] = alpha_new[i][j] > 0 ? alpha_new[i][j] : 0;
                    out_of_bound = alpha_new[i][j] <= 0;
                    if(out_of_bound) break;
                    double diff = Math.abs(alpha_new[i][j] - alpha_now[i][j]);
                    if(diff > max_dist) max_dist = diff;
                }
                if(out_of_bound) break;
            }
            if(out_of_bound){
                if(phi > 0) phi_max = phi;
                if(phi < 0) phi_min = phi;
                phi = rand.nextDouble() * (phi_max - phi_min) + phi_min;
                continue; // does not count as an iteration
            }


            new_lik = loglik(alpha_new, beta_new);

            if(new_lik >= test){
                break;
            }else if(max_dist < 1e-9){
                break;
            }else{
                count ++;
                if(phi > 0) phi_max = phi;
                if(phi < 0) phi_min = phi;
                phi = rand.nextDouble() * (phi_max - phi_min) + phi_min;
            }
        }

        if(new_lik >= test){
            this.ElipticalSliceSampler(rand, rngN, alpha_new, start, verbose);
            return(alpha_new);
        }else{
            this.ElipticalSliceSampler(rand, rngN, alpha_now, start, verbose);
            return(alpha_now);
        }
    }


    public double loglik(PoissonModel2_DictLoading alpha_model){
        double lik = 0;
        for(int i = 0; i < this.N; i++){
            double tmp = beta0;
            for(int j = 0; j < this.P; j++){
                tmp += beta[i] * alpha_model.alpha[i][j];
            }
            // Y is parameterized as {0, 1}
            lik += tmp * y[i] - Math.log(1 + Math.exp(tmp));
        }
        return lik;
    }

    public double loglik(PoissonModel2_DictLoading alpha_model, double[] betas){
        double lik = 0;
        for(int i = 0; i < this.N; i++){
            double tmp = betas[0];
            for(int j = 0; j < this.P; j++){
                tmp += betas[j+1] * alpha_model.alpha[i][j];
            }
            // Y is parameterized as {0, 1}
            lik += tmp * y[i] - Math.log(1 + Math.exp(tmp));
        }
        return lik;
    }

    public double loglik(double[][] alphas, double[] betas){
        double lik = 0;
        for(int i = 0; i < this.N; i++){
            double tmp = betas[0];
            for(int j = 0; j < this.P; j++){
                tmp += betas[j+1] * alphas[i][j];
            }
            // Y is parameterized as {0, 1}
            lik += tmp * y[i] - Math.log(1 + Math.exp(tmp));
        }
        return lik;
    }




}
