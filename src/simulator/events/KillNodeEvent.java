package simulator.events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class KillNodeEvent extends ScenarioEvent {
    public final String nodeId;

    public KillNodeEvent(double eventTime, String nodeId) {
        super(eventTime);
        this.nodeId = nodeId;
    }
}
