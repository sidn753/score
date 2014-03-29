package com.amusementlabs.whatsthescore.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.amusementlabs.whatsthescore.R;
import com.amusementlabs.whatsthescore.core.Game;

import static com.amusementlabs.whatsthescore.util.Constants.INPUT_MODE_KEYBOARD;
import static com.amusementlabs.whatsthescore.util.Constants.INPUT_MODE_PLUS_ONE;


public class PrefsHelper {


    private static boolean mShouldConfirm;
    private static boolean mShowRoundNum;
    private static boolean mShowTotalScore;
    private static boolean mUseNotification;
    private static boolean mVibrateOnCompletion;
    private static int mInputMode;
    private static int mSortingMode;
    private static String mRingtoneUri;

    private static PrefsHelper mInstance = null;

    private static Game mGame;


    //private ctor to prevent instantiation
    private PrefsHelper() {

    }

    public static PrefsHelper getInstance() {
        if (mInstance == null) {
            mInstance = new PrefsHelper();
        }
        return mInstance;
    }


    public static void updatePrefs(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        //convert input mode pref value to int code
        String inputMode = sp.getString(context.getString(R.string.prefs_title_input), "");
        if (inputMode.equals(context.getString(R.string.pref_input_keyboard)))
            mInputMode = INPUT_MODE_KEYBOARD;
        else if (inputMode.equals(context.getString(R.string.pref_input_plusone)))
            mInputMode = INPUT_MODE_PLUS_ONE;
        else
            Logr.e("ActMain- on ActPrefs Result- unknown input mode");

        //convert sorting mode pref value to int code
        String sortingMode = sp.getString(context.getString(R.string.prefs_title_sorting), "");
        if (sortingMode.equals(context.getString(R.string.pref_sorting_name)))
            mSortingMode = Game.SORT_BY_NAME;
        else if (sortingMode.equals(context.getString(R.string.pref_sorting_score_high_to_low)))
            mSortingMode = Game.SORT_BY_SCORE_HTL;
        else if (sortingMode.equals(context.getString(R.string.pref_sorting_score_low_to_high)))
            mSortingMode = Game.SORT_BY_SCORE_LTH;
        else if (sortingMode.equals(context.getString(R.string.pref_sorting_user)))
            mSortingMode = Game.SORT_BY_USER_DEFINED;
        else
            Logr.e("ActMain- on ActPrefs Result- unknown sorting mode");


        //actually sort the game
        //call getGame() in case the game hasnt been created, it will create one
        if (mGame != null)
            mGame.sortUserNamesList(mSortingMode);


        //set activity local fields for easy preference reference
        mShouldConfirm = sp.getBoolean(context.getString(R.string.prefs_title_save_conf), false);
        mShowRoundNum = sp.getBoolean(context.getString(R.string.prefs_title_show_round), true);
        mShowTotalScore = sp.getBoolean(context.getString(R.string.prefs_title_show_total), true);
        mUseNotification = sp.getBoolean(context.getString(R.string.prefs_title_timer_notification), true);
        mVibrateOnCompletion = sp.getBoolean(context.getString(R.string.prefs_title_timer_vibrate), true);
        mRingtoneUri = sp.getString(context.getString(R.string.prefs_title_timer_ringtone), "");
    }

    public static boolean shouldConfirmRound() {return mShouldConfirm; }

    public static boolean showRoundNum() { return mShowRoundNum; }

    public static boolean showTotalScore() { return mShowTotalScore; }

    public static boolean useNotificationForTimer() { return mUseNotification; }

    public static boolean vibrateOnTimerCompletion() { return mVibrateOnCompletion; }

    public static int getInputMode() { return mInputMode; }

    public static int getSortingMode() { return mSortingMode; }

    public static String getRingtoneUri() { return mRingtoneUri; }


}
