package eu.dziadosz.aurora;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayAdapter<KpIndex> mKpIndexAdapter;
    private Probability mProbability;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    public void updateData() {

        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData();
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, JSONObject> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private void getWeatherDataFromJson(JSONObject forecastJson)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_KP = "kp";
            final String OWM_PROBABILITY = "probability";

            JSONObject probability = forecastJson.getJSONObject(OWM_PROBABILITY);
            mProbability=new Probability(probability.getInt("p"),probability.getLong("g"),probability.getLong("v"));

            JSONArray kpArray = forecastJson.getJSONArray(OWM_KP);
            for (int i = 0; i < kpArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject kpForecast = kpArray.getJSONObject(i);
                //mKpIndexAdapter.add(new KpIndex(kpForecast.getDouble("kp"),kpForecast.getLong("time")));
            }
            TextView probabilityTextView=(TextView)getActivity().findViewById(R.id.probabilityTextView);
            probabilityTextView.setText(mProbability.getProbability().toString());
            return;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject json = null;
           // if (params.length == 0) {
             //   return null;
            //}

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;


            try {
                final String FORECAST_BASE_URL =
                        "http://dev.dziadosz.eu/android/aurora/get.php?";
                final String LON_PARAM = "lon";
                final String LAT_PARAM = "lat";


                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String lon = sharedPrefs.getString("location_lon", "0.0");
                String lat = sharedPrefs.getString("location_lat", "0.0");

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LON_PARAM, lon.toString())
                        .appendQueryParameter(LAT_PARAM, lat.toString())
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
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
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecast JSON recived:" + forecastJsonStr);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                json = new JSONObject(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {

                try {
                    getWeatherDataFromJson(json);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
