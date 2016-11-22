package edu.ucdavis.gc.bm.group;

import java.util.List;

public class GroupProfile_1 extends GroupProfile{
	
	private List<double[][]> pssm;
	
	private String name;
	
	@Override
	public List<double[][]> getPSSMs() {
		// TODO Auto-generated method stub
		return pssm;
	}

	public void setPSSM(List<double[][]> pssm){
		this.pssm = pssm;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public void setName(String name){
		this.name = name; 
	}
	
	@Override
	public int getNumberSegments() {
		// TODO Auto-generated method stub
		return pssm.size();
	}
	
}
