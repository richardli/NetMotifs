package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;


public class WriteArray {
  
	public void write(double[] one, String path, boolean append) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(path, append));
		bw.write(Arrays.toString(one).replace("[", "").replace("]", ""));
		bw.write("\n");
		bw.close();
  }
  
  public void write(double[][] two, String path, boolean append) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(path, append));
		for(int i = 0; i < two.length; i++){
			bw.write(Arrays.toString(two[i]).replace("[", "").replace("]", ""));			
			bw.write("\n");			
		}
		bw.close();
  }
}
