package eu.dziadosz.aurora;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import eu.dziadosz.aurora.receivers.AlarmReceiver;
import eu.dziadosz.aurora.services.AuroraService;

/**
 * Created by Radosław on 08.11.2016.
 */

public class AuroraApplication extends Application {

    private final static String LOG_TAG = AuroraApplication.class.getSimpleName();
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setBackgroundFetcher();
    }

    public void setBackgroundFetcher() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Integer sync_frequency = Integer.parseInt(sharedPreferences.getString("sync_frequency", "30"));

        Log.v(LOG_TAG, "sync_frequency: " + sync_frequency.toString());
        if (sync_frequency > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), sync_frequency * 60 * 1000, pending);
        } else {
            alarmManager.cancel(pending);
        }
    }
}