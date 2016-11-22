package edu.ucdavis.gc.bm.targetprofile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import edu.ucdavis.gc.bm.properties.Props;

public class TargetSSProfile {

	private double[][] PSSM;

	private final String targetName;

	private final String seq;

	private HashMap<Integer, Double> prob_C = new HashMap<Integer, Double>();

	private HashMap<Integer, Double> prob_H = new HashMap<Integer, Double>();

	private HashMap<Integer, Double> prob_E = new HashMap<Integer, Double>();


	public TargetSSProfile(String targetName, String seq) {
		this.targetName = targetName;
		this.seq = seq;
		PsiPred psipred = new PsiPred();
		
		this.setPSSM();
	}

	private void setPSSM() {

		PSSM = new double[seq.length()][3];

		for (int index = 0; index < seq.length(); index++) {

				PSSM[index][0] = prob_C.get(index );
				PSSM[index][1] = prob_E.get(index );
				PSSM[index][2] = prob_H.get(index );
		}
	}

	
	public double[][] getPSSM(){
		return PSSM;
	}
	
	private class PsiPred {

		PsiPred() {
			try {
				File file = runPsiPred();
				parse(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private File runPsiPred() {
			String psipredResDir = Props.get("psipredResDir");
			File dir = new File(psipredResDir);
			if (!dir.exists()){
				dir.mkdirs();
			}
			String resultFile = psipredResDir + System.getProperty("file.separator") + targetName + ".ss2";
			File file = new File(resultFile);
			if (file.exists()) {
				return file;
			}
			
			String psipredBinDir = Props.get("psipredBinDir");
			String mtxDir = Props.get("mtxDir");
			
			String program = psipredBinDir + System.getProperty("file.separator") + "psipred ";
			String mtxFile = mtxDir + System.getProperty("file.separator") + targetName + ".mtx";
			String parameters = mtxFile
					+ " " + psipredBinDir + System.getProperty("file.separator") + "../data/weights.dat"
					+ " " + psipredBinDir + System.getProperty("file.separator") + "../data/weights.dat2"
					+ " " + psipredBinDir + System.getProperty("file.separator") + "../data/weights.dat3"
					+ " " + psipredBinDir + System.getProperty("file.separator") + "../data/weights.dat4";
					//+ " > ./tmp_psipredDB/" + domain + ".ss";
			String command = program + parameters;

			Process p;
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(new File(psipredResDir + System.getProperty("file.separator") + targetName + ".ss")));
				p = Runtime.getRuntime().exec(command);
				
				// this block free stack
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				BufferedReader bre = new BufferedReader(new InputStreamReader(
						p.getErrorStream()));
				String line_br;
				String line_bre;
				boolean oef_br = false;
				boolean oef_bre = false;
				while (oef_br == false ||  oef_bre == false) {
					if (oef_br == false) {
						line_br = br.readLine();
						if (line_br == null) {
							oef_br = true;
						}else{
							pw.println(line_br);
						}
					}
					if (oef_bre == false) {
						line_bre = bre.readLine();
						if (line_bre == null) {
							oef_bre = true;
						}
					}
				}

				pw.close();
				br.close();
				bre.close();
				// end of block to free stack

				p.waitFor();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// run psipass2
			program = psipredBinDir + System.getProperty("file.separator") +"psipass2 ";
			parameters = " " + psipredBinDir + System.getProperty("file.separator") + "../data/weights_p2.dat 1 1.0 1.0 " +
					psipredResDir + System.getProperty("file.separator") + targetName +".ss2" + "  " +
					psipredResDir + System.getProperty("file.separator") + targetName +".ss";
			command = program + parameters;
			try {
				p = Runtime.getRuntime().exec(command);
				// this block free stack
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				BufferedReader bre = new BufferedReader(new InputStreamReader(
						p.getErrorStream()));
				String line;
				boolean oef_br = false;
				boolean oef_bre = false;
				while (oef_br == false || oef_bre == false) {
					if (oef_br == false) {
						line = br.readLine();
						if (line == null) {
							oef_br = true;
						}
					}
					if (oef_bre == false) {
						line = bre.readLine();
						if (line == null) {
							oef_bre = true;
						}
					}
				}
				br.close();
				bre.close();
				// end of block to free stack

				p.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//end psipass2 
			
			
			file = new File(resultFile);
			if (file.exists()) {
				return file;
			} else {
				return null;
			}

		}

		private void parse(File file) throws IOException {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			try {
				while (null != (line = reader.readLine())) {
					if (line.trim().equals("") || line.startsWith("#")) {
						continue;
					}
					if (line.trim().matches("^\\d.*")) { // starts with digit
						parseLine(line);
					}
				}
			} finally {
				reader.close();
			}
		}

		private void parseLine(String line) {
			String[] tokens = line.trim().split("\\s+");
			
			int resItnNo = Integer.parseInt(tokens[0].trim()) - 1; // enumeration
																	// should
																	// start
																	// from 0
			double raw_C = Double.parseDouble(tokens[3]);
			double raw_H = Double.parseDouble(tokens[4]);
			double raw_E = Double.parseDouble(tokens[5]);
			prob_C.put(resItnNo, raw_C / (raw_C + raw_H + raw_E));
			prob_H.put(resItnNo, raw_H / (raw_C + raw_H + raw_E));
			prob_E.put(resItnNo, raw_E / (raw_C + raw_H + raw_E));
		}
	}
}
