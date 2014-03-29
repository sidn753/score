package com.amusementlabs.whatsthescore.core;

import java.io.Serializable;

import static com.amusementlabs.whatsthescore.util.Constants.DELIM;


public class Die implements Serializable {

    private int mNumSides = 0;
    private int mLastVal = 0;

    public Die(int numSides) {
        mNumSides = numSides;
    }


    public int getLastVal() { return mLastVal; }

    public int getNumSides() { return mNumSides; }

    public void setLastVal(int lastVal) { mLastVal = lastVal; }

    public void setNumSides(int numSides) { this.mNumSides = numSides; }


    public String toString() {
        String retVal = "";

        retVal += mNumSides + DELIM;
        retVal += mLastVal + DELIM;

        return retVal;
    }


}
