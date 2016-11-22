package edu.ucdavis.gc.bm.hmm;

import java.util.HashMap;
import java.util.List;

import edu.ucdavis.gc.bm.group.GroupProfile;
import edu.ucdavis.gc.bm.group.GroupSSProfile;
import edu.ucdavis.gc.bm.group.ListAA;

public class Kalhas_model {
	/**
	 * AAProfile - matrix of lx20, where l=length('IMMMMMMIMMMMMMIMMMMMMIF')
	 * I - insertion states
	 * M - matched states
	 * F - final state - imagine state that points at end of sequence(it was introduced to 
	 * to eliminate cases when just part of segment was assigned, see transition matrix)
	 */
	private List<HashMap<Character, Double>> AAProfile;
	/**
	 * SSProfile - matrix of lx3
	 */
	private List<HashMap<Character, Double>> SSProfile;
	/**
	 * emission matrix of amino-acids symbols: log(p i,a) - log of probability
	 * to observe a at i-th position
	 */
	private Double[][] AAEmisMatrix;
	/**
	 * emission matrix of secondary structure symbols : log(p i,s) - log of
	 * probability to observe s at i-th position
	 */
	private Double[][] SSEmisMatrix;
	/**
	 * transition matrix
	 */
	private Double[][] transMatrix;
	/**
	 * stateStr - string of states symbols "IMMMMMIMMMMMMIMMMMMMMIF"; 'I' stands
	 * for insertion state (at the beginning, at the end and between the
	 * segments)
	 * F -final states, transition to "F" states is possible from last "I" or from last "M"
	 */
	private final StringBuilder stateStr;

	private Double[] initPr; // vector of initial probabilities of being in
								// state i
								// in our case the first aa can be in state
								// either 'I' or in first 'M'
	/**
	 * bigNumber is used in last column of transition Matrix (to artificial state "F" 
	 * which indicates the end of sequence). It is used to provide the paths which ends by "F" 
	 * would get higher score;
	 * in order to get "pure" score it's necessary to subtract this number
	 */
	private final Double artificialLastStateTransitionScore = 100000.0; 

	public Kalhas_model(GroupProfile PrGroup, GroupSSProfile SSPrGroup) {
		stateStr = setStateStr(PrGroup);
		this.setTransMatrix(PrGroup);
		this.setAAEmisMatrix(PrGroup);
		this.setSSEmisMatrix(SSPrGroup);
		this.setInitPr();
	}

	private StringBuilder setStateStr(GroupProfile PrGroup) {
		StringBuilder sb = new StringBuilder("I");
		for (int i = 0; i < PrGroup.getPSSMs().size(); i++) { // loop over
			// segments
			for (int j = 0; j < PrGroup.getPSSMs().get(i).length; j++) {
				sb.append("M");
			}
			sb.append("I");
		}
		sb.append("F");
		return sb;
	}

	private void setInitPr() {
		int n = stateStr.length();
		initPr = new Double[n];
		initPr[0] = 0.0;
		initPr[1] = 0.0;
		for (int i = 2; i < n; i++) {
			initPr[i] = Double.NEGATIVE_INFINITY;
		}

	}

	private void setTransMatrix(GroupProfile PrGroup) {
		int n = stateStr.length();
		transMatrix = new Double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				transMatrix[i][j] = Double.NEGATIVE_INFINITY;
				if (j == i + 1 ) {
					transMatrix[i][j] = Math.log(1.0);
				}
				if (stateStr.charAt(j) == 'I') {
					if (j == i) {
						transMatrix[j][j] = Math.log(1.0);
					}
				}
				// transition to state "F" is possible just from last "I" or last "M"
				// 
				if (stateStr.charAt(j) == 'F') {
					if ((j == i + 2 )||(j == i + 1)) {
						transMatrix[i][j] = this.artificialLastStateTransitionScore ; //Math.log(1.0);
					}
				}				
			}
		}
	}

	private void setAAEmisMatrix(GroupProfile PrGroup) {
		int n = stateStr.length();
		int k = 0; // k-th segment
		AAEmisMatrix = new Double[n][20];
		for (int i = 0; i < n;) {
			if (stateStr.charAt(i) == 'I') {
				for (int j = 0; j < 20; j++) {
					AAEmisMatrix[i][j] = Math.log(1.0);//Math.log(0.0001*ListAA.backGround_Robinson
							//.get(ListAA.AA.get(j)));
					}
				i++;continue;
			} 
			if (stateStr.charAt(i) == 'M') {
				for (int l = 0; l < PrGroup.getPSSMs().get(k).length; l++) {
					for (int j = 0; j < 20; j++) {
						AAEmisMatrix[i + l][j] = Math.log(PrGroup.getPSSMs()
								.get(k)[l][j]);
					}
				}
				i += PrGroup.getPSSMs().get(k).length;
				k++;continue;
			}
			if (stateStr.charAt(i) == 'F') {
				for (int j = 0; j < 20; j++) {
					AAEmisMatrix[i][j] = Math.log(1.0);
					}
				i++;continue;
			}
		}
	}

	private void setSSEmisMatrix(GroupSSProfile PrGroup) {
		int n = stateStr.length();
		SSEmisMatrix = new Double[n][3]; 
		int k = 0; // k-th segment
		for (int i = 0; i < n;) {
			if (stateStr.charAt(i) == 'I') {
				for (int j = 0; j < 3; j++) {
					SSEmisMatrix[i][j] = Math.log(1.0); //0.0001*Math.log(0.333);
				}
				i++;continue;
			} 
			if (stateStr.charAt(i) == 'M') {
				for (int l = 0; l < PrGroup.getPSSMs().get(k).length; l++) {
					for (int j = 0; j < 3; j++) {
						SSEmisMatrix[i + l][j] = Math.log(PrGroup.getPSSMs()
								.get(k)[l][j]);
					}
				}
				i += PrGroup.getPSSMs().get(k).length;
				k++;
			}
			if (stateStr.charAt(i) == 'F') {
				for (int j = 0; j < 3; j++) {
					SSEmisMatrix[i][j] = Math.log(1.0);
				}
				i++;continue;
			} 
		}
	}

	public Double[] getInitPr() {
		return this.initPr;
	}

	public Double[][] getTransMatrix() {
		return this.transMatrix;
	}

	public Double[][] getAAEmisMatrix() {
		return this.AAEmisMatrix;
	}

	public Double[][] getSSEmisMatrix() {
		return this.SSEmisMatrix;
	}

	public List<HashMap<Character, Double>> getAAProfile() {
		return AAProfile;
	}

	public List<HashMap<Character, Double>> getSSProfile() {
		return SSProfile;
	}
	
	public String getStateStr(){
		return this.stateStr.toString();
	}

	public Double getArtificialLastStateTransitionScore(){
		return this.artificialLastStateTransitionScore;
	}
}
