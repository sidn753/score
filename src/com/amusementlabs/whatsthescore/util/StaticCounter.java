package com.amusementlabs.whatsthescore.util;

public class StaticCounter {

    private static int mCount = 1;

    private static StaticCounter instance = null;

    protected StaticCounter() {
        // Exists only to defeat instantiation.
    }

    public static StaticCounter getInstance() {
        if (instance == null) {
            instance = new StaticCounter();
        }
        return instance;
    }

    public static int getCount() {
        return mCount;
    }

    public static void resetCount() {
        mCount = 1;
    }

    public static void setCount(int count) {
        mCount = count;
    }

    public static void incrementCount() {
        mCount++;
    }

    public static void decrementCount() {
        mCount--;
    }


}
