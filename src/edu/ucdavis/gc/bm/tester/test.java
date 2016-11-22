package edu.ucdavis.gc.bm.tester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.gc.bm.crossvalidation.TrainingMaxPPV;
import edu.ucdavis.gc.bm.crossvalidation.TrainingMaxSensSpec;
import edu.ucdavis.gc.bm.crossvalidation.TrainningWEKA;
import edu.ucdavis.gc.bm.exeptions.ViterbiExceptionShortTargetSequence;
import edu.ucdavis.gc.bm.group.DescriptorGroupData;
import edu.ucdavis.gc.bm.group.GroupProfile;
import edu.ucdavis.gc.bm.group.GroupSSProfile;
import edu.ucdavis.gc.bm.group.ListAA;
import edu.ucdavis.gc.bm.group.ParseGroupProfile;
import edu.ucdavis.gc.bm.group.ParserGroupSSProfile;
import edu.ucdavis.gc.bm.hmm.Assignments;
import edu.ucdavis.gc.bm.hmm.Kalhas_model;
import edu.ucdavis.gc.bm.hmm.ViterbiAlgorithmMultiple;
import edu.ucdavis.gc.bm.hmm.ViterbiAlgorithmMultipleInterface;
import edu.ucdavis.gc.bm.hmm.ViterbiAlgorithmMultipleProfiles;
import edu.ucdavis.gc.bm.properties.Props;
import edu.ucdavis.gc.bm.targetprofile.TargetSSProfile;
import edu.ucdavis.gc.bm.targetprofile.TargetSeqProfile;
import edu.ucdavis.gc.comparefasta.FastaComparator;
import edu.ucdavis.gc.exceptions.FileFormatException;
import edu.ucdavis.gc.hmmAssignment.CompareToTarget;
import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;
import edu.ucdavis.gc.pdb.PdbFile;
import edu.ucdavis.gc.pdb.PdbParser;
import edu.ucdavis.gc.pdb.PdbStructure;
import edu.ucdavis.gc.pdb.PdbStructureAddGaps;

public class test {

	private static String astralDir;

	private static List<String> targets;

	private static List<String> groups;

	private static String targetsListFileName = "415_Crossvalidation"; // file
																		// for
																		// crossvalidation

	private static String ssDir ;

	private static String inSSProfDir;

	private static String inAAProfDir;

	private static String outDirName;

	private static int from = 0;

	private static int to = Integer.MAX_VALUE;

	private static BufferedWriter bw;
	
	private static BufferedWriter bw_data;

	private static int noAssignPerGroup = 1;
	
	private static boolean targetProfilesFlag = false;

	private static boolean fAdditionalTrainning = false; 
	
	private static boolean lengthDependentNoAssignPerGroup = false;

	/**
	 * @param args
	 * @throws FileFormatException
	 * @throws IOException
	 * @throws ViterbiExceptionShortTargetSequence 
	 */
	public static void main(String[] args) throws IOException,
			FileFormatException, ViterbiExceptionShortTargetSequence {
		
		// read config.properties file
		
		try {
			targetsListFileName = Props.get("targetsListFileName");
			inSSProfDir = Props.get("inSSProfDir");
			inAAProfDir = Props.get("inAAProfDir");
			astralDir = Props.get("astralDir");
			ssDir = Props.get("ssDir");
			outDirName = Props.get("outDir");
			noAssignPerGroup =  Props.getInt("noAssignPerGroup", 1);
			targetProfilesFlag = Props.getBool("targetProfilesFlag", false);
			fAdditionalTrainning = Props.getBool("fAdditionalTrainning", false);
			lengthDependentNoAssignPerGroup = Props.getBool("lengthDependentNoAssignPerGroup", false);
		} catch (NullPointerException e) {

		}

		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].matches("-{1,2}from")) {
				from = Integer.valueOf(args[i + 1]);
			}
			if (args[i].matches("-{1,2}to")) {
				to = Integer.valueOf(args[i + 1]);
			}
		}


		readGroups();
		// create outdir
		File outDir = new File(outDirName);
		if (!outDir.isDirectory()) {
			if (!outDir.exists()) {
				outDir.mkdirs();
			}
		}

		new ListAA();
		// read list of targets
		getTargets();
		//TargetSeqProfile tpr = new TargetSeqProfile(targets.get(0), getSeq(targets.get(0)).get("aa"));
		
		//System.exit(0);
		
		int count = 0;
		for (String group : groups) { // loop over groups
			if (count < from) {
				count++;
				continue;
			}
			if (count > to) {
				break;
			}

			count++;

			//if (!group.equalsIgnoreCase("2nlva1#33")) continue;
			//System.out.println(group);
			// aa profile
			String filename = inAAProfDir + System.getProperty("file.separator") + group + ".prb"; 
			ParseGroupProfile parser = new ParseGroupProfile(filename);
			GroupProfile grPr = null;
			try {
				grPr = parser.parse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ss profile
			String filenamess = inSSProfDir + System.getProperty("file.separator") + group + ".ssp2";
			ParserGroupSSProfile parserss = new ParserGroupSSProfile(filenamess);
			GroupSSProfile grSsPr = null;
			try {
				grSsPr = parserss.parse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			DescriptorGroupData dgd = new DescriptorGroupData(filename);
			// System.out.println(dgd.getRootDesc() + "\t" + dgd.getRepDesc() +
			// "\t"
			// + dgd.getBonds());

			// Kalhas_model
			Kalhas_model k_model = new Kalhas_model(grPr, grSsPr);


			// open buffered writer for output
			File outFile = 	new File(outDir, group + ".crossval");
			if (outFile.exists()){
				continue;
			}
			bw = new BufferedWriter(new FileWriter(new File(outDir, group + ".crossval")));
			//bw_data = new BufferedWriter(new FileWriter(new File(outDir, group + ".assigns_data")));
			bw.write("assignment\t:target\t:RMSD  contS  Quality\n");
			//bw_data.write("target\thmmScore\tqualityStatus\n");
			List<DescriptorAssignment> allAssignments = new ArrayList<DescriptorAssignment>();
			for (String target : targets) {

				HashMap<String, String> seqs = getSeq(target);

				if (lengthDependentNoAssignPerGroup){
					noAssignPerGroup = Math.round(seqs.get("aa").length()/7) + 5;
				}
				if (noAssignPerGroup > 40){
					noAssignPerGroup = 40;
				}
				
				// target Structure
				PdbStructure targetStr = new PdbStructure();
				
				PdbFile pdbFilePath = new PdbFile(astralDir, target);
				String pdbFileName = pdbFilePath.getPdbFilePath();

				File pdbFile = new File(pdbFileName);
				if (!pdbFile.exists()) {
					targetStr = null;// return;
					throw new IOException("PDB file not found: ");
				}
				PdbParser pdbParser = new PdbParser(pdbFile);
				targetStr = pdbParser.parsePdbFile();
				if (!targetStr.getSeqLength().equals(seqs.get("aa").length())) {
					adjustNumeration(targetStr, seqs.get("aa"));
				}
				// end target Structure


				ViterbiAlgorithmMultipleInterface vam = null;
				try {
					if (targetProfilesFlag){
						TargetSeqProfile tpr = new TargetSeqProfile(target, getSeq(target).get("aa"));
						TargetSSProfile tsp = new TargetSSProfile(target,getSeq(target).get("ss"));
						vam = new ViterbiAlgorithmMultipleProfiles(seqs.get("aa"),
								seqs.get("ss"), tpr.getPSSM(), tsp.getPSSM(), k_model, noAssignPerGroup);
					} else {
						vam = new ViterbiAlgorithmMultiple(seqs.get("aa"),
							seqs.get("ss"), k_model, noAssignPerGroup);
					}
				} catch (ViterbiExceptionShortTargetSequence e) {
					System.err.println("Target " + target + " is too short (" 
							+ seqs.get("aa").length() + "residues) for group " + group +
							"(length of HMM model: " + k_model.getStateStr().length() +
							": " +
							"number of assignments: " + noAssignPerGroup + ")");
					continue;
					//throw e;
				} catch (OutOfMemoryError err){
					System.err.println("Target " + target);
					throw err; 
				}
				
				
				Assignments ass = new Assignments(vam, dgd);
				for (DescriptorAssignment descAss : ass
						.getDescriptorAssignments()) {
					descAss.setPdbDir(astralDir);
					descAss.setPdbFile();
					descAss.setFieldsStructure();
					try {
						CompareToTarget comp = new CompareToTarget(descAss,
								targetStr);
						descAss.setRMSDtoTarget(comp.getRMSD());
						descAss.setContAgrScoreToTarget(comp.getContAgrScore());
						comp = null;

						if (descAss.getRMSDtoTarget() < 0.0) { // skip
																// assignment
							continue;
						}
						// set QualityStatus
						// good assignments
						if (descAss.getRMSDtoTarget() < 3.5
								&& descAss.getRMSDtoTarget() >= 0.0
								&& descAss.getContAgrScoreToTarget() > 50) {
							descAss.setQualityStatus(1);

						} else {
							// really bad assignments
							if (descAss.getRMSDtoTarget() > 7.0
									&& descAss.getContAgrScoreToTarget() < 10) {
								descAss.setQualityStatus(3);
							} else {
								descAss.setQualityStatus(2);
							}
						}
						allAssignments.add(descAss);
						//if (descAss.getQualityStatus().equals(1)) {
							bw.write(descAss.toString() + "\t:" +target +"\t:"
									+ descAss.getRMSDtoTarget() + "\t"
									+ descAss.getContAgrScoreToTarget() + "\t"
									+ descAss.getQualityStatus() + "\n");
						//}
				//		bw_data.write(target + "\t" +
					//					descAss.getHMMScores().get(0) + "\t" +
						//		descAss.getQualityStatus() + "\n");
					} catch (NullPointerException e) {
						System.err.println(target);
						System.err.println(descAss.toString());
						throw e;
					} 				}
				targetStr = null;
				// break;
			}
			if (fAdditionalTrainning ){
				bw.write("Start training WEKA\n");
				TrainningWEKA tweka = new TrainningWEKA(allAssignments);
				bw.write(tweka.toString());
			
				bw.write("\nStart training PPV max\n");
				TrainingMaxPPV tn = new TrainingMaxPPV(allAssignments);
				bw.write(tn.toString());
			
				bw.write("\nStart training SensSpec Max\n");
				TrainingMaxSensSpec tss = new TrainingMaxSensSpec(allAssignments);
				bw.write(tss.toString());
			}
			bw.close();
			//bw_data.close();
		}// end loop over groups

	}

	private static void getTargets() throws IOException {
		targets = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(
				targetsListFileName));
		String line;
		while ((line = br.readLine()) != null) {
			targets.add(line.trim());
		}
		br.close();
	}

	public static HashMap<String, String> getSeq(String targetName)
			throws IOException {
		HashMap<String, String> result = new HashMap<String, String>();
		String fileName = ssDir + "/" + targetName + ".horiz";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		br.readLine(); // first line
		result.put("aa", br.readLine().trim());
		result.put("ss", br.readLine().trim());
		br.close();
		return result;
	}

	private static void adjustNumeration(PdbStructure targetStr,
			String fastaSequence) {
		String fastaSequencePdb = targetStr.getFastaSeq();
		FastaComparator fc = new FastaComparator(fastaSequence,
				fastaSequencePdb, "needle");
		new PdbStructureAddGaps(targetStr, fc.getMapSeq1ToSeq2(), fastaSequence);
	}

	private static void readGroups() {
		groups = new ArrayList<String>();
		File folder = new File(inAAProfDir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.endsWith(".prb");
			}
		};
		File[] listOfFiles = folder.listFiles(filter);
		for (File file : listOfFiles) {
			if (file.isFile()) {
				String name = file.getName();
				groups.add(name.substring(0, name.lastIndexOf(".prb")));
			}
		}
		Collections.sort(groups);
	}
}
