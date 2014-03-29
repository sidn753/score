package com.amusementlabs.whatsthescore;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.util.Constants;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import com.amusementlabs.whatsthescore.util.PrefsHelper;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerBuilder;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerDialogFragment;
import org.androidannotations.annotations.*;

import static com.amusementlabs.whatsthescore.util.Constants.*;

@EFragment(R.layout.frag_timer)
@OptionsMenu(R.menu.frag_timer)
public class FragTimer extends SherlockFragment implements HmsPickerDialogFragment.HmsPickerDialogHandler {

    private static final String SHARED_PREFS_KEY_TIMER = "timerPrefsKey";
    public static final String PREF_KEY_IS_TIMER_RUNNING = "isTimerRunning";
    private static final boolean NOT_RUNNING = false;
    private static final boolean RUNNING = true;

    private Context mContext;


    private Game mGame;
    private long mTimeLeft;
    private long mSetTime = 0;


    private PendingIntent mAlarmPendingIntent;

    @ViewById(R.id.timer_view)
    TextView mTimerView;

    @ViewById(R.id.resetButton)
    Button mResetButton;

    @ViewById(R.id.startButton)
    Button mStartButton;

    @ViewById(R.id.frag_timer_button_container)
    LinearLayout mButtonBar;

    @SystemService
    NotificationManager mNotificationManager;

    @SystemService
    AudioManager mAudioManager;

    @SystemService
    AlarmManager mAlarmManager;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        GameDataManager.storeGame(mContext, mGame);
        mContext.unregisterReceiver(onTimerTick);


    }

    @Override
    public void onResume() {
        super.onResume();

        mGame = GameDataManager.getGame(mContext);

        mSetTime = mGame.getTimerSetting();
        if ((mTimeLeft = mGame.getLastTimerValue()) == 0)
            mTimeLeft = mSetTime;

        mContext.registerReceiver(onTimerTick, new IntentFilter(TIMER_UI_THREAD_UPDATE_ACTION));


        if (isTimerRunning()) {
            mContext.registerReceiver(onTimerFinished, new IntentFilter(TIMER_FINISHED_ACTION));
            setUIState(RUNNING);

        } else {
            setUIState(NOT_RUNNING);
            setTimerDisplay(mTimeLeft);
        }


    }

    private boolean isTimerRunning() {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(SHARED_PREFS_KEY_TIMER, 0);
        return sharedPrefs.getBoolean(PREF_KEY_IS_TIMER_RUNNING, false);
    }


    @OptionsItem(R.id.menu_timer)
    boolean showTimePicker() {
        HmsPickerBuilder hpb = new HmsPickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setStyleResId(com.doomonafireball.betterpickers.R.style.BetterPickersDialogFragment_Light)
                .setTargetFragment(FragTimer.this);
        hpb.show();
        return true;
    }


    @Click(R.id.startButton)
    void startStop() {
        if (isTimerRunning()) { //pause
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void pauseTimer() {

        mGame.setLastTimerValue(mTimeLeft);
        stopTimer();

    }

    private void stopTimer() {


        mContext.stopService(new Intent(mContext, ServiceTimer_.class));

        mContext.unregisterReceiver(onTimerFinished);
        mAlarmManager.cancel(mAlarmPendingIntent);


        SharedPreferences sharedPrefs = mContext.getSharedPreferences(SHARED_PREFS_KEY_TIMER, 0);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putBoolean(PREF_KEY_IS_TIMER_RUNNING, false);
        prefsEditor.commit();


        setUIState(NOT_RUNNING);
    }

    private void startTimer() {

        if (mSetTime == 0)
            return; //timer not started yet

        Intent intent = new Intent(mContext, ServiceTimer_.class);
        intent.putExtra(SET_TIME_PARAM, mTimeLeft);
        mContext.startService(intent);

        //store running timer flag in prefs
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(SHARED_PREFS_KEY_TIMER, 0);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putBoolean(PREF_KEY_IS_TIMER_RUNNING, true);
        prefsEditor.commit();

        //register timer finished receiver
        mContext.registerReceiver(onTimerFinished, new IntentFilter(TIMER_FINISHED_ACTION));

        //if previous timer completion notification exists, clear it
        mNotificationManager.cancel(R.id.notification_id_timer_ongoing);

        //create new alarm
        Intent i = new Intent(TIMER_FINISHED_ACTION);
        mAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 0, i, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTimeLeft, mAlarmPendingIntent);

        setUIState(RUNNING);
    }

    @Click(R.id.resetButton)
    void resetTimer() {

        if (mSetTime == 0)
            return; //timer hasnt been set yet

        if (isTimerRunning()) { //timer was running
            stopTimer();
        }

        mTimeLeft = mSetTime;
        mGame.setLastTimerValue(mTimeLeft);
        setTimerDisplay(mSetTime);
    }


    private void setUIState(boolean timerRunning) {
        if (timerRunning) {
            mButtonBar.setBackgroundColor(getResources().getColor(R.color.button_bar_bg_red));
            mStartButton.setText(mContext.getString(R.string.timer_pause_button_text));
        } else {
            mButtonBar.setBackgroundColor(getResources().getColor(R.color.brand_green));
            mStartButton.setText(mContext.getString(R.string.timer_start_button_text));
        }

        //clear notification if it is displayed
        try {
            mNotificationManager.cancel(R.id.notification_id_timer_complete);
        } catch (NullPointerException e) {
            //do nothing, notification was not being displayed
        }
    }


    private void setTimerDisplay(long time) {
        mTimerView.setText(Constants.millisToClockTimeString(time));
    }

    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {

        if (isTimerRunning())
            stopTimer();

        long time = clockTimeToMillis(hours, minutes, seconds);
        mSetTime = mTimeLeft = time;
        mGame.setTimerSetting(time);
        setTimerDisplay(mSetTime);

    }


    private BroadcastReceiver onTimerTick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {

            if (isTimerRunning()) {
                long time = i.getLongExtra(TIME_LEFT, 0);
                String timeString = i.getStringExtra(TIME_LEFT_STRING);
                mTimeLeft = time;
                mTimerView.setText(timeString);
            }

        }
    };


    //caleld when the timer finishes
    private BroadcastReceiver onTimerFinished = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {

            mTimerView.setText(millisToClockTimeString(0));

            String ringtoneUri = PrefsHelper.getRingtoneUri();
            if (ringtoneUri != null && !ringtoneUri.equals("")) {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

                Uri notificationSoundUri = Uri.parse(ringtoneUri);

                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(mContext, notificationSoundUri);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    mediaPlayer.release();
                    return;
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                try {
                    mediaPlayer.prepare();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    mediaPlayer.release();
                    return;
                }
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();

                    }
                });
                mediaPlayer.setVolume(volume, volume);
                mediaPlayer.start();

            }


            //if notification is enabled in settings
            if (PrefsHelper.useNotificationForTimer()) {


                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);


                Intent resultIntent = new Intent(mContext, ActMain.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

                resultIntent.putExtra(OPENED_FROM_TIMER_NOTIFICATION, true);

                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


                builder.setContentIntent(resultPendingIntent)
                        .setContentTitle(getString(R.string.notification_timer_complete_title))
                        .setSmallIcon(R.drawable.ic_timer_white)
                        .setContentText(getString(R.string.notification_timer_complete_content))
                        .setAutoCancel(true);


                mNotificationManager.notify(R.id.notification_id_timer_complete, builder.build());

            }

            //if vibration is enable in settings
            if (PrefsHelper.vibrateOnTimerCompletion()) {

                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(VIBRATION_DURATION);
            }

            //call after notification is created, so it can be auto cleared if the fragment is open
            stopTimer();
            resetTimer();
        }
    };


    private long clockTimeToMillis(int hours, int mins, int seconds) {

        return hours * ONE_HOUR + mins * ONE_MINUTE + seconds * ONE_SECOND;

    }


}
