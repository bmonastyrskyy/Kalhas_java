package edu.ucdavis.gc.bm.crossvalidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;

public abstract class Training {
	
	private List<DescriptorAssignment> assigns;
	
	/**
	 * hash: value - hmmscore, value - "true positives"
	 * hmmscore - cutOff which separates positives and negative cases   
	 */
	private HashMap<Double,Integer> TP = new HashMap<Double,Integer>();
	/**
	 * hash: value - hmmscore, value - "false positives"
	 * hmmscore - cutOff which separates positives and negative cases   
	 */	
	private HashMap<Double,Integer> FP = new HashMap<Double,Integer>();
	/**
	 * hash: value - hmmscore, value - "true negatives"
	 * hmmscore - cutOff which separates positives and negative cases   
	 */	
	private HashMap<Double,Integer> TN = new HashMap<Double,Integer>();
	/**
	 * hash: value - hmmscore, value - "false negatives"
	 * hmmscore - cutOff which separates positives and negative cases   
	 */	
	private HashMap<Double,Integer>FN = new HashMap<Double,Integer>();
	/**
	 * set of hmmScores, which correspond to assignments of QualityStatus = 1
	 */
	private TreeSet<Double> cutOffs ;
	
	private List<Double> positives;
	/**
	 * all real positives
	 */
	int P;
	/**
	 * all real negatives
	 */
	private List<Double> negatives;
	
	int N;
	
	public Training(List<DescriptorAssignment> assigns){
		this.assigns = assigns; 
		//Collections.sort(this.assigns, descComparatorByQualityStatus);
		separatePositivesNegatives();
		Collections.sort(this.assigns, descComparatorByHMMScore);
		process();
	}
	
	private void process(){
		int tp = 0;
		int fp = 0;
		int tn = N;
		int fn = P;
		for(DescriptorAssignment descAss : assigns){

			if (descAss.getQualityStatus().equals(1)) {
				tp++;
				fn = P - tp;
				TP.put(descAss.getHMMScores().get(0), tp);
				TN.put(descAss.getHMMScores().get(0), tn);
				FP.put(descAss.getHMMScores().get(0), fp);
				FN.put(descAss.getHMMScores().get(0), fn);
			}else{
				fp++;
				tn = N - fp;
			}
		}

	}
	 
	private void separatePositivesNegatives(){
		//cutOffs = new TreeSet<Double>();
		//positives = new ArrayList<Double>();
		//negatives = new ArrayList<Double>();
		for(DescriptorAssignment descAss : assigns){
			if(descAss.getQualityStatus().equals(1)){
				//cutOffs.add(descAss.getHMMScores().get(0));
				//positives.add(descAss.getHMMScores().get(0));
				P++;
			}else{
				//negatives.add(descAss.getHMMScores().get(0));
				N++;
			}
		}
	}
	
	protected HashMap<Double,Integer> getTP(){
		return this.TP;
	}
	
	protected HashMap<Double,Integer> getTN(){
		return this.TN;
	}
	protected HashMap<Double,Integer> getFP(){
		return this.FP;
	}
	
	protected HashMap<Double,Integer> getFN(){
		return this.FN;
	}
	

	private static Comparator<DescriptorAssignment> descComparatorByQualityStatus = new Comparator<DescriptorAssignment>(){
		public int compare(DescriptorAssignment desc1, DescriptorAssignment desc2) {
			 
		      Integer qualityStatus1 = desc1.getQualityStatus();
		      Integer qualityStatus2 = desc2.getQualityStatus();
	 
		      //ascending order
		      return qualityStatus1.compareTo(qualityStatus2);

		    }
	};
	
	private static Comparator<DescriptorAssignment> descComparatorByHMMScore = new Comparator<DescriptorAssignment>(){
		public int compare(DescriptorAssignment desc1, DescriptorAssignment desc2) {
			 
		      Double hmmScore1 = desc1.getHMMScores().get(0);
		      Double hmmScore2 = desc2.getHMMScores().get(0);
	 
		      //descending order: higher score are first
		      return -hmmScore1.compareTo(hmmScore2);

		    }
	};

	
}
