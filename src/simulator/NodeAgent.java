package simulator;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Created by mikhail on 27.04.2017.
 */
public class NodeAgent implements Steppable {
    public final String id;
    public final int cores;

    public NodeAgent(String id, int cores) {
        this.id = id;
        this.cores = cores;
    }

    public void step(SimState simState) {
    }
}
