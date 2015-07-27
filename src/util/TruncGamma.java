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
		GammaDistribution gamma = new GammaDistribution(a, b);
		if(min < 0) min = 0;
		if(max < 0) max = 0;
		double vmax = gamma.cumulativeProbability(max);
		double vmin = gamma.cumulativeProbability(min);
		double u = rand.nextDouble() *(vmax - vmin) + vmin;
		
		double sample = gamma.inverseCumulativeProbability(u);
		return(sample);
	}
}
