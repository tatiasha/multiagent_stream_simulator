package simulator;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikhail on 25.04.2017.
 */
public class StreamStateWithUI extends GUIState {
    // workload
    private Display2D wldDisplay;
    private JFrame wldDisplayFrame;
    private final SparseGridPortrayal2D frameworkPortrayal = new SparseGridPortrayal2D();
    private final NetworkPortrayal2D dependenciesPortrayal = new NetworkPortrayal2D();

    //environment
    private Display2D envDisplay;
    private JFrame envDisplayFrame;
    private final SparseGridPortrayal2D nodesPortrayal = new SparseGridPortrayal2D();

    // agents
    private ArrayList<String> operatorsSetPorts = null;
    private ArrayList<String> nodesSetPorts = null;
    private HashMap<String, OperatorAgentChart> opCharts;
    private HashMap<String, NodeAgentChart> nodeCharts;

    private Controller controller;

    public static void main(String[] args) {
        StreamStateWithUI vid = new StreamStateWithUI();
        Console c = new Console(vid);
        c.setVisible(true);
    }

    public Object getSimulationInspectedObject() {
        return state;
    }

    public Inspector getInspector() {
        Inspector i = super.getInspector();
        i.setVolatile(true);
        return i;
    }

    public StreamStateWithUI(SimState state) {
        super(state);
    }

    private StreamStateWithUI() {
        super(new StreamState(System.currentTimeMillis()));
    }

    public static String getName() {
        return "Stream simulator";
    }

    public void start() {
        super.start();
        operatorsSetPorts = new ArrayList<>();
        nodesSetPorts = new ArrayList<>();
        opCharts = new HashMap<>();
        nodeCharts = new HashMap<>();
        setupPortrayals();
//        TestChart test = new TestChart(controller, this);
//        state.schedule.scheduleRepeating(test, 100, 1.0);
    }

    public void load(SimState state) {
        super.load(state);
        operatorsSetPorts = new ArrayList<>();
        nodesSetPorts = new ArrayList<>();
        opCharts = new HashMap<>();
        nodeCharts = new HashMap<>();
        setupPortrayals();
    }

    private void setupPortrayals() {
        StreamState streamState = (StreamState) state;

        frameworkPortrayal.setField(streamState.workload);
        dependenciesPortrayal.setField(new SpatialNetwork2D(streamState.workload, streamState.dependencies));

        SimpleEdgePortrayal2D edge = new SimpleEdgePortrayal2D();
        dependenciesPortrayal.setPortrayalForAll(edge);

        nodesPortrayal.setField(streamState.nodes);

        wldDisplay.reset();
        wldDisplay.setBackdrop(Color.LIGHT_GRAY);
        wldDisplay.repaint();

        envDisplay.reset();
        envDisplay.setBackdrop(Color.WHITE);
        envDisplay.repaint();
    }

    @Override
    public boolean step() {
        StreamState streamState = (StreamState) state;
        for (SinusoidalOperatorAgent opAgent : streamState.workloadMap.values()) {
            if (!operatorsSetPorts.contains(opAgent.id)) {
                Color clr = new Color(0.3F+0.7F*streamState.random.nextFloat(), 0.3F+0.7F*streamState.random.nextFloat(), 0.3F+0.7F*streamState.random.nextFloat(),0.8F);
                OperatorAgentPortrayal port = new OperatorAgentPortrayal(clr, opAgent.id);
                operatorsSetPorts.add(opAgent.id);
                frameworkPortrayal.setPortrayalForObject(opAgent, port);
                nodesPortrayal.setPortrayalForObject(opAgent, port);
            }
        }

        for (NodeAgent nodeAgent : streamState.nodesMap.values()) {
            if (!nodesSetPorts.contains(nodeAgent.id)) {
                Color clr = new Color(streamState.random.nextFloat(), streamState.random.nextFloat(), streamState.random.nextFloat());
                NodeAgentPortrayal port = new NodeAgentPortrayal(clr, nodeAgent.id);
                nodesSetPorts.add(nodeAgent.id);
                nodesPortrayal.setPortrayalForObject(nodeAgent, port);
                runNodeChart(nodeAgent);
            }
        }

        return super.step();
    }

    public void init(Controller c) {
        super.init(c);
        controller = c;
        wldDisplay = new Display2D(400, 200, this);
        wldDisplay.setClipping(false);
        wldDisplayFrame = wldDisplay.createFrame();
        wldDisplayFrame.setTitle("Workload");
        c.registerFrame(wldDisplayFrame);
//        wldDisplayFrame.setVisible(true);
        wldDisplay.attach(dependenciesPortrayal, "dependencies");
        wldDisplay.attach(frameworkPortrayal, "workload");

        envDisplay = new Display2D(300, 500, this);
        envDisplay.setClipping(false);
        envDisplayFrame = envDisplay.createFrame();
//        envDisplayFrame.setLocation(0, 400);
        envDisplayFrame.setTitle("Schedule");
        c.registerFrame(envDisplayFrame);
        envDisplayFrame.setVisible(true);
        envDisplay.attach(nodesPortrayal, "nodes");


    }

    public void quit() {
        super.quit();
        if (wldDisplayFrame != null) {
            wldDisplayFrame.dispose();
        }
        wldDisplayFrame = null;
        wldDisplay = null;

        if (envDisplayFrame != null) {
            envDisplayFrame.dispose();
        }
        envDisplayFrame = null;
        envDisplay = null;

    }

    public void runOperatorChart(SinusoidalOperatorAgent agent) {
        OperatorAgentChart newChart = new OperatorAgentChart(controller, agent, this);
        opCharts.put(agent.id, newChart);
        state.schedule.scheduleRepeating(newChart, 666, 1.0);
    }

    public void runNodeChart(NodeAgent agent) {
        NodeAgentChart newChart = new NodeAgentChart(controller, agent, this);
        nodeCharts.put(agent.id, newChart);
        state.schedule.scheduleRepeating(newChart, 666, 1.0);

    }
}
