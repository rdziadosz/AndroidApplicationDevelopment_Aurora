package eu.dziadosz.aurora.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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