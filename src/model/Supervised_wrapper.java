package model;

import cern.colt.matrix.tdouble.algo.DoublePartitioning;
import util.VectorUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Wrapper class for sparse coding; separate the process from R
 * <p/>
 * Created by zehangli on 8/2/15.
 *
 java7lib -Xms10g model/Supervised_wrapper 10 1 1 1 1000 1000 1 500 98765 100 2 g3p1 g3p1 g3p1
 */
public class Supervised_wrapper {
    public static void main(String[] args) throws Exception {

        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);
        double d = Double.parseDouble(args[3]);
        double epsilon = Double.parseDouble(args[4]);
        int T = Integer.parseInt(args[5]);
        int thin = Integer.parseInt(args[6]);
        int burn = Integer.parseInt(args[7]);
        int seed = Integer.parseInt(args[8]);
        double tau_a = Double.parseDouble(args[9]);
        double tau_b = Double.parseDouble(args[10]);
//        String motifFile = "../data/motif_counts/clean_motifs_" + args[9] + ".txt";
//        String dictFile = "../data/motif_counts/clean_dict_" + args[10] + ".txt";

//        String motifFile = "test-motif.txt";
//        String dictFile = "test-dict.txt";
//        String outcomeFile = "test-Y.txt";
//        String saveFile = "../data/motif_counts/test-run-beta.txt";
//        BufferedReader ybr = new BufferedReader(new FileReader(outcomeFile));

        String motifFile = "../data/motif_counts/clean_motifs_" + args[11] + ".txt";
        String dictFile = "../data/motif_counts/clean_dict_" + args[12] + ".txt";
        String saveFile = "../data/motif_counts/run_beta_" + args[13] + ".txt";
        BufferedReader mbr = new BufferedReader(new FileReader(motifFile));
        BufferedReader dbr = new BufferedReader(new FileReader(dictFile));

        String line;

        ArrayList<int[]> motifArray = new ArrayList<int[]>();
        ArrayList<double[]> dictArray = new ArrayList<double[]>();

        while ((line = mbr.readLine()) != null) {
            String[] field = line.split(" ");
            // last one is Y
            int[] motifTemp = new int[121];
            for (int i = 0; i < 121; i++) {
                motifTemp[i] = Integer.parseInt(field[i]);
            }
            motifArray.add(motifTemp);
        }
        while ((line = dbr.readLine()) != null) {
            String[] field = line.split(" ");
            double[] dictTemp = new double[120];
            for (int i = 0; i < 120; i++) {
                dictTemp[i] = Integer.parseInt(field[i]);
            }
            dictArray.add(dictTemp);
        }

        int[][] motifVec = new int[motifArray.size()][120];
        double[][] dictVec = new double[dictArray.size() - 2][120];
        int[] Y = new int[motifArray.size()];

//        int counter = 0;
//        while ((line = ybr.readLine()) != null) {
//                Y[counter] = Integer.parseInt(line);
//                counter++;
//        }

        for(int i = 0; i < motifArray.size(); i++){
            Y[i] = motifArray.get(i)[120];
            for(int j = 0; j < 120; j++){
                motifVec[i][j] = motifArray.get(i)[j];
            }
        }

        for(int i = 0; i < dictArray.size() -2; i++){
            for(int j = 0; j < 120; j++){
                dictVec[i][j] = dictArray.get(i + 2)[j];
            }
        }
        System.out.println("start fitting model");
        SupervisedSparseCoding scModel = new SupervisedSparseCoding();
        double[][] alpha = scModel.main(a, b, c, d, epsilon, T, thin, burn, seed,
                                        motifVec, dictVec, Y, tau_a, tau_b, 0.1, args[13]);
        VectorUtil.save2d(alpha, saveFile);
    }
}
