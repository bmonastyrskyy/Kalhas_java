package edu.ucdavis.gc.bm.hmm;

import edu.ucdavis.gc.bm.group.DescriptorGroupData;

public class DescriptorIIAssignments {
	private ViterbiAlgorithmMultiple vam;

	private DescriptorGroupData dgd;


	public DescriptorIIAssignments(ViterbiAlgorithmMultiple vam, DescriptorGroupData dgd) {
		this.vam = vam;
		this.dgd = dgd;	
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
		int no = paths.length;
		String[] result = new String[no];

		for (int i = 0; i < no; i++) {
			StringBuilder sb = new StringBuilder("");
			sb.append(dgd.getRootDesc() + "  " + dgd.getRepDesc());
			sb.append("  " + recoverBondsByStateString(paths[i]) + "/"
					+ dgd.getBonds());
			sb.append("  " + scores[i]);
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
		int no = paths.length;
		String[] result = new String[no];

		for (int i = 0; i < no; i++) {
			if (scores[i] >= hmmScore_cutOff) {
				StringBuilder sb = new StringBuilder("");
				sb.append(dgd.getRootDesc() + "  " + dgd.getRepDesc());
				sb.append("  " + recoverBondsByStateString(paths[i]) + "/"
						+ dgd.getBonds());
				sb.append("  " + scores[i] + "\n");
				result[i] = sb.toString();
			} else {
				result[i] = "";
			}
		}
		return result;
	}

}
