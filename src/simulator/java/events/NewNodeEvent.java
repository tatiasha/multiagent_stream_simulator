package events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class NewNodeEvent extends ScenarioEvent {
    public String nodeId;
    public int cores;

    public NewNodeEvent(double eventTime, String nodeId, int cores) {
        super(eventTime);
        this.nodeId = nodeId;
        this.cores = cores;
    }
}
