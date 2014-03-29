package com.amusementlabs.whatsthescore;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.amusementlabs.whatsthescore.util.Constants;
import com.amusementlabs.whatsthescore.util.Logr;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import static com.amusementlabs.whatsthescore.util.Constants.*;

@EService
public class ServiceTimer extends IntentService {
    private boolean mIsRunning;
    private long mSetTime;

    @SystemService
    NotificationManager mNotificationManager;

    NotificationCompat.Builder mBuilder;

    private long mTimeLeft = 0;


    public ServiceTimer() {
        super("ServiceTimer");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mIsRunning = true;
        setTimer(intent.getLongExtra(SET_TIME_PARAM, 0));

        mBuilder = new NotificationCompat.Builder(this);


        Intent resultIntent = new Intent(this, ActMain.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        resultIntent.putExtra(OPENED_FROM_TIMER_NOTIFICATION, true);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(resultPendingIntent)
                .setContentTitle(getString(R.string.notification_timer_in_progress_text))
                .setSmallIcon(R.drawable.ic_timer_white)
                .setProgress(100, 100, false)
                .setContentText(getTimeLeftString());


        startForeground(R.id.notification_id_timer_ongoing, mBuilder.build());
        mIsRunning = true;


        while (mIsRunning) {
            //get percent time left
            int percentElapsed = (int) (((double) mTimeLeft / (double) mSetTime) * 100);

            if (mTimeLeft > 0) {

                mBuilder.setProgress(100, percentElapsed, false)
                        .setContentText(getTimeLeftString());

                mNotificationManager.notify(R.id.notification_id_timer_ongoing, mBuilder.build());

                mTimeLeft -= ONE_SECOND;

                synchronized (this) {
                    try {
                        wait(ONE_SECOND);
                    } catch (Exception e) {
                    }
                }
                updateUIThread();

            } else { //timer is finished


                updateUIThread();
                mIsRunning = false;
                stopForeground(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        Logr.d("ServiceTimer Destroyed");
        mIsRunning = false;
        super.onDestroy();
    }


    private void updateUIThread() {

        Intent i = new Intent(TIMER_UI_THREAD_UPDATE_ACTION);
        i.putExtra(TIME_LEFT, mTimeLeft);
        i.putExtra(TIME_LEFT_STRING, getTimeLeftString());
        sendBroadcast(i);


    }


    private void setTimer(long time) {
        Logr.d("TimerService- New Timer: " + time / 1000 + " seconds");
        mSetTime = mTimeLeft = time;
    }


    private String getTimeLeftString() {
        return Constants.millisToClockTimeString(mTimeLeft);
    }


}