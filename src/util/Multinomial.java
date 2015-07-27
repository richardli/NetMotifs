package util;
import java.util.ArrayList;
import java.util.Random;


/**
 *      Class to hold a multinomial distribution object
 *
 *      @author zehangli
 *      last update: 07-27-2015
 */

public class Multinomial {

	// total number to select from
    int n;

    // empty constructor
	public Multinomial(){}

    // constructor specifying n
	public Multinomial(int n){
		this.n = n;
	}

    /**
     * Sample a multinomial vector
     *
     * @param prob array of probabilities, does not need to be normalized
     * @param sum  sum of prob (duplicated and no longer used) TODO: remove this argument
     * @param total number of samples to draw
     *
     * @return sampled count vector
     */
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
			double u = rand.nextDouble() * cumsum[this.n - 1];
			for(int j = 0; j < this.n; j++){
				if(u < cumsum[j]){sample[j]++; break;}
			}
		}
		return(sample);
	}

    /**
     * Sample a multinomial vector, save as ArrayList
     *
     * @param prob array of probabilities, does not need to be normalized
     * @param sum  sum of prob (duplicated and no longer used) TODO: remove this argument
     * @param total number of samples to draw
     *
     * @return sampled values in ArrayList
     */
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
