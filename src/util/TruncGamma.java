package util;

import java.util.Random;

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 *      Perform truncated Gamma distribution sampling
 *
 *      @author zehangli
 *      last update: 07-27-15
 *
 */
public class TruncGamma {

    /**
     *  Sample a r.v. from truncated gamma distribution
     *
     * @param a gamma alpha
     * @param b gamma beta
     * @param min lower truncation
     * @param max upper truncation
     * @param rand random number generator
     * @return
     */
	public static double truncGamma(double a, double b,
							double min, double max, Random rand){
        if(min > max){
            System.out.printf("Truncation range not right! a = %.4f, b = %.4f, on [%.4f, %.4f]\n", a, b, min, max);
            return(Double.NaN);
        }
        // Gamma in common3 is constructed with rate, scale!!!
		GammaDistribution gamma = new GammaDistribution(a, 1/b);
		if(min < 0) min = 0;
		if(max < 0) max = 0;
        double vmax;
        if(max >= Double.MAX_VALUE){
            vmax = 1;
        }else{
            try {
                vmax = gamma.cumulativeProbability(max);
            }catch(org.apache.commons.math3.exception.ConvergenceException e){
                vmax = 1;
                System.out.printf("Wrong inverse CDF! min = %.4f, max = %.4f\n",
                        min, max);
            }
        }
		double vmin = gamma.cumulativeProbability(min);
		double u = rand.nextDouble() *(vmax - vmin) + vmin;
		if(u < 0 | u > 1){
            System.out.printf("Wrong inverse CDF! min = %.4f, max = %.4f, sample = %.4f\n",
                    vmin, vmax, u);
        }
		double sample = gamma.inverseCumulativeProbability(u);
        if(sample > 100){
            System.out.printf("Large value returned! a = %.4f, b = %.4f, sample = %.4f, on [%.4f, %.4f]\n", a, b,
                    sample, min,
                    max);
        }
		return(sample);
	}
}
