package edu.ucdavis.gc.bm.group;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserGroupSSProfile {

	private File file;

	public ParserGroupSSProfile(String filename) {
		File file = new File(filename);
		this.file = file;
	}

	public ParserGroupSSProfile(File file) {
		this.file = file;
	}

	public GroupSSProfile parse() throws IOException {
		GroupSSProfile_1 grPr = new GroupSSProfile_1();
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
		double result[][] = new double[segmLines.size()][3];
		int i = 0;
		for (String line : segmLines) {
			String[] tokens = line.trim().split("\\s+");
			for (int j = 0; j < 3; j++) {
				if (j == 0){
					result[i][j] = Double.parseDouble(tokens[1]);					
				}
				if (j == 1){
					result[i][j] = Double.parseDouble(tokens[3]);
				}
				if (j == 2){
					result[i][j] = Double.parseDouble(tokens[2]);
				}				
			}
			i++;
		}
		return result;
	}
}
