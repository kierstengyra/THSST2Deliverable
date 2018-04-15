package com.thsst2.processes;

import android.util.Log;

import java.util.ArrayList;

public class FieldManager {

	private static FieldManager instance = null;
	private ArrayList<Field> fieldList;
	private ArrayList<Question> questionList;
	private int final_score;

	protected FieldManager() {
		this.fieldList = new ArrayList<Field>();
		this.questionList = new ArrayList<Question>();
		this.setScore(0);
	}

	public static FieldManager getInstance() {
		if(instance == null)
			instance = new FieldManager();

		return instance;
	}

	public double computeScore() {
		this.validateCheckMarks();

		double tempScore = 0;
		for(int i = 0; i < this.questionList.size(); i++)
			tempScore += this.questionList.get(i).getScore();

		Log.e("FieldManager", "Temp Score: "+tempScore);
		return tempScore;
	}

	public void validateCheckMarks() {
		this.computeAvgs();
		for(int i = 0; i < this.fieldList.size(); i++) {
			double question = this.fieldList.get(i).getQuestion().getNumber();
			double avg = this.questionList.get((int)question-1).getAverage();

			Log.e("FieldManager", "Field "+i);
			if(this.fieldList.get(i).getNonzeroPixels() > avg) {

				double score = 0;
				this.questionList.get((int)question-1).addChecked();

				if(this.questionList.get((int)question-1).getChecked() > 1) {
					score = 0;
				}
				else {
					score = this.fieldList.get(i).getScore();
				}

				this.questionList.get((int)question-1).setScore(score);
				Log.e("FieldManager", "["+question+"] Score: " + this.questionList.get((int) question - 1).getScore());
			}
		}
	}

	public void computeAvgs() {
		int sum = 0;
		double avg = 0;
		int question = 0;

		for(int i = 0; i < this.fieldList.size(); i++) {
			sum += this.fieldList.get(i).getNonzeroPixels();

			if((i+1) % 3 == 0) {
				avg = sum/3;
				this.questionList.get(question).setAverage(avg);
				Log.e("FieldManager", "Question Avgs: "+this.questionList.get(question).getAverage());
				sum = 0;
				question++;
			}
		}
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

	public int getScore() {
		return final_score;
	}

	public void setScore(int score) {
		this.final_score = score;
	}

	public ArrayList<Question> getQuestions() {
		return questionList;
	}

	public void addQuestion(Question q) {
		this.questionList.add(q);
	}

	public void setQuestions(ArrayList<Question> questions) {
		this.questionList = questions;
	}

}
