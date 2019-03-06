package simulator;

import simulator.events.*;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mikhail on 27.04.2017.
 */
class ScenarioAgent implements Steppable {
    private final List<ScenarioEvent> scenario;

    public ScenarioAgent(List<ScenarioEvent> scenario) {
        this.scenario = scenario;
    }


    public static List<ScenarioEvent> operatorScenario() {
        List<ScenarioEvent> scenario = new ArrayList<>();
        // Env
        scenario.add(new NewNodeEvent(0.0, "n0", 2));
        // Workload
        scenario.add(new NewTenantEvent(0.0, "t0", 1000, 500, 40, 0));

        scenario.add(new ScheduleEvent(0.0));
        scenario.add(new ScheduleEvent(100.0));
        return scenario;
    }

    public static List<ScenarioEvent> testScenario1() {
        List<ScenarioEvent> scenario = new ArrayList<>();
        // Env
        scenario.add(new NewNodeEvent(0.0, "n0", 20));
        scenario.add(new NewNodeEvent(0.0, "n1", 20));
        // Workload
        scenario.add(new NewTenantEvent(0.0, "t0", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t1", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t2", 249, 40, 40, Math.PI));
        scenario.add(new NewTenantEvent(0.0, "t3", 249, 40, 40, Math.PI));

        scenario.add(new ScheduleEvent(0.0));
        scenario.add(new ScheduleEvent(100.0));
        return scenario;
    }

    public static List<ScenarioEvent> testScenario2() {
        List<ScenarioEvent> scenario = new ArrayList<>();
        // Env
        scenario.add(new NewNodeEvent(0.0, "n0", 20));
        scenario.add(new NewNodeEvent(0.0, "n1", 20));
        scenario.add(new NewNodeEvent(0.0, "n2", 20));
        scenario.add(new NewNodeEvent(0.0, "n3", 20));
//        scenario.add(new NewNodeEvent(0.0, "n4", 20));
//        scenario.add(new NewNodeEvent(0.0, "n5", 20));
        // Workload
        scenario.add(new NewTenantEvent(0.0, "t0", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t1", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t2", 249, 40, 40, Math.PI));
        scenario.add(new NewTenantEvent(0.0, "t3", 249, 40, 40, Math.PI));
        scenario.add(new NewTenantEvent(0.0, "t4", 249, 40, 40, Math.PI / 2));
        scenario.add(new NewTenantEvent(0.0, "t5", 249, 40, 40, Math.PI / 2*3));
        scenario.add(new NewTenantEvent(200.0, "t6", 249, 40, 40, Math.PI / 2));
        scenario.add(new NewTenantEvent(200.0, "t7", 249, 40, 40, Math.PI / 2));
        scenario.add(new NewTenantEvent(200.0, "t8", 249, 40, 40, Math.PI / 2 * 3));
        scenario.add(new NewTenantEvent(200.0, "t9", 249, 40, 40, Math.PI / 2 * 3));

        scenario.add(new ScheduleEvent(0.0));
        scenario.add(new ScheduleEvent(100.0));
        scenario.add(new ScheduleEvent(300.0));
        return scenario;
    }

    public static List<ScenarioEvent> testScenario3() {
        List<ScenarioEvent> scenario = new ArrayList<>();
        // Env
        scenario.add(new NewNodeEvent(0.0, "n0", 20));
        scenario.add(new NewNodeEvent(0.0, "n1", 20));
        scenario.add(new NewNodeEvent(0.0, "n2", 20));
        scenario.add(new NewNodeEvent(0.0, "n3", 20));
        // Workload
        scenario.add(new NewTenantEvent(0.0, "t0", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t1", 249, 40, 40, 0));
        scenario.add(new NewTenantEvent(0.0, "t2", 249, 40, 40, Math.PI));
        scenario.add(new NewTenantEvent(0.0, "t3", 249, 40, 40, Math.PI));
        scenario.add(new NewTenantEvent(200.0, "t6", 249, 40, 40, Math.PI / 2));
        scenario.add(new NewTenantEvent(200.0, "t8", 249, 40, 40, Math.PI / 2 * 3));

        scenario.add(new ScheduleEvent(0.0));
        scenario.add(new ScheduleEvent(100.0));
        scenario.add(new ScheduleEvent(300.0));
        return scenario;
    }

    public void step(SimState simState) {
        StreamState state = (StreamState) simState;
        double time = state.schedule.getTime();
        List<ScenarioEvent> curEvents = scenario.stream().filter(event -> event.eventTime == time).collect(Collectors.toList());
        for (ScenarioEvent event : curEvents) {
            EventHandler.handleEvent(event, state);
            scenario.remove(event);
        }
    }
}
