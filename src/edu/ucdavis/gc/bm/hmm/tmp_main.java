package edu.ucdavis.gc.bm.hmm;

import java.io.IOException;

import edu.ucdavis.gc.bm.exeptions.ViterbiExceptionShortTargetSequence;
import edu.ucdavis.gc.bm.group.GroupProfile;
import edu.ucdavis.gc.bm.group.GroupSSProfile;
import edu.ucdavis.gc.bm.group.ListAA;
import edu.ucdavis.gc.bm.group.ParseGroupProfile;
import edu.ucdavis.gc.bm.group.ParserGroupSSProfile;
import edu.ucdavis.gc.bm.targetprofile.TargetSSProfile;
import edu.ucdavis.gc.bm.targetprofile.TargetSeqProfile;

public class tmp_main {

	/**
	 * @param args
	 * @throws ViterbiExceptionShortTargetSequence 
	 */
	public static void main(String[] args) throws ViterbiExceptionShortTargetSequence {

		new ListAA();
		String aa = "AISLITALVRSHVDTTPDPSCLDYSHYEEQSMSEADKVQQFYQLLTSSVDVIKQFAEKIPGYFDLLPEDQELLFQSASLELFVLRLAYRARIDDTKLIFCNGTVLHRTQCLRSFGEWLNDIMEFSRSLHNLEIDISAFACLCALTLITERHGLREPKKVEQLQMKIIGSLRDHVTYNAEAQKKQHYFSRLLGKLPELRSLSVQGLQRIFYLKLEDLVPAPALIENMFVTT";//"RIIAILLDAYYNLHEEEHYYYYQTYIR";
		String ss = "CHHHHHHHHHHHHCCCCCCCCCCCCCCCCCCCCHHHHHHHHHHHHHHHHHHHHHHHCCCCCCCCCCHHHHHHHHHHHHHHHHHHHHHHHCCCCCCEEEEECCEEECHHHHHHHHHHHHHHHHHHHHHHHHCCCCHHHHHHHHHHHEECCCCCCCCHHHHHHHHHHHHHHHHHHHHHHCCCCCCCCHHHHHHHHHHHHHHHHHHHHHHHHHHHHCCCCCCCHHHHHHHCCC";
		
		TargetSeqProfile targetSeqProf  = new TargetSeqProfile("d1pdua_", aa);
		for (int j = 0; j < aa.length(); j++){
			System.out.println(targetSeqProf.getPSSM()[j][0]);
		}
		
		TargetSSProfile targetSSProfile = new TargetSSProfile("d1pdua_", aa);
		for (int i = 0; i < aa.length(); i++){
			for (int j = 0; j < 3; j++){
			   System.out.print(targetSSProfile.getPSSM()[i][j] + "\t");
			}
			System.out.println();
		}
		
		System.exit(0);
		
		// aa profile
		String filename = "1a5za2#281_1.prb";
		ParseGroupProfile parser = new ParseGroupProfile(filename);
		GroupProfile grPr = null;
		try {
			grPr = parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ss profile
		String filenamess = "1a5za2#281_1.ssp2";
		ParserGroupSSProfile parserss = new ParserGroupSSProfile(filenamess);
		GroupSSProfile grSsPr = null;
		try {
			grSsPr = parserss.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Kalhas_model k_model = new Kalhas_model(grPr, grSsPr);
		
		ViterbiAlgorithmMultiple vam = new ViterbiAlgorithmMultiple(aa, ss,
				k_model, 10);
		
		String [] pathes = vam.getPaths();
		Double [] scores = vam.getScores();
		int no = pathes.length;
		for (int i = 0; i <no; i++){
			System.out.println(pathes[i] + "\t" + scores[i]);
		}
		System.out.println(no);
		System.out.println("====");
		
		
		System.out.println(aa);
		System.out.println(aa.length());
		assert (aa.length() != 0);
		double [][] prof = genProfUnits(aa);
		assert (prof.length >  0);
		for (int r = 0; r < 20 ; r++){
			//System.out.println(prof[3][r]);
			assert (prof[3][r] >= 0) : "alksjgfsk";
		}
		//System.exit(1);
		double [][] ssProf = genSSProfUnits(ss);
		ViterbiAlgorithmMultipleProfiles vamP = new ViterbiAlgorithmMultipleProfiles(aa, ss, prof, ssProf,
				k_model, 10);
		
		String [] pathesP = vamP.getPaths();
		Double [] scoresP = vamP.getScores();
		int noP = pathesP.length;
		for (int i = 0; i < noP; i++){
			System.out.println(pathesP[i] + "\t" + scoresP[i]);
		}
		System.out.println(noP);
		System.out.println("====");
	}
	
	private static double [][] genProfUnits(final String aa){
		double [][] result = new double [aa.length()][20];
		for (int i = 0; i < aa.length(); i++){
			for (int j = 0; j < 20; j++){
				if (ListAA.indexAA.get(aa.charAt(i)) == j ){
					result [i][j] = 0.981;					
				} else {
					result [i][j] = 0.001;
				}
			}
		}
		return result;
	}
	
	private static double [][] genSSProfUnits(final String ss){
		double [][] result = new double [ss.length()][3];
		for (int i = 0; i < ss.length(); i++){
			for (int j = 0; j < 3; j++){
				if (ListAA.indexSS.get(ss.charAt(i)) == j ){
					result [i][j] = 0.98;					
				} else {
					result [i][j] = 0.01;
				}
			}
		}
		return result;
	}
}
