package util;

import java.util.HashMap;

/**
 * Created by zehangli on 5/3/16.
 */
public class MotifOrder {
    public static final int[] order = new int[]{1,3,5,
            7,19,31,11,23,35,15,27,39,
            43,47,50,53,57,61,
            64,88,100,
            68,104,72,107,
            76,92,110,
            80,96,114,
            84,118,
            8,20,32,12,24,36,16,28,40,
            9,21,33,13,25,37,17,29,41,
            44,45,48,51,54,55,58,59,62,
            65,66,89,90,101,102,
            69,70,105,
            73,74,108,
            77,78,93,94,111,112,
            81,82,97,98,115,116,
            85,86,119,
            2,4,6,10,22,34,14,26,38,18,30,42,
            46,49,52,56,60,63,
            67,91,103,
            71,106,75,109,
            79,95,113,
            83,99,117,
            87,120};

    public static HashMap<Integer, Integer> changeOrder(HashMap<Integer, Integer> motif){
        HashMap<Integer, Integer> newMotif = new HashMap<Integer, Integer>();

        // note the first element in motif is not used in the new motif list,
        // so everything starts with 1
        // i.e. the first element in order is 1,
        // meaning the second element gets mapped to the first element in new list
        // i.e. motif{<0, 0>, <1, 5>,...}  --> newmotif{<0, 5>, ...}
        for(int i = 0; i < 120; i++){
            if(motif.get(order[i]) != null){
                newMotif.put(i, motif.get(order[i]));
            }else{
                newMotif.put(i, 0);
            }
        }
        return(newMotif);
    }

    public static HashMap<Integer, Double> changeOrderDouble(HashMap<Integer, Double> motif){
        HashMap<Integer, Double> newMotif = new HashMap<Integer, Double>();

        // note the first element in motif is not used in the new motif list,
        // so everything starts with 1
        // i.e. the first element in order is 1,
        // meaning the second element gets mapped to the first element in new list
        // i.e. motif{<0, 0>, <1, 5>,...}  --> newmotif{<0, 5>, ...}
        for(int i = 0; i < 120; i++){
            if(motif.get(order[i]) != null){
                newMotif.put(i, motif.get(order[i]));
            }else{
                newMotif.put(i, 0.0);
            }
        }
        return(newMotif);
    }

    /**
     *   Same method when the motif HashMap is a pointer
     *
    **/

    public static void changeOrder2(HashMap<Integer, Integer> motif){
        HashMap<Integer, Integer> newMotif = new HashMap<Integer, Integer>();

        // note the first element in motif is not used in the new motif list,
        // so everything starts with 1
        // i.e. the first element in order is 1,
        // meaning the second element gets mapped to the first element in new list
        // i.e. motif{<0, 0>, <1, 5>,...}  --> newmotif{<0, 5>, ...}
        for(int i = 0; i < 120; i++){
            if(motif.get(order[i]) != null){
                newMotif.put(i, motif.get(order[i]));
            }else{
                newMotif.put(i, 0);
            }
        }

        for(int i = 0; i < 120; i++){
            motif.put(i, newMotif.get(i));
        }
        motif.remove(120);
    }

    public static void changeOrderDouble2(HashMap<Integer, Double> motif){
        HashMap<Integer, Double> newMotif = new HashMap<Integer, Double>();

        // note the first element in motif is not used in the new motif list,
        // so everything starts with 1
        // i.e. the first element in order is 1,
        // meaning the second element gets mapped to the first element in new list
        // i.e. motif{<0, 0>, <1, 5>,...}  --> newmotif{<0, 5>, ...}
        for(int i = 0; i < 120; i++){
            if(motif.get(order[i]) != null){
                newMotif.put(i, motif.get(order[i]));
            }else{
                newMotif.put(i, 0.0);
            }
        }

        for(int i = 0; i < 120; i++){
            motif.put(i, newMotif.get(i));
        }
        motif.remove(120);
    }

}
