package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MathUtil {

    public static int percentile(ArrayList<Integer> arr, double per) {
        int n = arr.size() - 1;
        if (n <= 0) return (Integer.MAX_VALUE);
        int m = (int) (n * per);
        if (m == 0) m++;
        Collections.sort(arr);
        return (arr.get(m));
    }

    public static int UnionTwoArrayLists(ArrayList<Long> x, ArrayList<Long> y) {
        int total;
        Set<Long> union = new HashSet<Long>();
        union.addAll(x);
        union.addAll(y);
        total = union.size();
        return (total);
    }

    // add another two vectors y and z to x
    public static ArrayList<Integer> ArraySum(ArrayList<Integer> x, ArrayList<Integer> y) {

        ArrayList<Integer> z = new ArrayList<Integer>();
        for (int i = 0; i < y.size(); i++) {
            z.add(x.get(i) + y.get(i));
        }
        return (z);
    }

    // add another two vectors y and z to x
    public static void StringAdd(int[] x, int[] y, int[] z) {
        for (int i = 0; i < y.length; i++) {
            x[i] += y[i] + z[i];
        }
    }

    // perform sum over int vector
    public static int vectorSum(int[] x) {
        int sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return (sum);
    }

    public static int whichmin(Double[] x) {
        int which = 0;
        double min = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] < min) {
                which = i;
                min = x[i];
            }
        }
        return (which);
    }

    public static int whichmax(Double[] x) {
        int which = x.length - 1;
        double max = x[x.length - 1];
        for (int i = x.length - 1; i >= 0; i--) {
            if (x[i] > max) {
                which = i;
                max = x[i];
            }
        }
        return (which);
    }

    public static int whichmin(double[] x) {
        int which = 0;
        double min = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] < min) {
                which = i;
                min = x[i];
            }
        }
        return (which);
    }

    public static int whichmax(double[] x) {
        int which = x.length - 1;
        double max = x[x.length - 1];
        for (int i = x.length - 1; i >= 0; i--) {
            if (x[i] > max) {
                which = i;
                max = x[i];
            }
        }
        return (which);
    }
}