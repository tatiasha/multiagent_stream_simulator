package events;

/**
 * Created by mikhail on 27.04.2017.
 */
public abstract class ScenarioEvent {
    public double eventTime;

    public ScenarioEvent(double eventTime) {
        this.eventTime = eventTime;
    }
}
