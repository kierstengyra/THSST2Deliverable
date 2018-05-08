package com.thsst2.processes;

import java.util.ArrayList;

public class FieldManager {

	private ArrayList<Field> fieldList;
	private int page;
	private int partialScore;
	private ArrayList<String> partialAnswers;
	private double avgPixels;

	public FieldManager() {
		this.fieldList = new ArrayList<Field>();
	}

	public void setAvgPixelsPerQuestion() {
		int qIndex = 0;

		for(int i = 0; i < this.fieldList.size(); i++) {
			int question = this.fieldList.get(i).getQuestion();
			int nonzero = this.fieldList.get(i).getNonzero_pixels();
			FormManager.getInstance().getQuestion(question-1).addPixel(nonzero);

			if((i+1)%3 == 0) {
				FormManager.getInstance().getQuestion(qIndex).setAvgPixels();
				qIndex++;
			}
		}
	}

	public void selectPossibleAnswers() {
		this.setAvgPixelsPerQuestion();

		for(int i = 0; i < this.fieldList.size(); i++) {
			int question = this.fieldList.get(i).getQuestion();
			double avg = FormManager.getInstance().getQuestion(question-1).getAvgPixels();

			if(this.fieldList.get(i).getNonzero_pixels() > avg) {
				FormManager.getInstance().getQuestion(question-1).addAnswerCnt();
				this.fieldList.get(i).setSelected(true);
			}
		}
	}

	public ArrayList<Field> getFieldList() {
		return fieldList;
	}

	public Field getField(int index) {
		return this.fieldList.get(index);
	}

	public void addField(Field field) {
		this.fieldList.add(field);
	}

	public void setFieldList(ArrayList<Field> fieldList) {
		this.fieldList = fieldList;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPartialScore() {
		return partialScore;
	}

	public void setPartialScore(int partialScore) {
		this.partialScore = partialScore;
	}

	public ArrayList<String> getPartialAnswers() {
		return partialAnswers;
	}

	public void setPartialAnswers(ArrayList<String> partialAnswers) {
		this.partialAnswers = partialAnswers;
	}

	public double getAvgPixels() {
		return avgPixels;
	}

	public void setAvgPixels(double avgPixels) {
		this.avgPixels = avgPixels;
	}
}
