package edu.ucdavis.gc.bm.group;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseGroupProfile {

	private File file;

	public ParseGroupProfile(String filename) {
		File file = new File(filename);
		this.file = file;
	}

	public ParseGroupProfile(File file) {
		this.file = file;
	}

	public GroupProfile parse() throws IOException {
		GroupProfile_1 grPr = new GroupProfile_1();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String curLine;
		List<String> segmLines = null;
		List<double[][]> pssm = new ArrayList<double[][]>();
		while ((curLine = br.readLine()) != null) {
			// System.out.println(curLine);
			if (curLine.matches("^GROUP.*")) {
				String[] tokens = curLine.trim().split("\\s+");
				grPr.setName(tokens[1]);
				continue;
			}
			if (curLine.matches("^\\d+\\s+.*")) {
				if (segmLines == null) {
					segmLines = new ArrayList<String>();
				}
				segmLines.add(curLine);
				continue;
			}
			if (curLine.trim().isEmpty()) { // sign of the end of the segment
				pssm.add(this.parseSegmLines(segmLines));
				segmLines = null;
			}
		}
		// add last segment
		if (segmLines != null) {
			pssm.add(this.parseSegmLines(segmLines));
		}
		grPr.setPSSM(pssm);
		br.close();
		return grPr;
	}

	private double[][] parseSegmLines(List<String> segmLines) {
		double result[][] = new double[segmLines.size()][20];
		int i = 0;
		for (String line : segmLines) {
			String[] tokens = line.trim().split("\\s+");
			for (int j = 0; j < 20; j++) {
				if (j == 0) {
					result[i][j] = Double.parseDouble(tokens[j + 1]);
				} else {
					if (j == 19) {
						result[i][j] = Double.parseDouble(tokens[j + 3]);
					} else {
						result[i][j] = Double.parseDouble(tokens[j + 2]);
					}
				}
			}
			i++;
		}
		return result;
	}
}
