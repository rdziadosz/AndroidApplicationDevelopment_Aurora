package eu.dziadosz.aurora.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import eu.dziadosz.aurora.models.KpIndex;
import eu.dziadosz.aurora.models.Probability;
import eu.dziadosz.aurora.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    private static final String ARG_SECTION_NUMBER = "section_number";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    public void updateData() {

        //FetchWeatherTask weatherTask = new FetchWeatherTask();
        //weatherTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData();
    }
}
