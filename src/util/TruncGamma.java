package util;

import java.util.Random;

import org.apache.commons.math3.distribution.GammaDistribution;

public class TruncGamma {
	
	public double truncGamma(double a, double b, 
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
