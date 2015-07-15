package util;

import java.util.ArrayList;

public class VectorUtil {
    // fill in the array len times the new element
//	public static void fillElement(ArrayList<Integer> array, int len, int element){
//		if(len == 0 | array.size() == len) return;
//		for(int i = 0; i < len; i++){
//			array.add(element);
//		}
//	}
    // extend the array by repeating the last element until length len
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

    // add a new element to array which is inc + last element value in array
    public static void incNewBy(ArrayList<Integer> array, int inc) {
        if (array.size() == 0) {
            array.add(inc);
        } else {
            int temp = array.get(array.size() - 1);
            array.add(temp + inc);
        }
    }

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
}
