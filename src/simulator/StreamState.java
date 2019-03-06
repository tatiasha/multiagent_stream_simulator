package simulator;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;

import java.util.HashMap;

/**
 * Created by mikhail on 25.04.2017.
 */
public class StreamState extends SimState {
    // workload fields
    public final SparseGrid2D workload = new SparseGrid2D(14, 5);
    public final HashMap<String, SinusoidalOperatorAgent> workloadMap = new HashMap<>();
    public int WUId = 0;
    // nodes fields
    public final SparseGrid2D nodes = new SparseGrid2D(6, 10);
    public final HashMap<String, NodeAgent> nodesMap = new HashMap<>();
    public int NUId = 0;

    public final Network dependencies = new Network();

    public StreamState(long seed)
    {
        super(seed);
    }

    public int getNumberOfTenants() {
        return workloadMap.size();
    }
    public int getNumberOfNodes() {
        return nodesMap.size();
    }

    @Override
    public void start() {
        super.start();
        nodes.clear();
        nodesMap.clear();
        workload.clear();
        workloadMap.clear();
        dependencies.clear();
        WUId = 0;
        NUId = 0;
        // select scenario here
        schedule.scheduleRepeating(new ScenarioAgent(ScenarioAgent.testScenario2()), 0, 1.0);
    }
}
