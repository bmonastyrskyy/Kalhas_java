package edu.ucdavis.gc.bm.group;

import java.util.List;

public abstract class GroupProfile {
	
	abstract public List<double[][]> getPSSMs();
	
	abstract public String getName();

//	abstract public int getNumberMembers();

	abstract public int getNumberSegments();

//	abstract public Descriptor getRootDescriptor();
	
}
