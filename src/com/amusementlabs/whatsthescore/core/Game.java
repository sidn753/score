package com.amusementlabs.whatsthescore.core;

import com.amusementlabs.whatsthescore.util.Logr;

import java.io.Serializable;
import java.util.*;

import static com.amusementlabs.whatsthescore.util.Constants.NUM_MAX_DICE;
import static com.amusementlabs.whatsthescore.util.Constants.ONE_MINUTE;

public class Game implements Serializable {


    private String mName;
    private int type = 0; //type code, for future extention
    private int mRoundNum = 1; //round num is number of current active round
    private int mNumUsers = 0;
    private ArrayList<String> mUserNames; //this should always be sorted
    private HashMap<String, User> mUsers;
    private ArrayList<Die> mDice;
    private long mTimerSetting = ONE_MINUTE; //default value is 60 seconds


    private long mLastTimerValue = 0;

    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_SCORE_LTH = 1;
    public static final int SORT_BY_SCORE_HTL = 2;
    public static final int SORT_BY_USER_DEFINED = 3;


    public Game(String name) {
        mName = name;
        mUsers = new HashMap<String, User>();
        mUserNames = new ArrayList<String>();
        mDice = new ArrayList<Die>();
        initDefaultDice();
    }


    //default to one six sided die
    private void initDefaultDice() {
        mDice.add(new Die(6));
        //fill up to NUM_MAX_DICE with null
        for (int i = 1; i < NUM_MAX_DICE; i++) {
            mDice.add(null);
        }
    }


    public String getName() { return mName; }

    public int getType() { return type; }

    public int getRoundNum() { return mRoundNum; }

    public int getNumUsers() { return mNumUsers; }

    public ArrayList<String> getUserNames() { return mUserNames; }

    public ArrayList<Die> getDice() { return mDice; }

    public long getTimerSetting() { return mTimerSetting; }

    public long getLastTimerValue() {return mLastTimerValue; }


    public void setName(String mName) { this.mName = mName; }

    public void setType(int type) { this.type = type; }

    public void setTimerSetting(long time) { mTimerSetting = time; }

    public void setLastTimerValue(long mLastTimerValue) { this.mLastTimerValue = mLastTimerValue;}

//  public void setRoundNum(int mRoundNum) { this.mRoundNum = mRoundNum; }
//  public void setUserNames(ArrayList<User> mUserNames) { this.mUserNames = mUserNames; }
//  public void setNumDice(int mNumDice) { this.mNumDice = mNumDice; }
//  public void setNumPlayers(int mNumPlayers) { this.mNumPlayers = mNumPlayers; }
//  public void setDice(ArrayList<Die> mDice) { this.mDice = mDice; }

    public ArrayList<User> getUsers() {
        ArrayList<User> retVal = new ArrayList<User>(getNumUsers());

        for (String name : getUserNames()) {
            retVal.add(getUser(name));
        }

        return retVal;
    }


    public void addUser(User u) {

        mNumUsers++;
        mUserNames.add(u.getName());
        mUsers.put(u.getName(), u);
    }

    public boolean removeUser(User u) {
        mNumUsers--;
        return mUserNames.remove(u.getName()) && (mUsers.remove(u.getName()) != null);
    }

    public boolean removeUser(String name) {
        mNumUsers--;
        return mUserNames.remove(name) && (mUsers.remove(name) != null);
    }

    public void advanceRound() {
        for (String name : mUserNames) {
            getUser(name).advanceRound();
        }
        mRoundNum++;
    }

    public void addDie(Die d, int index) {
        mDice.set(index, d);
    }

    public void removeDie(int index) {
        mDice.set(index, null);
    }

    public User getUser(String name) {
        //Logr.d("Game- Getting user: "+ name);
        return mUsers.get(name);
    }

    public void updateUser(String oldName, User u) {

        User toUpdate = getUser(oldName);


        toUpdate.setName(u.getName());
        toUpdate.setTeam(u.getTeam());
        toUpdate.setColor(u.getColor());

        mUsers.remove(oldName);
        mUsers.put(toUpdate.getName(), toUpdate);

        mUserNames.remove(oldName);
        mUserNames.add(u.getName());

    }


    public void sortUserNamesList(int sortBy) {

        Comparator<String> comparator;

        if (sortBy == SORT_BY_NAME) {
            comparator = new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return getUser(lhs).getName().compareTo(getUser(rhs).getName());
                }
            };
        } else if (sortBy == SORT_BY_SCORE_HTL) {
            comparator = new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return getUser(rhs).getTotalScore() - getUser(lhs).getTotalScore();
                }
            };
        } else if (sortBy == SORT_BY_SCORE_LTH) {
            comparator = new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return getUser(lhs).getTotalScore() - getUser(rhs).getTotalScore();
                }
            };
        } else if (sortBy == SORT_BY_USER_DEFINED) {
            comparator = new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return getUser(lhs).getSortOrder() - getUser(rhs).getSortOrder();
                }
            };

        } else {
            Logr.e("Unknown sort order parameter passed into Game.sort()");
            return;
        }


        Collections.sort((List) mUserNames, comparator);
    }


    public int getTotalScore() {
        int retVal = 0;
        for (String name : mUserNames) {
            retVal += getUser(name).getTotalScore();
        }
        return retVal;
    }

    //default to resetting to 0
    public void resetGame() {
        resetGame(0);
    }


    public void resetGame(int resetScoreToValue) {
        mRoundNum = 1;
        for (User u : getUsers()) {
            u.resetUser(resetScoreToValue);
        }

    }


}
