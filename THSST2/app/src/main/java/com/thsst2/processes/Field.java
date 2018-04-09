package com.thsst2.processes;

public class Field {

	private double x;
	private double y;

	private double width;
	private double height;

	private Question question;
	private int score;

	private int nonzero_pixels;

	public Field(double x, double y, double width, double height, int question, int score) {
		this.setX(x);
		this.setY(y);

		this.setWidth(width);
		this.setHeight(height);

		this.setQuestion(question);
		this.setScore(score);

		this.setNonzeroPixels(0);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(double number) {
		this.question = FieldManager.getInstance().getQuestions().get((int)number-1);;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getNonzeroPixels() {
		return nonzero_pixels;
	}

	public void setNonzeroPixels(int nonzero_pixels) {
		this.nonzero_pixels = nonzero_pixels;
	}

}
