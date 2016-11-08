package eu.dziadosz.aurora.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import eu.dziadosz.aurora.AuroraApplication;
import eu.dziadosz.aurora.services.AuroraService;

/**
 * Created by Radosław on 07.11.2016.
 */


public class BootReceiver extends BroadcastReceiver {

    private final static String LOG_TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {
            ((AuroraApplication) context.getApplicationContext()).setBackgroundFetcher();
        }
    }
}