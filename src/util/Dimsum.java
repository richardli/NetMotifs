package util;

public class Dimsum {
	
	/**********************************************************/
	/**********************************************************/
	public double sum(double[] x){
		double sum =0.0;
		for(int i=0; i < x.length; i++) sum += x[i];
		return(sum);
	}
	
	public int sum(int[] x){
		int sum =0;
		for(int i=0; i < x.length; i++) sum += x[i];
		return(sum);
	}
	
	public int sum(int[][] x){
		int sum =0;
		for(int i=1; i < x.length; i++){
			for(int j = 0; j < x[0].length;j++) sum += x[i][j];
		}
		return(sum);
	}
	
	
	/**********************************************************/
	/**********************************************************/
	public double pick13_sum(int i, int k, double[][][] x){
		double sum = 0.0;
		for(int j = 0; j < x[0].length; j++){
			sum += x[i][j][k];
		}
		return(sum);
	}
	
	public int pick13_sum(int i, int k, int[][][] x){
		int sum = 0;
		for(int j = 0; j < x[0].length; j++){
			sum += x[i][j][k];
		}
		return(sum);
	}
	
	public int pick13_sum(int i, int k, MapWrapper[][] x){
		int sum = 0;
		for(int j = 0; j < x[0].length; j++){
			if(x[i][j].x.get(k) != null){
				sum += x[i][j].x.get(k);
			}
		}
		return(sum);
	}
	/**********************************************************/
	/**********************************************************/
	public double[] apply_sum(int ndim, double[][] x){
		double[] sum;
		if(ndim == 1){
			sum = new double[x.length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					sum[i] += x[i][j];
				}
			}
			return(sum);
		}else{
			sum = new double[x[0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					sum[j] += x[i][j];
				}
			}
			return(sum);
		}
	}
	
	public int[] apply_sum(int ndim, int[][] x){
		int[] sum;
		if(ndim == 1){
			sum = new int[x.length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					sum[i] += x[i][j];
				}
			}
			return(sum);
		}else{
			sum = new int[x[0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					sum[j] += x[i][j];
				}
			}
			return(sum);
		}
	}
	
	/**********************************************************/
	/**********************************************************/
	public double[] apply_sum(int ndim, double[][][] x){
		double[] sum;
		if(ndim == 1){
			sum = new double[x.length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[i] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}else if(ndim == 2){
			sum = new double[x[0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[j] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}else{
			sum = new double[x[0][0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[k] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}
	}
	/**********************************************************/
	/**********************************************************/
	public int[] apply_sum(int ndim, int[][][] x){
		int[] sum;
		if(ndim == 1){
			sum = new int[x.length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[i] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}else if(ndim == 2){
			sum = new int[x[0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[j] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}else{
			sum = new int[x[0][0].length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < x[0].length; j++){
					for(int k = 0; k < x[0][0].length; k++){
						sum[k] += x[i][j][k];						
					}
				}
			}
			return(sum);
		}
	}
	
}
