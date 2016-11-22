package edu.ucdavis.gc.bm.crossvalidation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;

/**
 * The class handles with training of Kalhas based on maximal  PPV - positive predict value
 * @author bohdan
 *
 */
public class TrainingMaxPPV {
	
	private StringBuilder output = new StringBuilder(""); 
	
	private List<DescriptorAssignment> assigns;
	
	private Double hmmCutOff;
	
	public TrainingMaxPPV(List<DescriptorAssignment> assigns){
		this.assigns = assigns;
		this.process();
	}
	
	public Double getHmmCutOff(){
		return this.hmmCutOff;
	}
	
	public String  toString(){
		return this.output.toString();
	}
	
	private void process(){
		// sort with respect to HMM score in ascending order
		Double maxPPV = -10000.0; // max PPV
		Double hmmCutOff = -10000.0; // hmmCutOff which corresponds to max PPV
		// sort assignments by quality status
		Collections.sort(assigns, descComparatorByQualityStatus);
		TreeSet<Double> cutOffs = this.getHMMScoresForGoodAssignments();
		//sort assignments by hmmScore
		Collections.sort(assigns,descComparatorByHMMScore);
		//for (Double cutOff : cutOffs.descendingSet()){ // pick up cutOffs in descending order
		for (Double cutOff : cutOffs){
			Double curPPV = this.getPPVforHMMScore(cutOff);
			output.append(cutOff + "\t" + curPPV + "\n");
			if (curPPV > maxPPV) {
				maxPPV = curPPV;
				hmmCutOff = cutOff;
			}
		}
		this.hmmCutOff = hmmCutOff;
		output.append("cutOff hmmScore : " + hmmCutOff + "\n");
	}
	
	private TreeSet<Double> getHMMScoresForGoodAssignments(){
		TreeSet<Double> result = new TreeSet<Double>();
		for(DescriptorAssignment descAss : assigns){
			if(!descAss.getQualityStatus().equals(1)){
				break;
			}
			if(descAss.getQualityStatus().equals(1)){
				result.add(descAss.getHMMScores().get(0));
			}
		}
		return result;
	}
	
	private Double getPPVforHMMScore(Double cutOff){
		int tp = 0;
		int fp = 0;
		for(DescriptorAssignment descAss : assigns){
			if(descAss.getHMMScores().get(0)<cutOff){
				break;
			}
			if(descAss.getQualityStatus().equals(1)){
				tp++;
			}else{
				fp++;
			}
		}
		return (double) 100.0*tp/(tp+fp);
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
	 
		      //descending  order - higher scores are first
		      return -hmmScore1.compareTo(hmmScore2);

		    }
	};
}
