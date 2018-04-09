package com.thsst2.processes;

/**
 * Created by gyra on 04/06/2018.
 */
public class Question {

    private double number;
    private double score;
    private double average;
    private int checked;

    public Question(double number) {
        this.setNumber(number);
        this.setChecked(0);
    }

    public double getNumber() {
        return number;
    }
    public void setNumber(double number) {
        this.number = number;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public double getAverage() {
        return average;
    }
    public void setAverage(double average) {
        this.average = average;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    public void addChecked() {
        this.checked += 1;
    }

}
