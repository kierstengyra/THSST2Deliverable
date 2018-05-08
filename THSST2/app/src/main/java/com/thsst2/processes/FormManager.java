package com.thsst2.processes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by gyra on 05/08/2018.
 */
public class FormManager {

    private static FormManager instance = null;
    private ArrayList<FieldManager> allPages;
    private int finalScore;
    private ArrayList<String> finalAnswers;
    private ArrayList<Question> questionList;

    protected FormManager() {
        this.allPages = new ArrayList<FieldManager>();
        this.questionList = new ArrayList<Question>();
    }

    public static FormManager getInstance() {
        if(instance == null)
            instance = new FormManager();

        return instance;
    }

    public void summarize() {
        Log.e("FormManager", "SUMMARY");

        for(int i = 0; i < this.allPages.size(); i++) {
            this.allPages.get(i).selectPossibleAnswers();
            Log.e("FormManager", "PAGE "+(i+1));

            int prevNo = 0;
            for(int j = 0; j < this.allPages.get(i).getFieldList().size(); j++) {
                Field f = this.allPages.get(i).getField(j);
                int qNo = f.getQuestion();

                if(f.isSelected()) {
                    if(prevNo != qNo) {
                        Log.e("FormManager", "");
                        Log.e("FormManager", "No. "+qNo+": "+this.questionList.get(qNo-1).getQuestion());
                    }

                    Log.e("FormManager", "Answer: "+f.getAnswer());
                    prevNo = qNo;
                }
            }
            Log.e("FormManager", "||||||||||||||||||||||||||");
            Log.e("FormManager", "");
        }
    }

    public ArrayList<FieldManager> getAllPages() {
        return allPages;
    }

    public FieldManager getPage(int index) {
        return this.allPages.get(index);
    }

    public void setAllPages(ArrayList<FieldManager> allPages) {
        this.allPages = allPages;
    }

    public void addPage(FieldManager fm) {
        this.allPages.add(fm);
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public ArrayList<String> getFinalAnswers() {
        return finalAnswers;
    }

    public void setFinalAnswers(ArrayList<String> finalAnswers) {
        this.finalAnswers = finalAnswers;
    }

    public ArrayList<Question> getQuestionList() {
        return questionList;
    }

    public Question getQuestion(int index) {
        return this.questionList.get(index);
    }

    public void addQuestion(Question question) {
        this.questionList.add(question);
    }

    public void setQuestionList(ArrayList<Question> questionList) {
        this.questionList = questionList;
    }

}
