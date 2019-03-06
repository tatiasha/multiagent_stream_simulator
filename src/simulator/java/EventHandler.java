import events.*;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;
import itmo.escience.streamsimulator.algorithms.signal_distr_ga.SDGene;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikhail on 27.04.2017.
 */
public class EventHandler {
    public static void handleEvent(ScenarioEvent event, StreamState state) {
        String eventClass = event.getClass().getSimpleName();
        System.out.println("Event case: " + eventClass);
        switch (eventClass) {
            case "NewNodeEvent": handleNewNode((NewNodeEvent) event, state);
                break;
            case "KillNodeEvent": handleKillNode((KillNodeEvent) event, state);
                break;
            case "NewTenantEvent": handleNewTenant((NewTenantEvent) event, state);
                break;
            case "KillTenantEvent": handleKillTenant((KillTenantEvent) event, state);
                break;
            case "StopSimEvent": handleStopSim((StopSimEvent) event, state);
                break;
            case "ScheduleEvent": handleSchedule((ScheduleEvent) event, state);
                break;
            default: System.out.println("Unpredicted event");
                break;
        }
    }

    public static void handleNewNode(NewNodeEvent event, StreamState state) {
        NodeAgent node = new NodeAgent("n"+state.NUId, event.cores);
        state.nodesMap.put(node.id, node);
        // find a free slot on grid
        int idx = 0; // TODO move to function
        for (int i = 0; i < state.nodes.getWidth(); i++) {
            if (state.nodes.numObjectsAtLocation(i, 0) == 0) {
                break;
            }
            idx++;
        }
        if (idx == state.nodes.getWidth()) {
            throw new IllegalStateException("Not enough nodes slots to place a new node");
        }

        state.nodes.setObjectLocation(node, idx, 0); // TODO find a free space for node (x)
        state.schedule.scheduleRepeating(node, 1, 1.0);
        state.NUId++;
    }

    public static void handleKillNode(KillNodeEvent event, StreamState state) {
        NodeAgent node = state.nodesMap.get(event.nodeId);
        Int2D nodeLoc = state.nodes.getObjectLocation(node);
        for (int i = 1; i < state.nodes.getHeight(); i++) {
            Bag ops = state.nodes.getObjectsAtLocation(nodeLoc.x, i);
            if (ops != null) {
                for (Object opObj : ops) {
                    SinusoidalOperatorAgent op = (SinusoidalOperatorAgent) opObj;
                    op.isExecuting = false;
                }
            }
        }
        // TODO remove from schedule
        state.nodes.remove(node);
        state.nodesMap.remove(event.nodeId);
    }

    public static void handleNewTenant(NewTenantEvent event, StreamState state) {
        SinusoidalOperatorAgent operator = new SinusoidalOperatorAgent("t"+state.WUId, event.baseline, event.amplitude, event.period, event.phase, event.eventTime);
        state.workloadMap.put(operator.id, operator);
        // find a place on grid
        int idx = 0; // TODO move to function
        for (int i = 0; i < state.workload.getWidth(); i++) {
            if (state.workload.numObjectsAtLocation(i, 2) == 0) {
                break;
            }
            idx++;
        }
        if (idx == state.workload.getWidth()) {
            throw new IllegalStateException("Not enough workload slots to place a new tenant");
        }

        state.workload.setObjectLocation(operator, idx, 2);

        state.schedule.scheduleRepeating(operator, 2, 1.0);
        state.dependencies.addNode(operator);
        String source = operator.id + "_source";
        String sink = operator.id + "_sink";
        state.workload.setObjectLocation(source, idx, 0);
        state.workload.setObjectLocation(sink, idx, 4);
        state.dependencies.addNode(source);
        state.dependencies.addNode(operator);
        state.dependencies.addNode(sink);
        state.dependencies.addEdge(source, operator, 1);
        state.dependencies.addEdge(operator, sink, 1);
        state.WUId++;

    }

    public static void handleKillTenant(KillTenantEvent event, StreamState state) {
        SinusoidalOperatorAgent operator = state.workloadMap.get(event.tenantId);
        operator.isExecuting = false;
        state.workload.remove(operator);
        state.workloadMap.remove(event.tenantId);
        Bag in = state.dependencies.getEdgesIn(operator);
        for (Object o : in) {
            Edge e = (Edge) o;
            Object from = e.from();
            state.workload.remove(from);
            state.dependencies.removeEdge(e);
            state.dependencies.removeNode(from);
        }
        Bag out = state.dependencies.getEdgesOut(operator);
        for (Object o : out) {
            Edge e = (Edge) o;
            Object to = e.to();
            state.dependencies.removeEdge(e);
            state.dependencies.removeNode(to);
            state.workload.remove(to);
        }
        state.dependencies.removeNode(operator);
        state.nodes.remove(operator);
        System.out.println();
    }

    public static void handleStopSim(StopSimEvent event, StreamState state) {
        state.finish();
    }

    public static void handleSchedule(ScheduleEvent event, StreamState state) {
        HashMap<String, ArrayList<SDGene>> algSolution = StreamScheduler.schedule(state);
        // apply schedule
        for (String key : algSolution.keySet()) {
            NodeAgent node = state.nodesMap.get(key);
            Int2D nodeLoc = state.nodes.getObjectLocation(node);
            ArrayList<SDGene> genes = algSolution.get(key);
            for (SDGene gene : genes) {
                String opId = gene.operatorId();
                SinusoidalOperatorAgent op = state.workloadMap.get(opId);
                boolean opPlaced = state.nodes.exists(op);
                if (!opPlaced) {
                    int opY = findSlot(state.nodes, nodeLoc.x);
                    state.nodes.setObjectLocation(op, nodeLoc.x, opY);
                    op.isExecuting = true;
                } else {
                    Int2D opLoc = state.nodes.getObjectLocation(op);
                    if (opLoc.x != nodeLoc.x) {
                        int opY = findSlot(state.nodes, nodeLoc.x);
                        state.nodes.setObjectLocation(op, nodeLoc.x, opY);
                        op.isExecuting = true;
                    } else {
                        op.isExecuting = true;
                    }
                }
            }
        }
    }

    public static int findSlot(SparseGrid2D schedGrid, int nodeX) {
        for (int i = 1; i < schedGrid.getHeight(); i++) {
            if (schedGrid.numObjectsAtLocation(nodeX, i) == 0) {
                return i;
            }
        }
        throw new IllegalStateException("Free slot not found for operator");
    }
}
