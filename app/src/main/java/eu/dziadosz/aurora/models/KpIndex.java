package eu.dziadosz.aurora.models;

/**
 * Created by Radosław on 03.11.2016.
 */

public class KpIndex {
    private Double kp;
    private Long time;

    public KpIndex(Double kp, Long time){
        setKp(kp);
        setTime(time);
    }

    public Double getKp() {
        return kp;
    }

    public void setKp(Double kp) {
        this.kp = kp;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long Long) {
        this.time = time;
    }
}
