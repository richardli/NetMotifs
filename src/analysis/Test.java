package analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zehangli on 7/13/15.
 */
public class Test {

    Set<Integer> test = new HashSet<Integer>();


    public static void main(String[] args) {
        // psvm -> main
        // sout -> sysout
        // souf -> sysout printf
        Test a0 = new Test();
        a0.test.add(1);
        a0.test.add(null);
        System.out.print(a0.test.size());

        double[][] test = new double[2][3];
        for (double[] row: test) Arrays.fill(row, 123.0);
        System.out.println("first");
        System.out.println(Arrays.toString(test[0]));
        System.out.println("second");
        System.out.println(Arrays.toString(test[1]));
        System.out.println(test[0][1]);

    }
}
