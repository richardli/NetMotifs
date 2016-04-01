package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VectorUtil {
    /**
     * Save a 2-d array to file
     */
    public static void save2d(double[][] vec, String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(VectorUtil.write2d(vec));
        bw.close();
    }
    /**
     * convert a 2-d array to string
     */
    public static String write2d(double[][] array){
        StringBuilder out = new StringBuilder();
        for(int j = 0; j < array.length; j++){
            out.append(Arrays.toString(array[j]).replace("[", "").replace("]", "\n"));
        }
        return(out.toString());
    }
    public static String write2d(int[][] array){
        StringBuilder out = new StringBuilder();
        for(int j = 0; j < array.length; j++){
            out.append(Arrays.toString(array[j]).replace("[", "").replace("]", "\n"));
        }
        return(out.toString());
    }


    /**
     * Save a 3-d array to file
     */
    public static void save3d(double[][][] vec, String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(VectorUtil.write3d(vec));
        bw.close();
    }
    /**
     * convert a 3-d array to string
     */
    public static String write3d(double[][][] array){
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[0].length; j++){
                out.append(Arrays.toString(array[i][j]).replace("[", "").replace("]", "\n"));
            }
        }
        return(out.toString());
    }
    public static String write3d(int[][][] array){
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[0].length; j++){
                out.append(Arrays.toString(array[i][j]).replace("[", "").replace("]", "\n"));
            }
        }
        return(out.toString());
    }

    /**
     * fill selected dimension of a 3-d array
     *
     */
    public static void fillselected(double[][][] arraylong, int ind, double[][] array){
        for(int i = 0; i < arraylong[ind].length; i++){
            for(int j = 0; j < arraylong[ind][0].length; j++){
                arraylong[ind][i][j] = array[i][j];
            }
        }
    }

    public static void fillselected(int[][][] arraylong, int ind, int[][] array){
        for(int i = 0; i < arraylong[ind].length; i++){
            for(int j = 0; j < arraylong[ind][0].length; j++){
                arraylong[ind][i][j] = array[i][j];
            }
        }
    }

    /**
     * Calculate percentile for an integer array
     * Naively ignoring ties
     *
     * @param arr array
     * @param per percentile
     * @return
     */
    public static int percentile(ArrayList<Integer> arr, double per) {
        int n = arr.size() - 1;
        if (n <= 0) return (Integer.MAX_VALUE);
        int m = (int) (n * per);
        if (m == 0) m++;
        Collections.sort(arr);
        return (arr.get(m));
    }

    /**
     * Union two Long ArrayLists
     *
     * @param x array 1
     * @param y array 2
     * @return
     */
    public static int UnionTwoArrayLists(ArrayList<Long> x, ArrayList<Long> y) {
        int total;
        Set<Long> union = new HashSet<Long>();
        union.addAll(x);
        union.addAll(y);
        total = union.size();
        return (total);
    }

    /**
     * element-wise adding two integer ArrayLists
     * TODO: this is duplicated with add method!
     *
     * @param x array 1
     * @param y array 2
     * @return
     */
    public static ArrayList<Integer> ArraySum(ArrayList<Integer> x, ArrayList<Integer> y) {

        ArrayList<Integer> z = new ArrayList<Integer>();
        for (int i = 0; i < y.size(); i++) {
            z.add(x.get(i) + y.get(i));
        }
        return (z);
    }

    /**
     * Element-wise adding two integer arrays to the first array
     * TODO: this is duplicated with add method!
     *
     * @param x array 1 (motified)
     * @param y array 2
     * @param z array 3
     */
    public static void StringAdd(int[] x, int[] y, int[] z) {
        for (int i = 0; i < y.length; i++) {
            x[i] += y[i] + z[i];
        }
    }

    /**
     * Sum of integer array
     *
     * @param x
     * @return
     */
    public static int vectorSum(int[] x) {
        int sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return (sum);
    }

    public static int vectorSum(int[][] x) {
        int sum = 0;
        for (int i = 1; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) sum += x[i][j];
        }
        return (sum);
    }

    /**
     * Sum of double array
     *
     * @param x
     * @return
     */
    public static double vectorSum(double[] x) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return (sum);
    }

    public static double vectorSum(double[][] x) {
        double sum = 0;
        for (int i = 1; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) sum += x[i][j];
        }
        return (sum);
    }


    /**
     * Find min element position
     *
     * @param x
     * @return
     */
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

    /**
     * Find max element position
     *
     * @param x
     * @return
     */
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

    /**
     * Find min element position
     *
     * @param x
     * @return
     */
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

    /**
     * Find max element position
     *
     * @param x
     * @return
     */
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


    /**
     * Extend the ArrayList by repeating the last element until length len
     *
     * @param array the ArrayList
     * @param len   expected length
     */
    public static void extElement(ArrayList<Integer> array, int len) {
        if (len == 0 | array.size() == len) return;
        int last;
        if (array.size() == 0) {
            last = 0;
        } else {
            last = array.get(array.size() - 1);
        }
        for (int i = array.size(); i < len; i++) {
            array.add(last);
        }
    }

    /**
     * add to ArrayList a new element: (inc + last element)
     *
     * @param array the ArrayList
     * @param inc   the increment to last
     */
    public static void incNewBy(ArrayList<Integer> array, int inc) {
        if (array.size() == 0) {
            array.add(inc);
        } else {
            int temp = array.get(array.size() - 1);
            array.add(temp + inc);
        }
    }

    /**
     * Extend the ArrayList to desired length and add to it a new element: (inc + last element)
     *
     * @param array the ArrayList
     * @param len   the length
     * @param inc   the increment to last
     */

    public static void incToLenBy(ArrayList<Integer> array, int len, int inc) {
        if (array.size() == len) {
            array.set(len - 1, array.get(len - 1) + inc);
        } else if (array.size() < len) {
            extElement(array, len - 1);
            incNewBy(array, inc);
        } else {
            System.out.println("Array too long!");
            System.out.println(len);
            System.out.println(array.size());
        }
    }

    /**
     * Sum over second dimension for given first and third index
     *
     * @param i first index
     * @param k third index
     * @param x array
     * @return
     */
    public static double pick13_sum(int i, int k, double[][][] x) {
        double sum = 0.0;
        for (int j = 0; j < x[0].length; j++) {
            sum += x[i][j][k];
        }
        return (sum);
    }

    /**
     * Sum over second dimension for given first and third index
     *
     * @param i first index
     * @param k third index
     * @param x array
     * @return
     */
    public static int pick13_sum(int i, int k, int[][][] x) {
        int sum = 0;
        for (int j = 0; j < x[0].length; j++) {
            sum += x[i][j][k];
        }
        return (sum);
    }

    /**
     * Sum over second dimension for given first and third index
     *
     * @param i first index
     * @param k third index
     * @param x array
     * @return
     */
    public static int pick13_sum(int i, int k, MapWrapper[][] x) {
        int sum = 0;
        for (int j = 0; j < x[0].length; j++) {
            if (x[i][j].x.get(k) != null) {
                sum += x[i][j].x.get(k);
            }
        }
        return (sum);
    }

    /**
     * Apply(x, ndim, sum) in R
     *
     * @param ndim which dimension to sum
     * @param x    double 2-d array
     * @return
     */
    public static double[] apply_sum(int ndim, double[][] x) {
        double[] sum;
        if (ndim == 1) {
            sum = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    sum[i] += x[i][j];
                }
            }
            return (sum);
        } else {
            sum = new double[x[0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    sum[j] += x[i][j];
                }
            }
            return (sum);
        }
    }

    /**
     * Apply(x, ndim, sum) in R
     *
     * @param ndim which dimension to sum
     * @param x    int 2-d array
     * @return
     */
    public static int[] apply_sum(int ndim, int[][] x) {
        int[] sum;
        if (ndim == 1) {
            sum = new int[x.length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    sum[i] += x[i][j];
                }
            }
            return (sum);
        } else {
            sum = new int[x[0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    sum[j] += x[i][j];
                }
            }
            return (sum);
        }
    }

    /**
     * Apply(x, ndim, sum) in R
     *
     * @param ndim which dimension to sum
     * @param x    double 3-d array
     * @return
     */
    public static double[] apply_sum(int ndim, double[][][] x) {
        double[] sum;
        if (ndim == 1) {
            sum = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[i] += x[i][j][k];
                    }
                }
            }
            return (sum);
        } else if (ndim == 2) {
            sum = new double[x[0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[j] += x[i][j][k];
                    }
                }
            }
            return (sum);
        } else {
            sum = new double[x[0][0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[k] += x[i][j][k];
                    }
                }
            }
            return (sum);
        }
    }

    /**
     * Apply(x, ndim, sum) in R
     *
     * @param ndim which dimension to sum
     * @param x    int 3-d array
     * @return
     */
    public static int[] apply_sum(int ndim, int[][][] x) {
        int[] sum;
        if (ndim == 1) {
            sum = new int[x.length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[i] += x[i][j][k];
                    }
                }
            }
            return (sum);
        } else if (ndim == 2) {
            sum = new int[x[0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[j] += x[i][j][k];
                    }
                }
            }
            return (sum);
        } else {
            sum = new int[x[0][0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[0].length; j++) {
                    for (int k = 0; k < x[0][0].length; k++) {
                        sum[k] += x[i][j][k];
                    }
                }
            }
            return (sum);
        }
    }


    /**
     * Element wise adding two arrays
     *
     * @param x
     * @param y
     * @return
     */
    public static double[] Add(double[] x, double[] y) {
        int n = y.length;
        for (int i = 0; i < n; i++) {
            x[i] += y[i];
        }
        return (x);
    }

    public static int[] Add(int[] x, int[] y) {
        int n = y.length;
        for (int i = 0; i < n; i++) {
            x[i] += y[i];
        }
        return (x);
    }

    public static int[][] Add(int[][] x, int[][] y) {
        int n = y.length;
        int m = y[1].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x[i][j] += y[i][j];
            }
        }
        return (x);
    }

    public static double[][] Add(double[][] x, double[][] y) {
        int n = y.length;
        int m = y[1].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x[i][j] += y[i][j];
            }
        }
        return (x);
    }

    public static double[] Multi(double[] x, double k) {
        int n = x.length;
        for (int i = 0; i < n; i++) {
            x[i] *= k;
        }
        return (x);
    }

    public static double[][] Multi(double[][] x, double k) {
        int n = x.length;
        int m = x[1].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                x[i][j] *= k;
            }
        }
        return (x);
    }

    /**
     * Element-wise multiplying an array by a constant
     *
     * @param x array
     * @param k constant
     * @return
     */
    public static double[] Multi(int[] x, double k) {
        int n = x.length;
        double[] y = new double[x.length];
        for (int i = 0; i < n; i++) {
            y[i] = ((double) x[i]) * k;
        }
        return (y);
    }

    public static double[][] Multi(int[][] x, double k) {
        int n = x.length;
        int m = x[1].length;
        double[][] y = new double[x.length][x[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                y[i][j] = ((double) x[i][j]) * k;
            }
        }
        return (y);
    }

    public static double CrossProd(double[] x, double[] y){
        double out = 0;
        // comment out this step for speed boost...a little unsafe!!
        // if(x.length != y.length){System.exit(1);}
        for(int i = 0; i < x.length; i++){
            out += x[i] * y[i];
        }
        return(out);
    }

    public static double CrossProd(double[] x, int[] y){
        double out = 0;
        // comment out this step for speed boost...a little unsafe!!
        // if(x.length != y.length){System.exit(1);}
        for(int i = 0; i < x.length; i++){
            out += x[i] * y[i];
        }
        return(out);
    }

}
