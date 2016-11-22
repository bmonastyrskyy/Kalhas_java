package edu.ucdavis.gc.bm.group;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DescriptorGroupData {
	private String rootDesc; 
	private String repDesc;
	private String bonds;
	private String file;
	
	public String getRootDesc(){
		return this.rootDesc;
	}
	
	public String getRepDesc(){
		return this.repDesc;
	}
	
	public String getBonds(){
		return this.bonds;
	}
	
	public DescriptorGroupData(String file){
		this.file = file;
		try {
			new Parser().parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class Parser{
		
		private void parse () throws IOException{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			boolean flag = false;
			while (( line = br.readLine()) != null){
				if(line.matches("^GROUP.*")){
					flag = true;
					continue;
				}
				if(flag){
					String [] tokens = line.split("\\s+");
					rootDesc = tokens[0];
					repDesc = tokens[1];
					bonds = tokens[2];
					break;
				}
			}
			br.close();
		}
	}
	
}
