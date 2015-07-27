package util;

import java.util.ArrayList;
import java.util.HashMap;

public class MapWrapper {
	public HashMap<Integer, Integer> x;
	
	public MapWrapper(){
		this.x = new HashMap<Integer, Integer>();
	}
	public void put(int a, int b){
		this.x.put(a, b);
	}
	
	public int get(int a){
		return(this.x.get(a));
	}
	
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
