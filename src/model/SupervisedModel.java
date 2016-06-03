package model;

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
    int M;

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
