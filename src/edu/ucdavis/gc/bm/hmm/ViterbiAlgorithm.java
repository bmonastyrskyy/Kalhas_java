package edu.ucdavis.gc.bm.hmm;

import edu.ucdavis.gc.bm.group.ListAA;
/**
 * the Viterbi algorithm was implemented from the sketch described at 
 * http://en.wikipedia.org/wiki/Viterbi_algorithm
 * @author bohdan
 *
 */
public class ViterbiAlgorithm {
	
	private final StringBuilder targetStr;
	
	private final StringBuilder ssTargetStr; 
	
	private final Double[][] AAEmisMatrix;
	/**
	 * emission matrix of secondary structure symbols : log(p i,s) - log of
	 * probability to observe s at i-th position
	 */
	private final Double[][] SSEmisMatrix;
	/**
	 * transition matrix
	 */
	private final Double[][] transMatrix;
	
	private final Double [] initPr; 
	
	private double w_aa = 1.0; // weight of the aa terms in the score function
	
	private double w_ss = 1.0; // weight of the ss terms in the score function
	
	private final String stateStr;
	
	private Double[][] T1;
	
	private Integer [][] T2;
	
	private Double score;
	
	public ViterbiAlgorithm(String targetStr, String ssTargetStr, Kalhas_model k_model){
		this.targetStr = new StringBuilder(targetStr);
		this.ssTargetStr = new StringBuilder(ssTargetStr);
		AAEmisMatrix = k_model.getAAEmisMatrix();
		SSEmisMatrix = k_model.getSSEmisMatrix();
		transMatrix = k_model.getTransMatrix();
		initPr = k_model.getInitPr();
		stateStr = k_model.getStateStr();
		//this.process();
	}
	
	public String process(){
		// calculate final score by forward procedure
		// loop over symbols of target's sequence
		int n = this.targetStr.length(); // length of target's sequence
		int m = transMatrix.length; // number of all potential states  
		T1 = new Double [m][n]; // auxiliar array to keep the maximal scores up to the   
		T2= new Integer [m][n]; // auxialar array to keep the argmax  
		//initialize the T1 T2 arrays at first position
		char aa = targetStr.charAt(0);
		char ss = ssTargetStr.charAt(0);
		for (int j = 0 ; j < m; j++){
			T1[j][0] = initPr[j] + 
				w_aa * AAEmisMatrix[j][ListAA.indexAA.get(aa)] + 
				w_ss * SSEmisMatrix[j][ListAA.indexSS.get(ss)];
			T2[j][0] = 0;
		}
		for (int i = 1; i < n; i++){ // loop over the symbols of the target's sequence
			aa = targetStr.charAt(i);
			ss = ssTargetStr.charAt(i);
			for (int j = 0; j < m; j++){ // loop over the potential states 
				// get the maximal value of the score
				// and argmax 
				int k = 0;
				T1[j][i] = T1[k][i-1] + transMatrix[k][j] + 
					w_aa * AAEmisMatrix[j][ListAA.indexAA.get(aa)] + 
					w_ss * SSEmisMatrix[j][ListAA.indexSS.get(ss)];
				T2[j][i] = k;
				while(++k < m){
					if ( T1[j][i] < T1[k][i-1] + transMatrix[k][j] + 
							w_aa * AAEmisMatrix[j][ListAA.indexAA.get(aa)] + 
							w_ss * SSEmisMatrix[j][ListAA.indexSS.get(ss)]) {
						T1[j][i] = T1[k][i-1] + transMatrix[k][j] + 
						w_aa * AAEmisMatrix[j][ListAA.indexAA.get(aa)] + 
						w_ss * SSEmisMatrix[j][ListAA.indexSS.get(ss)];
						T2[j][i] = k;
					}
				}
			}
		}
		
		//the final score is the max value in last column in T1[][]
		score = T1[0][n-1];
		Integer last_k = 0;
		for (int s = 1; s < m; s++){
			if (score < T1[s][n-1]){
				score = T1[s][n-1];
				last_k = s;
			}
		}
		StringBuilder sb = new StringBuilder("");
		sb.insert(0, this.stateStr.charAt(last_k));
		// recover the path from T2[][]
		for (int p = n - 1; p > 0; p--){
			last_k = T2[last_k][p];
			sb.insert(0, this.stateStr.charAt(last_k));
		}
		return sb.toString();
	}
	
	public Double [][] getT1(){
		return T1;
	}
	
	public Integer [][] getT2(){
		return T2;
	}
	
}
