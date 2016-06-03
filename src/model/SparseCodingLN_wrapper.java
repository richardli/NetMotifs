package model;

import util.VectorUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Wrapper class for sparse coding; separate the process from R
 * <p/>
 * Created by zehangli on 12/14/15.
 *
 javalib -Xms10g model/SparseCodingLN_wrapper 10 1 1 1 1000 1000 1 500 98765 0.01 g1p1
 java -cp .:../library/\* -Xms4g model/SparseCodingLN_wrapper 10 1 1 1 1000 1000 1 500 1234 0.01 Sampled_g1p1
 */
public class SparseCodingLN_wrapper {
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
        double sd = Double.parseDouble(args[9]);

        String motifFile = "../../MotifwithNeighbour/clean/Motifs_" + args[10] + ".txt";
        String dictFile = "../../MotifwithNeighbour/dict/clean_dict_" + args[10] + ".txt";
        String saveFile = "../../MotifwithNeighbour/fit/run_alpha_" + args[10] + ".txt";

        String motif2File = "../../MotifwithNeighbour/clean/MotifsMutual_" + args[10] + ".txt";
        String deltaOutFile = "../../MotifwithNeighbour/fit/Delta" + args[10] + ".txt";

        BufferedReader mbr = new BufferedReader(new FileReader(motifFile));
        BufferedReader mbr2 = new BufferedReader(new FileReader(motif2File));
        BufferedReader dbr = new BufferedReader(new FileReader(dictFile));
        String line;

        ArrayList<int[]> motifArray = new ArrayList<int[]>();
        ArrayList<Double[]> motif2Array = new ArrayList<Double[]>();
        ArrayList<double[]> dictArray = new ArrayList<double[]>();

        while ((line = mbr.readLine()) != null) {
            String[] field = line.split(" ");
            int[] motifTemp = new int[120];
            for (int i = 0; i < 120; i++) {
                motifTemp[i] = (int) Double.parseDouble(field[i]);
            }
            motifArray.add(motifTemp);
        }
        while ((line = mbr2.readLine()) != null) {
            String[] field = line.split(" ");
            Double[] motifTemp = new Double[120];
            for (int i = 0; i < 120; i++) {
                motifTemp[i] = Double.parseDouble(field[i]);
            }
            motif2Array.add(motifTemp);
        }

        //         artificial codes to create fake motif2, test only
        for(int i = 0; i < motifArray.size(); i++){
            int ii;
            if(i == 0){
                ii = motifArray.size() - 1;
            }else{
                ii = i - 1;
            }
            Double[] motifTemp = new Double[120];
            for(int j = 0; j < 120; j++){
                motifTemp[j] = (motifArray.get(ii)[j] == 0) ? 0 : Math.log(motifArray.get(ii)[j]);
            }
            motif2Array.add(motifTemp);
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
        double[][] motif2Vec = new double[motifArray.size()][120];
        double[][] dictVec = new double[dictArray.size()][120];

        for(int i = 0; i < motifArray.size(); i++){
            for(int j = 0; j < 120; j++){
                motifVec[i][j] = motifArray.get(i)[j];
                motif2Vec[i][j] = motif2Array.get(i)[j];
            }
        }


        for(int i = 0; i < dictArray.size(); i++){
            for(int j = 0; j < 120; j++){
                dictVec[i][j] = dictArray.get(i)[j];
            }
        }
        System.out.println("start fitting model");
        SparseCodingLN scModel = new SparseCodingLN();
        double[][] alpha = scModel.main(a, b, c, d, epsilon, T, thin, burn, seed, motifVec, dictVec,
                motif2Vec, sd, deltaOutFile);
        VectorUtil.save2d(alpha, saveFile);
    }
}
