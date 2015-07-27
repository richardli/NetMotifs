package util;

public class VecOperation {
	public static double[] Add (double[] x, double[] y){
		int n = y.length;
		for(int i = 0; i < n; i++){
				x[i] += y[i];
		}
		return(x);
	}
	
	public static int[] Add (int[] x, int[] y){
		int n = y.length;
		for(int i = 0; i < n; i++){
				x[i] += y[i];
		}
		return(x);
	}
	
	public static int[][] Add (int[][] x, int[][] y){
		int n = y.length;
		int m = y[1].length;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				x[i][j] += y[i][j];
			}
		}
		return(x);
	}
	
	public static double[][] Add (double[][] x, double[][] y){
		int n = y.length;
		int m = y[1].length;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				x[i][j] += y[i][j];
			}
		}
		return(x);
	}
	
	public static double[]Multi (double[]x, double k){
		int n = x.length;
		for(int i = 0; i < n; i++){
				x[i] *= k;
		}
		return(x);
	}
	public static double[][] Multi (double[][] x, double k){
		int n = x.length;
		int m = x[1].length;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				x[i][j] *= k;
			}
		}
		return(x);
	}
	
	public static double[]Multi (int[]x, double k){
		int n = x.length;
		double[] y = new double[x.length];
		for(int i = 0; i < n; i++){
				y[i] = ((double) x[i]) * k;
		}
		return(y);
	}
	public static double[][] Multi (int[][] x, double k){
		int n = x.length;
		int m = x[1].length;
		double[][] y = new double[x.length][x[0].length];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				y[i][j] = ((double) x[i][j]) * k;
			}
		}
		return(y);
	}
}
