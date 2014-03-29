package com.amusementlabs.whatsthescore.util;


import android.content.Context;
import android.content.SharedPreferences;
import com.amusementlabs.whatsthescore.core.Game;
import com.google.gson.Gson;


public class GameDataManager {


    public static final String SHARED_PREFS_KEY_SAVED_DATA = "isTimerRunning";
    private static final String SHARED_PREFS_KEY_PLAYER_NUM_COUNTER = "playerNumberCounter";
    private static final String PREF_KEY_PLAYER_NUM_COUNTER = "playerNumberCounterValue";
    public static final String DEFAULT_GAME_NAME = "myGame"; //dummy game name


    private static GameDataManager mInstance = null;

    private static Game mGame;
    private static boolean gameInMemoryIsFresh = false;


    //private ctor to prevent instantiation
    private GameDataManager() {
    }

    public static GameDataManager getInstance() {
        if (mInstance == null) {
            mInstance = new GameDataManager();
        }
        return mInstance;
    }

    //No name resets default game
    public static Game resetGame(Context context) {
        return resetGame(context, DEFAULT_GAME_NAME, 0);
    }

    public static Game resetGame(Context context, int resetAllScoresToValue) {
        return resetGame(context, DEFAULT_GAME_NAME, resetAllScoresToValue);
    }

    public static Game resetGame(Context context, String name, int resetAllScoresToValue) {
        Context appContext = context.getApplicationContext();

        mGame.resetGame(resetAllScoresToValue);
        //StaticCounter.resetCount(); //reset player counter

//        storeGame(appContext, mGame);

        return mGame;
    }


    public static void storeGame(Context context, Game game) {
        Context appContext = context.getApplicationContext();

        SharedPreferences sp = appContext.getSharedPreferences(SHARED_PREFS_KEY_SAVED_DATA, 0);
        SharedPreferences.Editor prefsEditor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(game);
        prefsEditor.putString(game.getName(), json);
        prefsEditor.commit();

        //persist the player counter
        sp = appContext.getSharedPreferences(SHARED_PREFS_KEY_PLAYER_NUM_COUNTER, Context.MODE_PRIVATE);
        prefsEditor = sp.edit();
        prefsEditor.putInt(PREF_KEY_PLAYER_NUM_COUNTER, StaticCounter.getCount());
        prefsEditor.commit();

        gameInMemoryIsFresh = false;

    }

    //no name gets default game
    public static Game getGame(Context context) {
        return getGame(context, DEFAULT_GAME_NAME);
    }


    public static Game getGame(Context context, String name) {

        if (gameInMemoryIsFresh) {
            return mGame;
        } else {
            Context appContext = context.getApplicationContext();
            SharedPreferences sp = appContext.getSharedPreferences(SHARED_PREFS_KEY_SAVED_DATA, 0);

            Gson gson = new Gson();
            String json = sp.getString(name, "");

            Game game;
            if (!json.equals("")) {
                game = gson.fromJson(json, Game.class);

            } else {

                game = new Game(name); //if game does not exist, create one
                storeGame(appContext, game);
            }

            //restore the player counter
            sp = appContext.getSharedPreferences(SHARED_PREFS_KEY_PLAYER_NUM_COUNTER, Context.MODE_PRIVATE);
            StaticCounter.setCount(sp.getInt(PREF_KEY_PLAYER_NUM_COUNTER, 1));

            mGame = game;
            gameInMemoryIsFresh = true;
            return game;
        }
    }


}
