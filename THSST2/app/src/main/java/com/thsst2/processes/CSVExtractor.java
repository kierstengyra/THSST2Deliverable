package com.thsst2.processes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVExtractor {
	
	public void readCSV(String filename) {
		BufferedReader br = null;
		String line = "";
		String splitter = ",";
		
		try {
			br = new BufferedReader(new FileReader(filename));
			while((line = br.readLine()) != null) {
				String[] info = line.split(splitter);
				
				Field field = new Field(Double.parseDouble(info[1]), Double.parseDouble(info[2]), Double.parseDouble(info[3]),
						Double.parseDouble(info[4]), Double.parseDouble(info[5]), Double.parseDouble(info[6]));
				
				FieldManager.getInstance().addField(field);
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			if(br != null) {
				try {
					br.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
