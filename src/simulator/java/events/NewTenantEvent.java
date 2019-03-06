package events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class NewTenantEvent extends ScenarioEvent{
    public double baseline;
    public double amplitude;
    public double period;
    public double phase;
    public String tenantId;

    public NewTenantEvent(double eventTime, String tenantId, double baseline, double amplitude, double period, double phase) {
        super(eventTime);
        this.baseline = baseline;
        this.amplitude = amplitude;
        this.period = period;
        this.phase = phase;
        this.tenantId = tenantId;
    }
}
