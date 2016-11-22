package edu.ucdavis.gc.bm.hmm;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.gc.bm.group.DescriptorGroupData;
import edu.ucdavis.gc.exceptions.FileFormatException;
import edu.ucdavis.gc.hmmAssignment.DescriptorAssignment;
import edu.ucdavis.gc.hmmAssignment.HMMAssignmentsParser;

public class Assignments {

	private ViterbiAlgorithmMultipleInterface vam;

	private DescriptorGroupData dgd;

	private List<DescriptorAssignment> descAssigns;

	public Assignments(ViterbiAlgorithmMultipleInterface vam, DescriptorGroupData dgd) {
		this.vam = vam;
		this.dgd = dgd;
		this.descAssigns = this.extractDescriptorAssignments();
	}

	private String recoverBondsByStateString(String stateString) {
		StringBuilder sb = new StringBuilder("");
		// consider chunks of two symbols
		for (int i = 0; i < stateString.length() - 1; i++) {
			String chunk;
			if (i == stateString.length() - 2) {
				chunk = stateString.substring(i);
			} else {
				chunk = stateString.substring(i, i + 2);
			}

			if (i == 0 && chunk.equals("MM")) {
				sb.append("0-");
			}
			if (chunk.equals("IM")) {
				sb.append((i + 1) + "-");
			}
			if (chunk.equals("MI") || chunk.equals("MF")) {
				sb.append(i + ";");
			}
		}
		return sb.toString();

	}
	/**
	 * the method returns an array of lines of assignments
	 * @return
	 */
	public String[] getLinesOfAssignements() {
		String[] paths = vam.getPaths();
		Double[] scores = vam.getScores();
		List<ArrayList<Double>> scoresPerSegment = vam.getScoresPerSegment();
		int no = paths.length;
		String[] result = new String[no];

		for (int i = 0; i < no; i++) {
			StringBuilder sb = new StringBuilder("");
			sb.append(dgd.getRootDesc() + "  " + dgd.getRepDesc());
			sb.append("  " + recoverBondsByStateString(paths[i]) + "/"
					+ dgd.getBonds());
			if (scoresPerSegment != null){
				for(Double score : scoresPerSegment.get(i)){
					sb.append(" " + score);
				}
			} else {
				sb.append("  " + scores[i] );
			}
			result[i] = sb.toString();
		}
		return result;
	}

	/**
	 * the method returns an array of lines of assignments with hmmScore
	 * above the cutOff
	 * 
	 * @param hmmScore_cutOff
	 * @return
	 */
	public String[] getLinesOfAssignements(Double hmmScore_cutOff) {
		String[] paths = vam.getPaths();
		Double[] scores = vam.getScores();
		List<ArrayList<Double>> scoresPerSegment = vam.getScoresPerSegment();
		int no = paths.length;
		String[] result = new String[no];

		for (int i = 0; i < no; i++) {
			if (scores[i] >= hmmScore_cutOff) {
				StringBuilder sb = new StringBuilder("");
				sb.append(dgd.getRootDesc() + "  " + dgd.getRepDesc());
				sb.append("  " + recoverBondsByStateString(paths[i]) + "/"
						+ dgd.getBonds());
				if (scoresPerSegment != null){
					for(Double score :scoresPerSegment.get(i)){
						sb.append(" " + score);
					}
					
				} else {
					sb.append("  " + scores[i]);
				}
				result[i] = sb.toString();
			} else {
				result[i] = "";
			}
		}
		return result;
	}

	private List<DescriptorAssignment> extractDescriptorAssignments() {
		List<DescriptorAssignment> result = new ArrayList<DescriptorAssignment>();
		HMMAssignmentsParser parser = new HMMAssignmentsParser();
		for (String line : this.getLinesOfAssignements()) {
			try {
				result.add(parser.parseDescriptorAssignment(line, true));
			} catch (FileFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NumberFormatException e) {
				System.err.println(line);
				continue;
				// throw e;
			}
		}
		return result;
	}

	public List<DescriptorAssignment> getDescriptorAssignments() {
		return this.descAssigns;
	}

}
