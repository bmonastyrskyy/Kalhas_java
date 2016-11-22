package edu.ucdavis.gc.bm.group;

import java.util.List;

public class GroupSSProfile_1 extends GroupSSProfile {

	private List<double[][]> pssm;
	
	private String name;
 
	public List<double[][]> getPSSMs() {
		// TODO Auto-generated method stub
		return pssm;
	}

	public void setPSSM(List<double[][]> pssm){
		this.pssm = pssm;
	}
	
 	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public void setName(String name){
		this.name = name; 
	}

	public int getNumberSegments() {
		// TODO Auto-generated method stub
		return pssm.size();
	}
	

}
