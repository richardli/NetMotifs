package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *  Helper class to write array to file
 *
 *  @author zehangli
 *  last update: 07-27-15
 *
 */
public class WriteArray {

    /**
     * Write one dimensional array to file
     *
     * @param one    double array
     * @param path   file path and name
     * @param append boolean for appending
     * @throws IOException
     */
    public void write(double[] one, String path, boolean append) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path, append));
        bw.write(Arrays.toString(one).replace("[", "").replace("]", ""));
        bw.write("\n");
        bw.close();
    }

    /**
     * Write two dimensional array to file
     *
     * @param two    double array
     * @param path   file path and name
     * @param append boolean for appending
     * @throws IOException
     */
    public void write(double[][] two, String path, boolean append) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(path, append));
        for (int i = 0; i < two.length; i++) {
            bw.write(Arrays.toString(two[i]).replace("[", "").replace("]", ""));
            bw.write("\n");
        }
        bw.close();
    }
}
