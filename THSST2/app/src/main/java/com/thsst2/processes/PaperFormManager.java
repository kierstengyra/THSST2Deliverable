package com.thsst2.processes;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Type: Process
 * PaperFormManager handles the paper form module.
 */
public class PaperFormManager {

    private static PaperFormManager instance = null;
    private ArrayList<FieldManager> allPages;
    private int finalScore;
    private ArrayList<String> finalAnswers;
    private ArrayList<Question> questionList;

    protected PaperFormManager() {
        this.allPages = new ArrayList<FieldManager>();
        this.questionList = new ArrayList<Question>();
    }

    public static PaperFormManager getInstance() {
        if(instance == null)
            instance = new PaperFormManager();

        return instance;
    }

    public void summarize(String studentName, String studentLastName, String schoolName) {
        Log.e("PaperFormManager", "Summary");
        int total = 0;

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Results/"+schoolName);
            if(!root.exists()) {
                root.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File resultsFile = new File(root, studentLastName+"_PHY_"+timeStamp+".txt");
            FileWriter writer = new FileWriter(resultsFile);

            writer.append(studentName+"\n\n");

            for(int m = 0; m < this.allPages.size(); m++)
                this.allPages.get(m).setAnswers();

            for(int i = 0; i < this.getQuestionList().size(); i++) {
                Question q = this.getQuestion(i);

                writer.append((i+1)+". "+q.getQuestion()+"\n");
                for(int j = 0; j < q.getScoreList().size(); j++) {
                    if(q.getScoreList().size() == 1) {
                        if(q.getAnswer(j).equals("Minsan nangyayari"))
                            total++;
                        else if(q.getAnswer(j).equals("Madalas nangyayari"))
                            total += 2;
                    }

                    writer.append("A: " + q.getAnswer(j));
                }

                writer.append("\n\n");
            }

            writer.append("TOTAL SCORE: "+total);
            writer.flush();
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void summarize1() {
        Log.e("PaperFormManager", "SUMMARY");

        for(int i = 0; i < this.allPages.size(); i++) {
            this.allPages.get(i).selectPossibleAnswers();
            Log.e("PaperFormManager", "PAGE "+(i+1));

            int prevNo = 0;
            for(int j = 0; j < this.allPages.get(i).getFieldList().size(); j++) {
                Field f = this.allPages.get(i).getField(j);
                int qNo = f.getQuestion();

                if(f.isSelected()) {
                    if(prevNo != qNo) {
                        Log.e("PaperFormManager", "No. "+qNo+": "+this.questionList.get(qNo-1).getQuestion());
                    }

                    Log.e("PaperFormManager", "Answer: "+f.getAnswer());
                    prevNo = qNo;
                }
            }
            Log.e("PaperFormManager", "||||||||||||||||||||||||||");
        }
    }

    public boolean isComplete() {
        int ctr = 0;

        for(int i = 0; i < this.allPages.size(); i++) {
            if(this.allPages.get(i).containsPicture())
                ctr++;
        }

        if(ctr == 6)
            return true;
        else
            return false;
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
