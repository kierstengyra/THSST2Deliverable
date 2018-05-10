package com.thsst2.processes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Type: Process
 * Question is the class type for all pre-defined questions.
 */
public class Question {

    private int number;
	private ArrayList<Integer> pixelList;
	private double avgPixels;
	private int answerCnt;
	private String question;

	public Question(int number, String question) {
		this.pixelList = new ArrayList<Integer>();
		this.setNumber(number);
		this.setQuestion(question);
		this.setAnswerCnt(0);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public double getAvgPixels() {
		return avgPixels;
	}

	public void setAvgPixels() {
		int sum = 0;

		for(int i = 0; i < this.pixelList.size(); i++)
			sum += this.pixelList.get(i);

		this.avgPixels = sum/this.pixelList.size();
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getAnswerCnt() {
		return answerCnt;
	}

	public void addAnswerCnt() {
		this.answerCnt++;
	}

	public void setAnswerCnt(int answerCnt) {
		this.answerCnt = answerCnt;
	}

	public ArrayList<Integer> getPixelList() {
		return this.pixelList;
	}

	public int getPixel(int index) {
		return this.pixelList.get(index);
	}

	public void setPixelList(ArrayList<Integer> pixelList) {
		this.pixelList = pixelList;
	}

	public void addPixel(int pixel) {
		this.pixelList.add(pixel);
	}

}
