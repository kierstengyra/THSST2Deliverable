package com.thsst2.processes;

import java.util.ArrayList;

public class FieldManager {
	
	private static FieldManager instance = null;
	private ArrayList<Field> fieldList;
	
	protected FieldManager() {
		this.fieldList = new ArrayList<Field>();
	}
	
	public static FieldManager getInstance() {
		if(instance == null)
			instance = new FieldManager();
		
		return instance;
	}

	public ArrayList<Field> getFieldList() {
		return fieldList;
	}

	public void setFieldList(ArrayList<Field> fieldList) {
		this.fieldList = fieldList;
	}
	
	public void addField(Field field) {
		this.fieldList.add(field);
	}
	
	public Field getField(int index) {
		return this.fieldList.get(index);
	}
	
	public void printFieldList() {
		for(int i = 0; i < this.fieldList.size(); i++)
			System.out.println(this.fieldList.get(i).getQuestion()+"\t"+this.fieldList.get(i).getScore());
	}

}
