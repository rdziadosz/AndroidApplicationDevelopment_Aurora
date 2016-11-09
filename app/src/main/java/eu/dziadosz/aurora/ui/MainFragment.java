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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.dziadosz.aurora.R;
import eu.dziadosz.aurora.services.AuroraService;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public final static String LOG_TAG = MainFragment.class.getSimpleName();
    SharedPreferences sharedPreferences;
    private static final String ARG_SECTION_NUMBER = "section_number";
    BroadcastReceiver reciever;

    private Typeface mTfLight;
    private PieChart pieChartProbability;
    private PieChart pieChartKp;
    private PieData data;
    private PieData data_null;

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

        pieChartProbability = (PieChart) view.findViewById(R.id.chartProbability);
        pieChartKp = (PieChart) view.findViewById(R.id.chartKp);
        preparePieChart(pieChartProbability);
        preparePieChart(pieChartKp);


        data = prepareFakePieChartData(100);
        data_null = prepareFakePieChartData(0);

        updateData();

        return view;
    }

    private void preparePieChart(PieChart pieChart) {
        pieChart.setDrawCenterText(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDescription(null);
        pieChart.setCenterTextTypeface(mTfLight);
        pieChart.setHoleRadius(85);
    }

    private PieData prepareFakePieChartData(int value) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(value, ""));
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(ColorTemplate.JOYFUL_COLORS[0]);
        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(colors);
        set.setDrawValues(false);
        return new PieData(set);
    }


    private void updateData() {
        Integer nowProbability = sharedPreferences.getInt("probability_p", 0);
        //TODO remove test random data
        Random r = new Random();
        nowProbability = r.nextInt(80) + 10;

        Float nowKpIndex = sharedPreferences.getFloat("kp_max_hour", 0);
        Log.v(LOG_TAG, "kp tera" + nowKpIndex + "zaokr" + Math.round(nowKpIndex*100 / 9));
        updatePieChartData(pieChartProbability, nowProbability, "Probability", nowProbability.toString()+"%");
        updatePieChartData(pieChartKp, Math.round(nowKpIndex*100 / 9), "KP Index", nowKpIndex.toString());
    }

    private void updatePieChartData(PieChart pieChart, Integer percentage, String label, String value) {
        //prepare center text
        SpannableString s = new SpannableString(label + "\n" + value);
        s.setSpan(new RelativeSizeSpan(1.5f), 0, label.length(), 0);
        s.setSpan(new RelativeSizeSpan(2.4f), label.length(), s.length(), 0);

        //set data and animation
        if (percentage > 0) {
            pieChart.setMaxAngle(percentage * 3.6f);
            pieChart.setData(data);
        } else {
            pieChart.setData(data_null);
            pieChart.setMaxAngle(360);
        }

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieChart.setCenterText(s);
        pieChart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
