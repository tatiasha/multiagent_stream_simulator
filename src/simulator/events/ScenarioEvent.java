package simulator.events;

/**
 * Created by mikhail on 27.04.2017.
 */
public abstract class ScenarioEvent {
    public final double eventTime;

    ScenarioEvent(double eventTime) {
        this.eventTime = eventTime;
    }
}
