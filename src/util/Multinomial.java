package util;
import java.util.ArrayList;
import java.util.Random;


public class Multinomial {
	int n;

	public Multinomial(){}

	public Multinomial(int n){
		this.n = n;
	}

	public int[] Sample(double[] prob, double sum, int total){
		this.n = prob.length;
		int[] sample = new int[this.n];
		double[] cumsum = new double[this.n];
		cumsum[0] = prob[0];
		for(int i = 1; i < this.n; i++){
			cumsum[i] = cumsum[i-1] + prob[i];
		}

		Random rand = new Random();		
		for(int i = 0; i < total; i++){
			double u = rand.nextDouble() * sum;
			for(int j = 0; j < this.n; j++){
				if(u < cumsum[j]){sample[j]++; break;}
			}
		}
		return(sample);
	}
	
	public ArrayList<Integer> SampleAsList(double[] prob, double sum, int total){
		this.n = prob.length;
		ArrayList<Integer> sample = new ArrayList<Integer>();
		double[] cumsum = new double[this.n];
		cumsum[0] = prob[0];
		for(int i = 1; i < this.n; i++){
			cumsum[i] = cumsum[i-1] + prob[i];
		}

		Random rand = new Random();		
		for(int i = 0; i < total; i++){
			double u = rand.nextDouble() * sum;
			for(int j = 0; j < this.n; j++){
				if(u < cumsum[j]){sample.add(j); break;}
			}
		}
		return(sample);
	}
}
