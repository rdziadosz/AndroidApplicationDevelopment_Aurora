package eu.dziadosz.aurora.models;

import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Radosław on 06.11.2016.
 */

public class AllData {

    private List<KpIndex> kpIndexList;
    private Probability probability;

    public List<KpIndex> getKpIndexList() {
        return kpIndexList;
    }

    public void setKpIndexList(List<KpIndex> kpIndexList) {
        this.kpIndexList = kpIndexList;
    }

    public Probability getProbability() {
        return probability;
    }

    public void setProbability(Probability probability) {
        this.probability = probability;
    }
}
