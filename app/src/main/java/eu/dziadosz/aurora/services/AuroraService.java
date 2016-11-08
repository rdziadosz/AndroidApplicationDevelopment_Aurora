package eu.dziadosz.aurora.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import eu.dziadosz.aurora.R;
import eu.dziadosz.aurora.models.AllData;
import eu.dziadosz.aurora.models.KpIndex;
import eu.dziadosz.aurora.models.Probability;
import eu.dziadosz.aurora.receivers.AlarmReceiver;
import eu.dziadosz.aurora.ui.MainActivity;

/**
 * Created by Radosław on 06.11.2016.
 */

public class AuroraService extends IntentService {

    private final static String DATA_BASE_URL = "http://dev.dziadosz.eu/android/aurora/get.php?";
    private final static String LOG_TAG = AuroraService.class.getSimpleName();
    private URL DATA_URL;

    private static final long NOTIFICATION_FREQUENCY_TIME = 1000 * 60 * 60 * 3;

    private final static int MORSE_DOT = 200;
    private final static int MORSE_DASH = 500;
    private final static int MORSE_SHORT_GAP = 200;
    private final static int MORSE_MEDIUM_GAP = 500;
    private final static int MORSE_LONG_GAP = 1000;

    SharedPreferences sharedPreferences;

    public AuroraService() {
        super("AuroraService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, AuroraService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            DATA_URL = buildUrl(DATA_BASE_URL);

            if (!isOnline()) {
                sharedPreferences.edit().putInt("error", 1).apply();
                sharedPreferences.edit().putString("details", "").apply();
                notifyUpdateError(1, "");
                emitStatusUpdated(false);
                return;
            }

            try {
                sharedPreferences.edit().putInt("error", 0).apply();
                emitStatusUpdated(true);
                sharedPreferences.edit().putLong("updated", System.currentTimeMillis()).apply();
                String fetchedData = fetchPage(DATA_URL);
                AllData allData = getDataFromJsonStr(fetchedData);
                storeData(allData);
                generateNotification(allData);
                cancelNotify(1);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to download file: " + DATA_URL.toString(), e);

                sharedPreferences.edit().putInt("error", 1).apply();
                sharedPreferences.edit().putString("details", e.getLocalizedMessage()).apply();

                notifyUpdateError(2, e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse downloaded file: " + DATA_URL.toString(), e);

                sharedPreferences.edit().putInt("error", 3).apply();
                sharedPreferences.edit().putString("details", e.getLocalizedMessage()).apply();

                notifyUpdateError(3, e.getLocalizedMessage());
            } finally {
                emitStatusUpdated(false);
            }
        }
    }

    private URL buildUrl(String baseUrl) {
        final String LON_PARAM = "lon";
        final String LAT_PARAM = "lat";

        String lon = sharedPreferences.getString("location_lon", "0.0");
        String lat = sharedPreferences.getString("location_lat", "0.0");

        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(LON_PARAM, lon.toString())
                .appendQueryParameter(LAT_PARAM, lat.toString())
                .build();
        try {
            Log.v(LOG_TAG, "Built URI " + builtUri.toString());
            return new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private void emitStatusUpdated(boolean pending) {
        notifyChecking(pending);
        //EventBus.getDefault().post(new StatusUpdatedEvent(pending));
    }

    private void generateNotification(AllData data) {
        Long lastNotificationTime = sharedPreferences.getLong("last_notification_time", 0);
        Integer minKpIndex = Integer.parseInt(sharedPreferences.getString("notifications_min_kp", "4"));
        Integer minProbability = Integer.parseInt(sharedPreferences.getString("notifications_min_probability", "30"));

        Integer nowKpIndex = sharedPreferences.getInt("probability_p", 0);
        Integer nowProbability = sharedPreferences.getInt("kp_max_hour", 0);

        Boolean time=(lastNotificationTime+NOTIFICATION_FREQUENCY_TIME)<System.currentTimeMillis();
        Boolean reachedProbability = nowProbability>minProbability;
        Boolean reachedKpIndex = nowKpIndex>minKpIndex;

        if(time && (reachedProbability||reachedKpIndex)) {
            CharSequence title = getString(R.string.notify_title_aurora);
            CharSequence content = null;
            if (reachedProbability && reachedKpIndex) {
                content = getString(R.string.notify_content_both);
            } else if (reachedProbability) {
                content = getString(R.string.notify_content_probability);
            } else {
                content = getString(R.string.notify_content_kp);
            }
            showNotify(title, content, R.drawable.ic_notifications_black_24dp, 0, true, Color.YELLOW);
            sharedPreferences.edit().putLong("last_notification_time", System.currentTimeMillis()).apply();
        }
    }

    private void notifyUpdateError(int type, String error) {
        if (type == 0) {
            cancelNotify(1);
            return;
        }

        CharSequence title = getString(R.string.notify_title_error);
        String content = getResources().getStringArray(R.array.error_type_array)[type - 1];
        CharSequence details = error.length() == 0 ? "" : "\n\n" + error;
        showNotify(title, content + details, R.drawable.ic_notifications_black_24dp, 1, false, Color.RED);
    }

    private void notifyChecking(boolean visible) {
        if (visible) {
            showNotify(getString(R.string.notify_title_checking), getString(R.string.notify_content_checking), R.drawable.ic_notifications_black_24dp, 2, false, 0);
        } else {
            cancelNotify(2);
        }
    }

    private void showNotify(CharSequence title, CharSequence content, int icon, int id, boolean loud, int color) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        Boolean pref_ringtone = sharedPreferences.getBoolean("notifications_aurora_ringtone", true);
        Boolean pref_vibrate = sharedPreferences.getBoolean("notifications_aurora_vibrate", true);

        if (loud) {
            if (pref_vibrate) {
                builder.setVibrate(new long[]{MORSE_DOT, MORSE_SHORT_GAP, MORSE_DASH, MORSE_SHORT_GAP, MORSE_DASH, MORSE_SHORT_GAP, MORSE_DOT});
            }

            if (pref_ringtone) {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        }

        if (color != 0) {
            builder.setLights(color, 3000, 3000);
        }

        Notification n = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(id, n);
    }

    private void cancelNotify(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private static String fetchPage(URL url) throws IOException {

        HttpURLConnection connection = null;

        BufferedReader reader = null;
        String dataJsonStr = null;

        // Create the request and open the connection
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        try {
            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                dataJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                dataJsonStr = null;
            }
            dataJsonStr = buffer.toString();
            Log.v(LOG_TAG, "JSON recived:" + dataJsonStr);

            return dataJsonStr;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                reader.close();
            }
        }

    }

    private AllData getDataFromJsonStr(String jsonStr) throws JSONException {
        JSONObject forecastJson = new JSONObject(jsonStr);
        AllData allData = new AllData();
        List<KpIndex> listKpIndex = new ArrayList<KpIndex>();

        // These are the names of the JSON objects that need to be extracted.
        final String API_KP = "kp";
        final String API_PROBABILITY = "probability";

        JSONObject probability = forecastJson.getJSONObject(API_PROBABILITY);
        allData.setProbability(new Probability(probability.getInt("p"), probability.getLong("g"), probability.getLong("v")));

        JSONArray kpArray = forecastJson.getJSONArray(API_KP);
        for (int i = 0; i < kpArray.length(); i++) {
            JSONObject kpForecast = kpArray.getJSONObject(i);
            listKpIndex.add(new KpIndex(kpForecast.getDouble("kp"), kpForecast.getLong("time")));
        }
        allData.setKpIndexList(listKpIndex);
        return allData;
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void storeData(AllData data) {

        sharedPreferences.edit().putInt("probability_p", data.getProbability().getProbability()).apply();
        sharedPreferences.edit().putLong("probability_g", data.getProbability().getTime()).apply();
        sharedPreferences.edit().putLong("probability_v", data.getProbability().getValid()).apply();

        sharedPreferences.edit().putLong("success", System.currentTimeMillis()).apply();
        sharedPreferences.edit().putLong("updated", System.currentTimeMillis()).apply();
    }


}
