package com.example.guesssongs.Class;

import java.util.List;

public class Score {
    private String userId;
    private int score_classic;
    private List<Integer> classic_scores;

    private Score(){

    }
    public Score(String userId, int score_classic, List<Integer> classic_scores) {
        this.userId = userId;
        this.score_classic = score_classic;
        this.classic_scores = classic_scores;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore_classic() {
        return score_classic;
    }

    public void setScore_classic(int score_classic) {
        this.score_classic = score_classic;
    }

    public List<Integer> getClassic_scores() {
        return classic_scores;
    }

    public void setClassic_scores(List<Integer> classic_scores) {
        this.classic_scores = classic_scores;
    }
}
