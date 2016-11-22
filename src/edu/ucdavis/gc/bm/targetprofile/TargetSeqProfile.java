package edu.ucdavis.gc.bm.targetprofile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import edu.ucdavis.gc.bm.group.ListAA;
import edu.ucdavis.gc.bm.properties.Props;

public class TargetSeqProfile {
	private double[][] PSSM;

	private final String targetName;

	private final String seq;

	public TargetSeqProfile(String targetName, String seq) {
		this.targetName = targetName;
		this.seq = seq;
		calculateListPSSMs();
	}

	public double [][] getPSSM(){
		return PSSM;
	}
	
	/**
	 * calculate list of PSSM for all segments
	 */
	protected void calculateListPSSMs() {
		// get file .mtx with matrics of blast scores - profile
		int[][] scoresMTX = this.parseMTX(this.runBlast());

		PSSM = new double[seq.length()][20];
		for (int i = 0; i < seq.length(); i++) {
			PSSM[i] = calculateVectorPSSM(scoresMTX[i]);
		}

	}

	/**
	 * 
	 * @param scoreVector
	 *            - vector of dimension 20 with blast scores for a certain
	 *            residue position
	 * @return vector of dimension 20 with probabilities recovered from blast
	 *         scores
	 */
	private double[] calculateVectorPSSM(int[] scoreVector) {
		double[] result = new double[20];
		if (20 != scoreVector.length) {
			return null;
		}
		/*
		 * scale factor used by makemat with args -S param; by default 100
		 */
		double param = 100.0;
		double sum = 0;
		for (int i = 0; i < 20; i++) {
			double backProb = ListAA.backGround_Robinson.get(ListAA.AA.get(i));
			result[i] = backProb * Math.exp((double) scoreVector[i] / param);
			sum += result[i];
		}
		// normalize
		if (1 != sum) {
			for (int i = 0; i < 20; i++) {
				result[i] = result[i] / sum;
			}
		}

		return result;
	}

	/**
	 * @param fileMTX
	 * @return matrix[numberResidues][20] of MTX scores
	 */
	private int[][] parseMTX(File fileMTX) {
		int[][] result = new int[seq.length()][20];
		String line;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileMTX));
			int position = 0;
			while (null != (line = br.readLine())) {
				String[] tokens = line.split("\\s+");
				if (28 != tokens.length) {
					continue;
				}
				try{
				result[position] = this.parseLineMTX(line);
				} catch (ArrayIndexOutOfBoundsException e){
					System.err.println(fileMTX.getName());
					throw e;
				}
				position++;
			}
			br.close();
		} catch (IOException e) {

		}
		return result;
	}

	/**
	 * 
	 * @return vector of length 20 with MTX scores for 20 amino acids ordered
	 *         alphabetically (A,C,D,...,Y)
	 */
	private int[] parseLineMTX(String line) {
		int[] result = new int[20];
		String[] tokens = line.split("\\s+");
		if (28 != tokens.length) {
			throw new NullPointerException();
		}
		int indexAA = 0;
		for (int i = 0; i < 23; i++) { // loop over amino acids
			if (0 != i && 2 != i && 21 != i) {
				result[indexAA] = Integer.parseInt(tokens[i]);
				indexAA++;
			}
		}
		return result;
	}

	private File runBlast() {

		String mtxDir = Props.get("mtxDir") + System.getProperty("file.separator");
		File filemtx = new File(mtxDir, targetName + ".mtx");
		//File filepssm = new File(tmp_dir, targetName + ".pssm");
		int attempt = 0;
		//while ((!filemtx.exists() || !filepssm.exists())) {
		while (!filemtx.exists()){
			RunBlast runBlast = new RunBlast();
			System.out.println(targetName + "\tattempt :" + (++attempt));
			if (attempt > 11)
				break;
		}
		return filemtx;
	}

	private class RunBlast {

		private String DB = Props.get("db"); 
		private String mtxDir = Props.get("mtxDir") + System.getProperty("file.separator");
		private String blastBinDir = Props.get("blastBinDir") + System.getProperty("file.separator");

		private String parameters = " ";

		RunBlast() {
			try {
				String fastaSeq = this.generateSeqFile();
				String chk = mtxDir
						+ System.getProperty("file.separator") + targetName
						+ ".chk";

				parameters += (" -d " + DB + " -i " + fastaSeq + " -C " + chk
						+ " -j 3 -h 0.001 ");

				String command = blastBinDir + "blastpgp" + parameters;
				Thread.currentThread().sleep(500);
				Process p = Runtime.getRuntime().exec(command);

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

				// run makemat
				this.generateMTX(fastaSeq, chk);

			} catch (Exception err) {
				err.getStackTrace();
			}
		}

		private String generateSeqFile() throws IOException {
			String dir_name = mtxDir;
			File dir = new File(dir_name);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String seqFileName = dir_name
					+ System.getProperty("file.separator") + targetName
					+ ".seq";
			File seqFile = new File(seqFileName);
			PrintWriter pw = new PrintWriter(new FileWriter(seqFile));

			pw.println(">" + targetName);
			pw.println(seq);
			pw.close();
			return seqFileName;
		}

		/**
		 * run makemat, which creates mtx file
		 * 
		 * @throws IOException
		 * @throws InterruptedException
		 */
		private void generateMTX(String seqFileName, String pssmFileName)
				throws IOException, InterruptedException {
			// =========================================
			// create file sn
			// =========================================
			String dir_name = mtxDir;
			File dir = new File(dir_name);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String snFileName = dir_name + System.getProperty("file.separator")
					+ targetName + ".sn";
			File snFile = new File(snFileName);
			PrintWriter pw = new PrintWriter(new FileWriter(snFile));
			int lastIndex = seqFileName.lastIndexOf(System
					.getProperty("file.separator"));
			pw.println(seqFileName.substring(lastIndex + 1));
			pw.close();

			// ========================================
			// create file pn
			// ========================================
			String pnFileName = dir_name + System.getProperty("file.separator")
					+ targetName + ".pn";
			File pnFile = new File(pnFileName);
			pw = new PrintWriter(new FileWriter(pnFile));
			lastIndex = pssmFileName.lastIndexOf(System
					.getProperty("file.separator"));
			pw.println(pssmFileName.substring(lastIndex + 1));
			pw.close();

			// ========================================
			// run makemat
			// ========================================
			String command = blastBinDir + "makemat -P " + mtxDir + targetName;
			Process p = Runtime.getRuntime().exec(command);

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
			// wait for the end of the process
			p.waitFor();
		}

		private void clean(){
			File file2del = new File(this.mtxDir + targetName + ".chk");
			if (file2del.exists() && file2del.delete()){
			
			}
			file2del = new File(this.mtxDir + targetName + ".sn");
			if (file2del.exists() && file2del.delete()){
				
			}
			file2del = new File(this.mtxDir + targetName + ".sp");
			if (file2del.exists() && file2del.delete()){
				
			}
			file2del = new File(this.mtxDir + targetName + ".seq");
			if (file2del.exists() && file2del.delete()){
				
			}
		}
		File getMTXFile() {
			return new File(this.mtxDir + targetName + ".mtx");
		}
	}
}
