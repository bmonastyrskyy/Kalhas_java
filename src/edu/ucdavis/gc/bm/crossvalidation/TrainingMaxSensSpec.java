package edu.ucdavis.gc.bm.crossvalidation;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;

public class TrainingMaxSensSpec extends Training {
	
    private StringBuilder sb = new StringBuilder(""); 
	
	public TrainingMaxSensSpec(List<DescriptorAssignment> assigns){
		super(assigns);
		this.process();
	}
	
	private void process(){
		HashMap<Double, Integer> TP = super.getTP();
		HashMap<Double, Integer> FP = super.getFP();
		HashMap<Double, Integer> TN = super.getTN();
		HashMap<Double, Integer> FN = super.getFN();
		TreeSet<Double> cutOffs = new TreeSet<Double>();
		cutOffs.addAll(TP.keySet());
		sb.append("cutOff\tTP\tFP\tTN\tFN\tSens\tSpec\n");
		for (Double cutOff : cutOffs){
			sb.append(cutOff + "\t");
			sb.append(TP.get(cutOff) + "\t");
			sb.append(FP.get(cutOff) + "\t");
			sb.append(TN.get(cutOff) + "\t");
			sb.append(FN.get(cutOff) + "\t");
			sb.append((double)(100.0*TP.get(cutOff))/(TP.get(cutOff)+FN.get(cutOff)) + "\t");
			sb.append((double)(100.0*TN.get(cutOff))/(TN.get(cutOff)+FP.get(cutOff))+"\n");
		}
	}
	
	public String toString(){
		return this.sb.toString();
	}
}
