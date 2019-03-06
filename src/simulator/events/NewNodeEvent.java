package simulator.events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class NewNodeEvent extends ScenarioEvent {
    private final String nodeId;
    public final int cores;

    public NewNodeEvent(double eventTime, String nodeId, int cores) {
        super(eventTime);
        this.nodeId = nodeId;
        this.cores = cores;
    }
}
