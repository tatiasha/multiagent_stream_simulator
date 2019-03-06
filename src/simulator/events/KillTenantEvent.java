package simulator.events;

/**
 * Created by mikhail on 27.04.2017.
 */
public class KillTenantEvent extends ScenarioEvent {
    public final String tenantId;

    public KillTenantEvent(double eventTime, String tenantId) {
        super(eventTime);
        this.tenantId = tenantId;
    }
}
