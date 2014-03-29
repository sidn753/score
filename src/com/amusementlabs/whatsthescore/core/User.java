package com.amusementlabs.whatsthescore.core;

import java.io.Serializable;
import java.util.ArrayList;

import static com.amusementlabs.whatsthescore.util.Constants.NO_TEAM;

public class User implements Serializable {


    private String mName;
    private int mTeam = NO_TEAM;
    private int mSortOrder;
    private int mColor = NO_TEAM;
    private int mTotalScore = 0;
    private int mPendingScore = 0;
    private int mRoundCount = 0;
    private ArrayList<Integer> mScores;

    public String getName() { return mName; }

    public int getTeam() { return mTeam; }

    public int getSortOrder() { return mSortOrder; }

    public int getColor() { return mColor; }

    public int getTotalScore() { return mTotalScore; }

    public int getPendingScore() { return mPendingScore; }

    public int getRoundCount() { return mRoundCount; }

    public ArrayList<Integer> getScores() { return mScores; }

    public void setPendingScore(int pendingScore) { this.mPendingScore = pendingScore; }

    public void setName(String name) { mName = name; }

    public void setTeam(int team) { mTeam = team; }

    public void setColor(int color) { mColor = color; }

    public void setSortOrder(int sortOrder) { mSortOrder = sortOrder; }


    //for late adds
    public User(String name, int team, int color, int round) {
        this(name, team, color);
        if (round > 1)
            catchUpToRound(round);
    }

    public User(String name, int team, int color) {
        this();
        mName = name;
        mTeam = team;
        mColor = color;
    }


    private User() {
        mScores = new ArrayList<Integer>();
    }

    public void advanceRound() {
        mTotalScore += mPendingScore;
        mScores.add(mRoundCount++, mPendingScore);
        mPendingScore = 0;
    }

    public void decrementPendingScore() {
        mPendingScore--;
    }

    public void incrementPendingScore() {
        mPendingScore++;
    }

    //zero fills score array for late add to game
    private void catchUpToRound(int round) {
        mRoundCount = round - 1;
        for (int i = 1; i < round; i++) {
            mScores.add(0);
        }
    }

    //default to resetting to 0
    public void resetUser() {
        resetUser(0);
    }

    public void resetUser(int resetScoreToValue) {
        mRoundCount = 0;
        mPendingScore = 0;
        mTotalScore = resetScoreToValue;
        mScores.clear();
    }


}
