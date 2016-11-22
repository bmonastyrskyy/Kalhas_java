package edu.ucdavis.gc.bm.hmm;

import java.util.ArrayList;
import java.util.List;

public interface ViterbiAlgorithmMultipleInterface {
	/**
	 * The method returns an array of paths.</br>
	 * Every path looks like IIIIMMMMMMMIIIMMMMMMIII, </br>
	 * the letter at i-th position correspond to either insertion (I) or match (M) state of the i-th residue</br> 
	 */
	public String[] getPaths();
	/**
	 * 
	 * The method return array of scores which correspond to the assignments.</br>
	 * The order of the scores correspond to the order of paths returned by getPath() method. 
	 */
	public Double[] getScores();
	/**
	 * The method returns the list of lists of scores</b>
	 * Every list of scores correspond to one assignment.</b>
	 * The order of scores:</b>
	 * total score, score segment1, score segment2, ...
	 */
	public List<ArrayList<Double>> getScoresPerSegment();
}
