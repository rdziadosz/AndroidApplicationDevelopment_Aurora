package eu.dziadosz.aurora.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import eu.dziadosz.aurora.R;
import eu.dziadosz.aurora.services.AuroraService;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public final static String LOG_TAG = MainFragment.class.getSimpleName();
    SharedPreferences sharedPreferences;

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    BroadcastReceiver reciever;

    private Typeface mTfLight;
    private PieChart pieChartProbability;
    private PieChart pieChartKp;
    private PieData data_probability;
    private PieData data_kp;
    private PieDataSet dataset_probability;
    private PieDataSet dataset_kp;
    TextView update_time;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mTfLight = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");

        reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(AuroraService.LOG_TAG + "Update")) {
                    Log.v(LOG_TAG, "BroadcastRecieved, updating data..." + intent.getAction().toString());
                    updateData();
                }
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reciever, new IntentFilter(AuroraService.LOG_TAG + "Update"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(reciever);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        dataset_probability = preparePieChartData(55);
        dataset_kp = preparePieChartData(77);
        data_probability = new PieData(dataset_probability);
        data_kp = new PieData(dataset_kp);

        pieChartProbability = (PieChart) view.findViewById(R.id.chartProbability);
        pieChartKp = (PieChart) view.findViewById(R.id.chartKp);

        preparePieChart(pieChartProbability, data_probability);
        preparePieChart(pieChartKp, data_kp);

        update_time = (TextView) view.findViewById(R.id.update_time);

        updateData();

        return view;
    }

    private void preparePieChart(PieChart pieChart, PieData data) {
        pieChart.setDrawCenterText(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDescription(null);
        pieChart.setCenterTextTypeface(mTfLight);
        pieChart.setHoleRadius(85);
        pieChart.setData(data);
    }

    private PieDataSet preparePieChartData(int value) {
        PieDataSet set = new PieDataSet(new ArrayList<PieEntry>(), null);
        set.clear();
        set.addEntry(new PieEntry(value, null));
        set.addEntry(new PieEntry(100 - value, null));
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        colors.add(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        set.setColors(colors);
        set.setDrawValues(false);
        return set;
    }

    private void updatePieDataSet(PieDataSet set, int value) {
        set.clear();
        set.addEntry(new PieEntry(value, null));
        set.addEntry(new PieEntry(100 - value, null));
        set.notifyDataSetChanged();
        Log.v(LOG_TAG, "Percentage:" + value);
    }

    private void updateData() {
        Integer nowProbability = sharedPreferences.getInt("probability_p", 0);
        updatePieDataSet(dataset_probability, nowProbability);

        Float nowKpIndex = sharedPreferences.getFloat("kp_max_hour", 0);
        updatePieDataSet(dataset_kp, Math.round(nowKpIndex * 100 / 9));

        updatePieChartData(pieChartProbability, "Probability", nowProbability.toString() + "%");
        updatePieChartData(pieChartKp, "KP Index", nowKpIndex.toString());

        Long success_update = sharedPreferences.getLong("success", 0);

        update_time.setText(DATE_TIME_FORMAT.format(success_update));
    }

    private void updatePieChartData(PieChart pieChart, String label, String value) {
        //prepare center text
        SpannableString s = new SpannableString(label + "\n" + value);
        s.setSpan(new RelativeSizeSpan(1.5f), 0, label.length(), 0);
        s.setSpan(new RelativeSizeSpan(2.4f), label.length(), s.length(), 0);

        pieChart.animateY(1000, Easing.EasingOption.EaseInOutExpo);
        pieChart.setCenterText(s);
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
