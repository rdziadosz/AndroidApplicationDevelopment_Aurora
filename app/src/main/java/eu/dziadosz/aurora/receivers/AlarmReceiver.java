package eu.dziadosz.aurora.receivers;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.net.URL;

import eu.dziadosz.aurora.services.AuroraService;

/**
 * Created by Radosław on 07.11.2016.
 */


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AuroraService.startService(context);
    }
}