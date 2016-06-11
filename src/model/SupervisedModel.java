package model;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zehangli on 6/2/16.
 */
public class SupervisedModel {

    LogisticRegressionVB vb_model;
    LogisticRegressionIRLS irls_model;
    String type;
    double[][] X;
    int[] Y;
    int N;
    double[][] X_test;
    int[] Y_test;
    int N_test;
    int pos_test;
    int M;
    int fp;
    int tp;
    int fn;
    int tn;
    int fp2;
    int tp2;
    int fn2;
    int tn2;

    // index [test_start, ..., test_end] are testing data
    public SupervisedModel(double[][] X, int[] Y, String type, int test_start, int test_end){

        for(int i = 0; i < Y.length; i++){
            if(Y[i] == 1 | Y[i] == 0){
                if(i >= test_start & i <= test_end){
                    this.N_test++;
                }else{
                    this.N++;
                }

            }
        }

        this.M = X[0].length;
        this.Y = new int[this.N];
        this.X = new double[this.N][this.M];
        this.Y_test = new int[this.N_test];
        this.X_test = new double[this.N_test][this.M];
        int counter = 0;
        int counter_test = 0;

        for(int i = 0; i < Y.length; i++){
            if(i >= test_start & i <= test_end){
                if(Y[i] == 1 | Y[i] == 0){
                    for(int j = 0; j < this.M; j++) this.X_test[counter_test][j] = X[i][j];
                    this.Y_test[counter_test] = Y[i];
                    counter_test ++;
                    this.pos_test += Y[i];
                }
                continue;
            }else{
                if(Y[i] == 1 | Y[i] == 0){
                    for(int j = 0; j < this.M; j++) this.X[counter][j] = X[i][j];
                    this.Y[counter] = Y[i];
                    counter ++;
                }
            }
        }
        this.type = type;
        System.out.println(test_start + " " + test_end + " " + this.Y.length + " " + this.Y_test.length);
    }

    public SupervisedModel(double[][] X, int[] Y, String type){
        for(int i = 0; i < Y.length; i++){
            if(Y[i] == 1 | Y[i] == 0) this.N++;
        }
        this.M = X[0].length;
        this.Y = new int[this.N];
        this.X = new double[this.N][this.M];
        int counter = 0;
        for(int i = 0; i < Y.length; i++){
            if(Y[i] == 1 | Y[i] == 0){
                for(int j = 0; j < this.M; j++) this.X[counter][j] = X[i][j];
                this.Y[counter] = Y[i];
                counter ++;
            }
        }
        this.type = type;
    }
    public void test(double[] beta){
        double[] betax = new double[this.Y_test.length];
        this.tp = this.fp = this.tn = this.fn = 0;
        for(int i = 0; i < this.Y_test.length; i++){
            betax[i] += beta[0];
            for(int j =1; j < this.M; j++){
                betax[i] += beta[j] * this.X_test[i][j];
            }
            if(betax[i] > 0 & this.Y_test[i] == 1){
                this.tp ++;
            }else if(betax[i] > 0 & this.Y_test[i] == 0){
                this.fp ++;
            }else if(betax[i] < 0 & this.Y_test[i] == 0){
                this.tn ++;
            }else if(betax[i] < 0 & this.Y_test[i] == 1){
                this.fn ++;
            }
        }

        Percentile perc = new Percentile();
        perc.setData(betax);
        double cutoff = perc.evaluate((int) ((1 - (this.pos_test + 0.0) / (this.N_test + 0.0)) * 100));

        this.tp2 = this.fp2 = this.tn2 = this.fn2 = 0;
        for(int i = 0; i < this.Y_test.length; i++){
            betax[i] += beta[0];
            for(int j =1; j < this.M; j++){
                betax[i] += beta[j] * this.X_test[i][j];
            }
            if(betax[i] > cutoff & this.Y_test[i] == 1){
                this.tp2 ++;
            }else if(betax[i] > cutoff & this.Y_test[i] == 0){
                this.fp2 ++;
            }else if(betax[i] < cutoff & this.Y_test[i] == 0){
                this.tn2 ++;
            }else if(betax[i] < cutoff & this.Y_test[i] == 1){
                this.fn2 ++;
            }
        }


    }
    public void fit(){
        this.fit(0.0);
    }

    public void fit(double tau){
        if(this.type == "IRLS"){
               this.irls_model = new LogisticRegressionIRLS(N, M, Y, X);
               this.irls_model.fit(tau, true);
        }

        if(this.type == "VB"){
            this.vb_model = new  LogisticRegressionVB();
            this.vb_model.fit(X, Y);
        }
    }

    public double[] get_coef(){
        if(this.type == "IRLS"){
            return(this.irls_model.coef());
        }else if(this.type == "VB"){
            return(this.vb_model.beta);
        }

        return(new double[M]);
    }

    public double get_loglik(){
        if(this.type == "IRLS"){
            return(this.irls_model.calculateLogL());
        }else if(this.type == "VB"){
            return(this.vb_model.calculateLogL());
        }

        return(0);
    }


}
