package model;

import util.VectorUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Wrapper class for sparse coding; separate the process from R
 * <p/>
 * Created by zehangli on 8/1/15.
 *
  java7lib -Xms10g model/SparseCoding_wrapper 10 1 1 1 1000 1000 1 500 98765 g1p1 g1p1 g1p1
 */
public class SparseCoding_wrapper {
    public static void main(String[] args) throws IOException {

        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);
        double d = Double.parseDouble(args[3]);
        double epsilon = Double.parseDouble(args[4]);
        int T = Integer.parseInt(args[5]);
        int thin = Integer.parseInt(args[6]);
        int burn = Integer.parseInt(args[7]);
        int seed = Integer.parseInt(args[8]);

        String motifFile = "../data/motif_counts/clean_motifs_" + args[9] + ".txt";
        String dictFile = "../data/motif_counts/clean_dict_" + args[10] + ".txt";
        String saveFile = "../data/motif_counts/run_alpha_" + args[11] + ".txt";
//          String motifFile = "../data/FluMotifWithNeighbour.txt";
//          String dictFile = "../data/dict0.txt";
//          String saveFile = "../data/FluMotifWithNeighbour_alpha.txt";

        BufferedReader mbr = new BufferedReader(new FileReader(motifFile));
        BufferedReader dbr = new BufferedReader(new FileReader(dictFile));
        String line;

        ArrayList<int[]> motifArray = new ArrayList<int[]>();
        ArrayList<double[]> dictArray = new ArrayList<double[]>();

        while ((line = mbr.readLine()) != null) {
            String[] field = line.split(" ");
            int[] motifTemp = new int[120];
            for (int i = 0; i < 120; i++) {
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
        double[][] dictVec = new double[dictArray.size()][120];

        for(int i = 0; i < motifArray.size(); i++){
            for(int j = 0; j < 120; j++){
                motifVec[i][j] = motifArray.get(i)[j];
            }
        }

        for(int i = 0; i < dictArray.size(); i++){
            for(int j = 0; j < 120; j++){
                dictVec[i][j] = dictArray.get(i)[j];
            }
        }
        System.out.println("start fitting model");
        SparseCoding scModel = new SparseCoding();
        double[][] alpha = scModel.main(a, b, c, d, epsilon, T, thin, burn, seed, motifVec, dictVec);
        VectorUtil.save2d(alpha, saveFile);
    }
}
