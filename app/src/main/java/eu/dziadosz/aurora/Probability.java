package eu.dziadosz.aurora;

/**
 * Created by Radosław on 03.11.2016.
 */

public class Probability {

    private Integer probability;
    private Long time;
    private Long valid;

    Probability(Integer probability, Long time, Long valid) {
        setProbability(probability);
        setTime(time);
        setValid(valid);
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getValid() {
        return valid;
    }

    public void setValid(Long valid) {
        this.valid = valid;
    }
}
