package events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class KillNodeEvent extends ScenarioEvent {
    public String nodeId;

    public KillNodeEvent(double eventTime, String nodeId) {
        super(eventTime);
        this.nodeId = nodeId;
    }
}
