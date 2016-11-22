package edu.ucdavis.gc.bm.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ListAA {
	/**
	 * hash contains one letter code of aminoacid(key) and counting index (value) in alphabetical order
	 * A - 0; C - 1; D - 2; ... Y - 19. 
	 */
	public static final HashMap<Character,Integer> indexAA = new HashMap<Character,Integer>();
	/**
	 * hash contains one letter code of aminoacid(key) and background probability computed on ASTRAL 40 percent
	 * calculated by myself
	 */
	public static final HashMap<Character,Double> backGround_ASTRAL = new HashMap<Character,Double>();
	
	/**
	 * hash contains one letter code of aminoacid(key) and background probability used by default by blast;
	 * extracted from source code at http://www.ncbi.nlm.nih.gov/IEB/ToolBox/C_DOC/lxr/source/tools/blastkar.c;
	 * probably get from the paper 
	 * A. B. Robinson, L. R. Robinson Distribution of glutamine and asparagine residues and their near neighbors in peptides and proteins 
	 * Proc. Nati. Acad. Sci. USA, Biochemistry, Vol. 88, pp. 8880-8884, October 1991
	 */
	public static final HashMap<Character,Double>  backGround_Robinson = new HashMap<Character,Double>();
	/**
	 * hash contains one letter code of aminoacid(key) and background probability used by blast; 
	 * extracted from source code at http://www.ncbi.nlm.nih.gov/IEB/ToolBox/C_DOC/lxr/source/tools/blastkar.c
	 */
	public static final HashMap<Character,Double> backGround_Altschul  = new HashMap<Character,Double>();
	/**
	 * hash contains one letter code of aminoacid(key) and background probability by blast; 
	 * extracted from source code at http://www.ncbi.nlm.nih.gov/IEB/ToolBox/C_DOC/lxr/source/tools/blastkar.c 
	 */
	public static final HashMap<Character,Double> backGround_Dayhoff  = new HashMap<Character,Double>();

	/**
	 * ArrayList contains 20 amino acids' one letter codes in alphabetical order 
	 */
	public static final List<Character> AA = new ArrayList<Character>(); 
	/**
	 * Arraylist contains 3 ss symbols - c, E, H
	 */
	public static final List<Character> SS = new ArrayList<Character>();
	/**
	 * hash contains key : ss character ; value : index
	 */
	public static final HashMap<Character,Integer> indexSS = new HashMap<Character,Integer>();
	
	public ListAA(){
		this.setIndexAA();
		this.setBackGround();
		this.setAA();
		this.setSS();
		this.setIndexSS();
	}
	
	private void setIndexAA(){
		indexAA.put('A', 0);
		indexAA.put('C', 1);
		indexAA.put('D', 2);
		indexAA.put('E', 3);
		indexAA.put('F', 4);
		
		indexAA.put('G', 5);
		indexAA.put('H', 6);
		indexAA.put('I', 7);
		indexAA.put('K', 8);
		indexAA.put('L', 9);

		indexAA.put('M', 10);
		indexAA.put('N', 11);
		indexAA.put('P', 12);
		indexAA.put('Q', 13);
		indexAA.put('R', 14);

		indexAA.put('S', 15);
		indexAA.put('T', 16);
		indexAA.put('V', 17);
		indexAA.put('W', 18);
		indexAA.put('Y', 19);
	}
	
	private void setBackGround(){
		//astral background probabilities
		backGround_ASTRAL.put('A', 0.080612274016797740);
		backGround_ASTRAL.put('C', 0.013888642111310507);
		backGround_ASTRAL.put('D', 0.05804908594482341);
		backGround_ASTRAL.put('E', 0.06980449293130304);
		backGround_ASTRAL.put('F', 0.04023156531107705);
		
		backGround_ASTRAL.put('G', 0.07279328348605725);
		backGround_ASTRAL.put('H', 0.023155992683897307);
		backGround_ASTRAL.put('I', 0.05732113695724861);
		backGround_ASTRAL.put('K', 0.05928853754940711);
		backGround_ASTRAL.put('L', 0.09334061282320909);

		backGround_ASTRAL.put('M', 0.02186862136045697);
		backGround_ASTRAL.put('N', 0.041820453966648956);
		backGround_ASTRAL.put('P', 0.04599754586428597);
		backGround_ASTRAL.put('Q', 0.037143166351957954);
		backGround_ASTRAL.put('R', 0.05221795611092177);

		backGround_ASTRAL.put('S', 0.05966328051934207);
		backGround_ASTRAL.put('T', 0.05370400581928451);
		backGround_ASTRAL.put('V', 0.07101056225982932);
		backGround_ASTRAL.put('W', 0.01365012036464214);
		backGround_ASTRAL.put('Y', 0.03443866356749923);
		
		// Robinson background probabilities
		backGround_Robinson.put('A', 0.07805);
		backGround_Robinson.put('C', 0.01925);
		backGround_Robinson.put('D', 0.05364);
		backGround_Robinson.put('E', 0.06295);
		backGround_Robinson.put('F', 0.03856);
		
		backGround_Robinson.put('G', 0.07377);
		backGround_Robinson.put('H', 0.02199);
		backGround_Robinson.put('I', 0.05142);
		backGround_Robinson.put('K', 0.05744);
		backGround_Robinson.put('L', 0.09019);
		
		backGround_Robinson.put('M', 0.02243);
		backGround_Robinson.put('N', 0.04487);
		backGround_Robinson.put('P', 0.05203);
		backGround_Robinson.put('Q', 0.04264);
		backGround_Robinson.put('R', 0.05129);
		
		backGround_Robinson.put('S', 0.0712);
		backGround_Robinson.put('T', 0.05841);
		backGround_Robinson.put('V', 0.06441);
		backGround_Robinson.put('W', 0.0133);
		backGround_Robinson.put('Y', 0.03216);
		
		// Altschul background probabilities
		backGround_Altschul.put('A', 0.081);
		backGround_Altschul.put('C', 0.015);
		backGround_Altschul.put('D', 0.054);
		backGround_Altschul.put('E', 0.061);
		backGround_Altschul.put('F', 0.04);
		
		backGround_Altschul.put('G', 0.068);
		backGround_Altschul.put('H', 0.022);
		backGround_Altschul.put('I', 0.057);
		backGround_Altschul.put('K', 0.056);
		backGround_Altschul.put('L', 0.093);
		
		backGround_Altschul.put('M', 0.025);
		backGround_Altschul.put('N', 0.045);
		backGround_Altschul.put('P', 0.049);
		backGround_Altschul.put('Q', 0.039);
		backGround_Altschul.put('R', 0.057);
		
		backGround_Altschul.put('S', 0.068);
		backGround_Altschul.put('T', 0.058);
		backGround_Altschul.put('V', 0.067);
		backGround_Altschul.put('W', 0.013);
		backGround_Altschul.put('Y', 0.032);

		// Dayhoff background probabilities
		backGround_Dayhoff.put('A', 0.08713);
		backGround_Dayhoff.put('C', 0.03347);
		backGround_Dayhoff.put('D', 0.04687);
		backGround_Dayhoff.put('E', 0.04953);
		backGround_Dayhoff.put('F', 0.03977);
		
		backGround_Dayhoff.put('G', 0.08861);
		backGround_Dayhoff.put('H', 0.03362);
		backGround_Dayhoff.put('I', 0.03689);
		backGround_Dayhoff.put('K', 0.08048);
		backGround_Dayhoff.put('L', 0.08536);
		
		backGround_Dayhoff.put('M', 0.01475);
		backGround_Dayhoff.put('N', 0.04043);
		backGround_Dayhoff.put('P', 0.05068);
		backGround_Dayhoff.put('Q', 0.03826);
		backGround_Dayhoff.put('R', 0.0409);
		
		backGround_Dayhoff.put('S', 0.06958);
		backGround_Dayhoff.put('T', 0.05854);
		backGround_Dayhoff.put('V', 0.06472);
		backGround_Dayhoff.put('W', 0.01049);
		backGround_Dayhoff.put('Y', 0.02992);

	}
	
	private void setAA(){
		AA.add('A');
		AA.add('C');
		AA.add('D');
		AA.add('E');
		AA.add('F');
		
		AA.add('G');
		AA.add('H');
		AA.add('I');
		AA.add('K');
		AA.add('L');

		AA.add('M');
		AA.add('N');
		AA.add('P');
		AA.add('Q');
		AA.add('R');

		AA.add('S');
		AA.add('T');
		AA.add('V');
		AA.add('W');
		AA.add('Y');
	}
	
	private void setSS(){
		SS.add('C');
		SS.add('E');
		SS.add('H');
	}
	
	private void setIndexSS(){
		indexSS.put('C', 0);
		indexSS.put('E', 1);
		indexSS.put('H', 2);
	}
}
