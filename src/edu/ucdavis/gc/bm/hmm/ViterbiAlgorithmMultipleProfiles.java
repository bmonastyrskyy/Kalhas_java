package edu.ucdavis.gc.bm.hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucdavis.gc.bm.exeptions.ViterbiExceptionShortTargetSequence;
import edu.ucdavis.gc.bm.group.ListAA;
import edu.ucdavis.gc.bm.properties.Props;

/**
 * the Viterbi algorithm was implemented from the sketch described at
 * http://en.wikipedia.org/wiki/Viterbi_algorithm
 * 
 * @author bohdan
 * 
 */
public class ViterbiAlgorithmMultipleProfiles implements ViterbiAlgorithmMultipleInterface {
	/**
	 * probabilities to observe amino acid s at position i NOTE! Pure
	 * probability
	 */
	private final Double[][] targetProfile;

	private final Double[][] ssTargetProfile;

	private final StringBuilder targetStr;

	private final StringBuilder ssTargetStr;
	/**
	 * emission matrix of amino acids symbols : log(p i,s) - log of probability
	 * to observe s at i-th position
	 */
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

	private final Double[] initPr;

	private double w_aa = 1.0; // weight of the aa terms in the score function

	private double w_ss = 1.0; // weight of the ss terms in the score function

	private final String stateStr;
	/**
	 * Auxiliary matrix to keep l max scores [m][n][l] m - dimension of states
	 * space, i.e. number of all possible states n - length of sequence of
	 * observed symbols, i.e. length of target sequence l - number of best
	 * scores to keep.
	 */
	private Double[][][] T1;
	/**
	 * Auxiliary matrix to keep indexes of previous states which correspond to l
	 * max scores [m][n][l] m - dimension of states space, i.e. number of all
	 * possible states n - length of sequence of observed symbols, i.e. length
	 * of target sequence l - number of best scores to keep.
	 */
	private Integer[][][] T2;

	private Double[] scores;
	/**
	 * List of Lists of scores:<br>
	 * order scores which correspond to a certain assignment:
	 * total_score, score_segment_1, score_segment_2, ... 
	 * 
	 */
	private List<ArrayList<Double>> scoresPerSegment;

	private String[] paths;
	/**
	 * length of sequence of observed symbols; length of target
	 */
	private final Integer n;
	/**
	 * dimension of the states space; i.e. number of possible states
	 */
	private final Integer m;
	/**
	 * number of assignments per group
	 */
	private final Integer l;

	private final Double artificialLastStateTransitionScore;

	private final String compProfileMethod;

	public ViterbiAlgorithmMultipleProfiles(String targetStr,
			String ssTargetStr, double[][] targetProfile,
			double[][] ssTargetProfile, Kalhas_model k_model, int l)
			throws ViterbiExceptionShortTargetSequence {
		this.targetProfile = this.addArtificialLastProfileRow(targetProfile);
		this.ssTargetProfile = this
				.addArtificialLastProfileRow(ssTargetProfile);

		this.targetStr = new StringBuilder(targetStr).append("F"); // append
																	// artificial
																	// symbol
																	// "F" - end
																	// of
																	// sequence
		this.ssTargetStr = new StringBuilder(ssTargetStr).append("E"); // append
																		// artificial
																		// symbol
																		// "E" -
																		// end
																		// of
																		// sequence

		AAEmisMatrix = k_model.getAAEmisMatrix();
		SSEmisMatrix = k_model.getSSEmisMatrix();
		transMatrix = k_model.getTransMatrix();
		initPr = k_model.getInitPr();
		stateStr = k_model.getStateStr();
		// n = this.targetStr.length();
		n = this.targetProfile.length;
		compProfileMethod = Props.get("compProfilesMethod");
		assert (n > 0) : "n = " + n;
		m = transMatrix.length;
		artificialLastStateTransitionScore = k_model
				.getArtificialLastStateTransitionScore();
		this.l = l;
		this.process();
		this.calcPaths();
		if (true) {// transMatrix.length <= targetStr.length() + l + 126 ) {
		} else {
			ViterbiExceptionShortTargetSequence e = new ViterbiExceptionShortTargetSequence();
			throw e;
		}
	}

	public void process() {

		T1 = new Double[m][n][]; // Auxiliary array to keep the maximal scores
									// up to the
		T2 = new Integer[m][n][]; // Auxiliary array to keep the argmax

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				calculateMatrixes(j, i);
			}
		}

	}

	public Double[][][] getT1() {
		return T1;
	}

	public Integer[][][] getT2() {
		return T2;
	}

	/**
	 * calculates matrixes T1 and T2 at position i of the target's sequence
	 */
	private void calculateMatrixes(int j, int i) {

		char aa = targetStr.charAt(i);
		char ss = ssTargetStr.charAt(i);
		Double[] t1;
		Integer[] t2;
		if (i == 0) {
			t1 = new Double[l];
			t2 = new Integer[l];
			
			// t1[0] = initPr[j] + w_aa *
			// AAEmisMatrix[j][ListAA.indexAA.get(aa)]
			// + w_ss * SSEmisMatrix[j][ListAA.indexSS.get(ss)];
			
			t1[0] = initPr[j] + w_aa * getProfileScore(j, i, compProfileMethod)
					+ w_ss * getSsProfileScore(j, i, compProfileMethod);

			t2[0] = 0;
			for (int r = 1; r < l; r++) {
				t1[r] = Double.NEGATIVE_INFINITY;
				t2[r] = 0;
			}

		} else {
			t1 = new Double[m * l];
			t2 = new Integer[m * l];
			for (int k = 0; k < m; k++) {
				for (int r = 0; r < min(l, T1[k][i - 1].length); r++) {

					t1[k * l + r] = T1[k][i - 1][r] + transMatrix[k][j] + w_aa
							* getProfileScore(j, i, compProfileMethod) + w_ss
							* getSsProfileScore(j, i, compProfileMethod);
					t2[k * l + r] = k;

				}
			}
		}

		sort2ArraysBy1(t1, t2, l);

		T1[j][i] = t1;
		T2[j][i] = t2;
	}

	/**
	 * The method calculates the score of comparison of two profile vectors:
	 * target profile vector at i-th position and HMM profile vector at j-th
	 * position The formula of calculation is based on the scalar product.
	 * 
	 * @return
	 */
	private Double getProfileScore(final int j, final int i, String... method) {
		double result = 0.0;
		if (this.stateStr.charAt(j) == 'I') { // for insertion state zero
			return result;
		}

		String flag;
		if (method == null) {
			flag = "dotProd";
		} else if (method.length == 0) {
			flag = "dotProd";
		} else if (!method[0].equalsIgnoreCase("baker")
				&& !method[0].equalsIgnoreCase("dotProd")) {
			flag = "dotProd";
		} else {
			flag = method[0];
		}
		// convert from log(probability) -> probability
		for (int r = 0; r < ListAA.indexAA.size(); r++) {
			if (flag.equalsIgnoreCase("dotProd")) {
				result += Math.exp(AAEmisMatrix[j][r])
						* Math.exp(targetProfile[i][r]);
			} else if (flag.equalsIgnoreCase("baker")) {
				result += -Math.abs(Math.exp(AAEmisMatrix[j][r])
						- Math.exp(targetProfile[i][r]));
			}
		}
		return result;
	}

	/**
	 * The method calculates the score of comparison of two profile vectors:
	 * target ss profile vector at i-th position and HMM profile vector at j-th
	 * position The formula of calculation is based on the scalar product.
	 * 
	 * @return
	 */
	private Double getSsProfileScore(final int j, final int i, String... method) {
		double result = 0.0;
		if (this.stateStr.charAt(j) == 'I') { // for insertion state zero
			return result;
		}
		String flag;
		if (method == null) {
			flag = "dotProd";
		} else if (method.length == 0) {
			flag = "dotProd";
		} else if (!method[0].equalsIgnoreCase("baker")
				&& !method[0].equalsIgnoreCase("dotProd")) {
			flag = "dotProd";
		} else {
			flag = method[0];
		}
		for (int r = 0; r < ListAA.indexSS.size(); r++) {

			// convert from log(probability) -> probability
			if (flag.equalsIgnoreCase("dotProd")) {
				result += Math.exp(SSEmisMatrix[j][r])
						* Math.exp(ssTargetProfile[i][r]);
			} else if (flag.equalsIgnoreCase("baker")) {
				result += -Math.abs(Math.exp(SSEmisMatrix[j][r])
						- Math.exp(ssTargetProfile[i][r]));
			}

		}

		return result;
	}

	/**
	 * generate of profiles of log(probabilities)
	 * 
	 * @param profile
	 *            - pure probabilities
	 * @return
	 */
	private Double[][] addArtificialLastProfileRow(double[][] profile) {
		assert (profile.length > 0) : "profile.length = " + profile.length;
		Double[][] result = new Double[profile.length + 1][profile[0].length];
		for (int i = 0; i < profile.length + 1; i++) {
			for (int j = 0; j < profile[0].length; j++) {
				if (i != profile.length) {
					result[i][j] = Math.log(profile[i][j]);
				} else {
					result[i][j] = Math.log(1.0);
				}
			}
		}
		return result;
	}

	public String[] getPaths() {
		return this.paths;
	}

	private void calcPaths() {
		List<String> tmp = new ArrayList<String>();
		Double[] t1 = new Double[m * l];
		Integer[] t2 = new Integer[m * l];
		for (int k = 0; k < m; k++) {
			for (int r = 0; r < l; r++) {
				t1[k * l + r] = T1[k][n - 1][r];
				t2[k * l + r] = k; // T2[k][n - 1][r];
			}
		}
		sort2ArraysBy1(t1, t2, l);

		for (int r = 0; r < l; r++) {
			int reduce_index = noElementsInArrayEqualToNumber(t2, t2[r], r + 1) - 1;
			// noUniqueElementsInArray(t2, r + 1) - 1;
			// System.out.println(reduce_index);
			int last_k = t2[r];
			// the path has to ends at "F" state
			// if it's not the case discard it
			// and all others since the t1 and t2 matrixes are sorted in order
			//
			if (this.stateStr.charAt(last_k) != 'F') {
				break;
			}
			StringBuilder sb = new StringBuilder("");
			sb.insert(0, this.stateStr.charAt(last_k));
			// recover the path from T2[][]
			for (int p = n - 1; p > 0; p--) {
				// int r_index = max(0, reduce_index);
				// System.err.println(r_index);
				int previous_k = last_k;
				last_k = T2[last_k][p][max(0, reduce_index)];
				sb.insert(0, this.stateStr.charAt(last_k));
				reduce_index = noElementsInArrayEqualToNumber(
						T2[previous_k][p], T2[previous_k][p][reduce_index],
						reduce_index + 1) - 1;
				// noUniqueElementsInArray(T2[last_k][p], r + 1) - 1;
			}
			// return sb.toString();
			tmp.add(sb.toString());
		}

		paths = new String[tmp.size()];
		scores = new Double[tmp.size()];
		scoresPerSegment =  new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < tmp.size(); i++) {
			paths[i] = tmp.get(i);
			scores[i] = t1[i] - this.artificialLastStateTransitionScore;
			scoresPerSegment.add(calcScoresPerSegment(paths[i], scores[i]));
		}

	}
	/**
	 * calculates the scores per segment
	 * @param path
	 * @param totalScore
	 * @return
	 */
	private ArrayList<Double> calcScoresPerSegment(String path, double totalScore){
		ArrayList<Double> result = new ArrayList<Double>();
		result.add(totalScore);
		double scorePerSegm = 0.0;
		boolean shouldAdd = false;
		int j = 1;
		for (int i = 0; i < path.length(); i++){
			if ((path.charAt(i) == 'I' || path.charAt(i) == 'F') && shouldAdd == true){
					result.add(scorePerSegm);
					shouldAdd = false;
					scorePerSegm = 0.0;
					j++;
			} else if(path.charAt(i) == 'M'){
				shouldAdd = true;
				scorePerSegm += w_aa
						* getProfileScore(j, i, compProfileMethod) + w_ss
						* getSsProfileScore(j, i, compProfileMethod);
				j++;
			}
		}
		return result;
	}

	@Override
	public List<ArrayList<Double>> getScoresPerSegment(){
		return this.scoresPerSegment;
	}
	
	public Double[] getScores() {
		return this.scores;
	}

	
	/**
	 * sort array and return array of indexes which correspond to sorted input
	 * array in descending order
	 * 
	 * @param arr
	 *            - array of numbers
	 * @return array of indexes
	 */
	private static Integer[] argSortArray(Double[] arr, int l) {
		Integer[] res_index = new Integer[arr.length];
		// initialization of res array
		for (int i = 0; i < res_index.length; i++) {
			res_index[i] = i;
		}
		// insertion sort
		for (int i = 1; i < arr.length; i++) {
			for (int j = 1; j <= i; j++) {
				for (int k = j; k > 0; k--) {

					if (arr[k] > arr[k - 1]) {// swap
						Double tmp = arr[k];
						arr[k] = arr[k - 1];
						arr[k - 1] = tmp;
						int tmp_index = res_index[k];
						res_index[k] = res_index[k - 1];
						res_index[k - 1] = tmp_index;
					} else {
						break;
					}
				}
			}
		}

		return res_index;
	}

	/**
	 * sort array1 (first argument) in descending order and simultaneously
	 * modify array2 (second argument) in such a way that the mapping of indexes
	 * are kept and trunk every array to l (third argument) elements
	 * 
	 * @param arrDouble
	 *            - array of scores arrInt - array of indexes
	 */
	private static void sort2ArraysBy1(Double[] arrDouble, Integer[] arrInt,
			int l) {
		if (arrDouble.length != arrInt.length) {
			System.err.println("The arrays arenot of the same size");
			return;
		}
		for (int i = 1; i < arrDouble.length; i++) {
			for (int j = 1; j <= i; j++) {
				for (int k = j; k > 0; k--) {
					try {
						if (arrDouble[k] > arrDouble[k - 1]) {// swap
							Double tmp = arrDouble[k];
							arrDouble[k] = arrDouble[k - 1];
							arrDouble[k - 1] = tmp;
							int tmp_index = arrInt[k];
							arrInt[k] = arrInt[k - 1];
							arrInt[k - 1] = tmp_index;
						} else {
							break;
						}
					} catch (NullPointerException e) {
						System.err.println("k " + k);
						throw e;
					}
				}
			}
		}

		if (arrDouble.length > l) {
			for (int i = l; i < arrDouble.length; i++) {
				arrDouble[i] = null;
				arrInt[i] = null;
			}
		}

	}

	private static int min(int m1, int m2) {
		if (m1 < m2) {
			return m1;
		} else {
			return m2;
		}
	}

	private static int max(int m1, int m2) {
		if (m1 > m2) {
			return m1;
		} else {
			return m2;
		}
	}

	/**
	 * The method calculates number of unique elements among l (second argument)
	 * first elements of an array (first argument)
	 * 
	 * @param arr
	 * @param l
	 * @return
	 */
	private static int noUniqueElementsInArray(Integer[] arr, int l) {
		Set<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < min(arr.length, l); i++) {
			set.add(arr[i]);
		}
		return set.size();
	}

	/**
	 * The method calculates number of elements in array (first argument) equal
	 * to a given number (second argument) among l (third argument) first
	 * elements
	 * 
	 * @param arr
	 * @param number
	 * @return number of elements equal to a given number
	 */
	private static int noElementsInArrayEqualToNumber(Integer[] arr,
			Integer number, int l) {
		int result = 0;
		for (int i = 0; i < min(arr.length, l); i++) {
			if (arr[i] == number) {
				result++;
			}
		}
		return result;
	}

	public static void main(String[] args) {
		Double[][][] arr = new Double[10][2][];
		Integer[][][] iarr = new Integer[10][2][];
		Double[] tmpArr = new Double[5];
		Integer[] tmpIArr = new Integer[5];
		tmpArr[0] = 1.0;
		tmpIArr[0] = 0;

		tmpArr[1] = 22.0;
		tmpIArr[1] = 1;
		tmpArr[2] = Double.NEGATIVE_INFINITY;
		tmpIArr[2] = 2;
		tmpArr[3] = Double.NEGATIVE_INFINITY;
		tmpIArr[3] = 3;
		tmpArr[4] = 16.0;
		tmpIArr[4] = 4;

		arr[0][0] = tmpArr;
		iarr[0][0] = tmpIArr;
		/*
		 * System.out.println(arr[0][0][0]); arr[0][0][1] = 22.0; iarr[0][0][1]
		 * = 1; System.out.println(arr[0][0][1]); arr[0][0][2] = 4.0;
		 * iarr[0][0][2] = 2; System.out.println(arr[0][0][2]); arr[0][0][3] =
		 * 16.0; iarr[0][0][3] = 3; System.out.println(arr[0][0][3]);
		 * arr[0][0][4] = 16.0; iarr[0][0][4] = 4;
		 * System.out.println(arr[0][0][4]);
		 */

		sort2ArraysBy1(tmpArr, tmpIArr, 5);
		System.out.println(iarr[0][0].length);
		for (int i = 0; i < iarr[0][0].length; i++) {
			System.out.println(tmpArr[i] + " : " + tmpIArr[i]);

		}
		System.out.println(noUniqueElementsInArray(tmpIArr, 5));
		System.out.println(noElementsInArrayEqualToNumber(tmpIArr, tmpIArr[4],
				7));

	}

}
