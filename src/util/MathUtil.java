package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MathUtil {

    /**
     * For (i, j, k) entry in D1 x D2 x D3 array,
     *    return order (starting from 0) of the entry
     * e.g., (0, 2, 1) entry in 5x6x7 array:
     *        order is 0 * 6 * 7 + 2 * 7 + 1 = 15
     * **/
    public static int orderhash(int D2, int D3, int i, int j ,int k){
        return(i * D2 * D3 + j * D3 + k);
    }

    public static double logSumOfExponentials(double x1, double x2) {
        double max = x1;
        if(x2 > x1) max = x2;
        double sum = 0.0;
        if (x1 != Double.NEGATIVE_INFINITY)
            sum += java.lang.Math.exp(x1 - max);
        if (x2 != Double.NEGATIVE_INFINITY)
            sum += java.lang.Math.exp(x2 - max);
        return max + java.lang.Math.log(sum);
    }
    /**
     * Log Gamma function
     *
     * @param x
     * @return
     */
    public static double logGamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    /**
     * Gamma function
     *
     * @param x
     * @return
     */
    public static double gamma(double x) {
        return Math.exp(logGamma(x));
    }
}