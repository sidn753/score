package com.amusementlabs.whatsthescore.util;

public final class Constants {


    //Use:     import static com.amusementlabs.whatsthescore.util.Constants.*;

    //constants for teams
    public static final int NO_TEAM = 0;
    public static final int RED = 1;
    public static final int ORANGE = 2;
    public static final int YELLOW = 3;
    public static final int LIME_GREEN = 4;
    public static final int GREEN = 5;
    public static final int TEAL = 6;
    public static final int SKY_BLUE = 7;
    public static final int BLUE = 8;
    public static final int PURPLE = 9;
    public static final int PINK = 10;
    public static final int BEIGE = 11;
    public static final int BROWN = 12;
    public static final int BLACK = 13;
    public static final int GRAY = 14;


    public static final int EDIT_PLAYERS_REQUEST_CODE = 0;
    public static final int EDIT_PREFS_REQUEST_CODE = 1;


    public static final int INPUT_MODE_KEYBOARD = 0;
    public static final int INPUT_MODE_PLUS_ONE = 1;


    public static final int VIBRATION_DURATION = 500;

    public static final int ONE_SECOND = 1000;
    public static final int ONE_MINUTE = 60000;
    public static final int ONE_HOUR = 3600000;

    public static final String TIME_LEFT_STRING = "timeLeftString";
    public static final String TIME_LEFT = "timeLeft";
    public static final String SET_TIME_PARAM = "setTime";
    public static final String TIMER_FINISHED_ACTION = "timerFinished";
    public static final String TIMER_UI_THREAD_UPDATE_ACTION = "tickTock";

    private Constants() { }  // Prevents instantiation

    //deliminator for writing score lists to string
    public static final String DELIM = ";";

    //key for intent extra to indicate timer needs to be opened
    public static final String OPENED_FROM_TIMER_NOTIFICATION = "openedFromTimerNotification";

    public static final int NUM_MAX_DICE = 6;

    public static String millisToClockTimeString(long time) {

        long hours = time / ONE_HOUR;
        long mins = (time % ONE_HOUR) / ONE_MINUTE;
        long seconds = (time % ONE_MINUTE) / Constants.ONE_SECOND;

        String retVal = "";
        if (hours > 0) {
            retVal += "" + hours + ":";
        }

        if (mins > 0) {
            if (mins < 10 && hours > 0)
                retVal += "0"; //pad single digit minutes if there are hours

            retVal += "" + mins + ":";
        } else {
            retVal += (hours > 0) ? "00:" : "0:";  //double padding zeros if there are hours, otherwise one
        }

        if (seconds > 0) {
            if (seconds < 10) {
                retVal += "0"; //always pad single digit seconds
            }
            retVal += "" + seconds;
        } else {
            retVal += "00"; //always double padding zeros
        }


        return retVal;
    }


}