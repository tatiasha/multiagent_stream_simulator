package simulator.events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class NewTenantEvent extends ScenarioEvent{
    public final double baseline;
    public final double amplitude;
    public final double period;
    public final double phase;
    private final String tenantId;

    public NewTenantEvent(double eventTime, String tenantId, double baseline, double amplitude, double period, double phase) {
        super(eventTime);
        this.baseline = baseline;
        this.amplitude = amplitude;
        this.period = period;
        this.phase = phase;
        this.tenantId = tenantId;
    }
}
