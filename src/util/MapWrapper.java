package util;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *   A wrapper class for HashMap<Integer, Integer>
 *   @author zehang li
 *   Last update: 07-27-15
 */
public class MapWrapper {
	// the HashMap
    public HashMap<Integer, Integer> x;

	// initialization
	public MapWrapper(){
		this.x = new HashMap<Integer, Integer>();
	}

    // put method
	public void put(int a, int b){
		this.x.put(a, b);
	}

    // get method
	public int get(int a){
		return(this.x.get(a));
	}

    // sum within range (inclusive)
    public int getsum(int a, int b){
        int sum = 0;
        for(int i = a; i <= b; i++){
            if(this.x.containsKey(i)) sum += this.x.get(i);
        }
        return(sum);
    }

    // return a vector within range (inclusive)
    public int[] getVector(int a, int b){
        int[] sum = new int[b - a + 1];
        for(int i = a; i <= b; i++){
            if(this.x.containsKey(i)) sum[i - a] += this.x.get(i);
        }
        return(sum);
    }

    public boolean containsKey(int a){
        return(this.x.containsKey(a));
    }

    /**
     * Add one to value for all elements specified by key
     * @param a list of keys
     */
	public void addAll(ArrayList<Integer> a){
		for(int element : a){
			if(this.x.get(element) != null){
			   this.x.put(element,  this.x.get(element) + 1);
			}else{
				this.x.put(element,  1);
			}
		}
	}
	
}
