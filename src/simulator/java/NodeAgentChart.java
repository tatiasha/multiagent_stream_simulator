import ec.util.MersenneTwisterFast;
import org.jfree.data.xy.XYSeries;
import sim.display.Controller;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by mikhail on 02.05.2017.
 */
public class NodeAgentChart implements Steppable {

    private boolean isAlive = true;

    private ArrayList<XYSeries> statSeriesList;
    private ArrayList<XYSeries> forecastSeriesList;
    private XYSeries totalStatSeries;
    private XYSeries totalForecastSeries;

    private TimeSeriesChartGenerator chart;
    private JFrame chartFrame;
    private NodeAgent agent;
    private ArrayList<SinusoidalOperatorAgent> opAgents;

    private int inputPoints = 200;
    private int forecastPoints = 50;
    private StreamState state;
    private ArrayList<TimeSeriesAttributes> attr = new ArrayList<>();
    TimeSeriesAttributes totalStatAtt;
    TimeSeriesAttributes totalForAtt;

    public NodeAgentChart(Controller c, NodeAgent agent, StreamStateWithUI gui) {
        StreamState streamState = (StreamState) gui.state;
        this.state = streamState;

        this.agent = agent;

        int agentX = streamState.nodes.getObjectLocation(agent).getX();
        opAgents = new ArrayList<>();
        for (int i = 1; i < streamState.nodes.getHeight(); i++) {
            Bag objects = streamState.nodes.getObjectsAtLocation(agentX, i);
            if (objects != null) {
                for (Object obj : objects) {
                    SinusoidalOperatorAgent op = (SinusoidalOperatorAgent) obj;
                    opAgents.add(op);
                }
            }
        }

        if (!opAgents.isEmpty()) {
            forecastPoints = opAgents.get(0).forecastPoints;
        }

        // init
        chart = new TimeSeriesChartGenerator();
        chart.setTitle("Statistics for Node: " + agent.id);
        chart.setYAxisLabel("Tuples per second");
        chart.setXAxisLabel("Time");
        chartFrame = chart.createFrame(false);
        chartFrame.setVisible(true);
        chartFrame.pack();
        chartFrame.setSize(550, 380);
        chartFrame.setLocation(state.random.nextInt(400), state.random.nextInt(300));
        c.registerFrame(chartFrame);

        // start
        chart.removeAllSeries();

        int seriesNumber = opAgents.size();
        statSeriesList = new ArrayList<>();
        forecastSeriesList = new ArrayList<>();
        double[] inputForecastSum = new double[forecastPoints];

        for (int i = 0; i < seriesNumber; i++) {
            SinusoidalOperatorAgent op = opAgents.get(i);
            statSeriesList.add(new XYSeries("stat " + op.id));
            forecastSeriesList.add(new XYSeries("forecast " + op.id));
            for (int j = 0; j < forecastPoints; j++) {
                forecastSeriesList.get(i).add(op.outputForecast.get(j)[0], op.outputForecast.get(j)[1]);
                inputForecastSum[j] += op.outputForecast.get(j)[1];
            }
        }
        totalStatSeries = new XYSeries("Total statistics");
        totalForecastSeries = new XYSeries("Total forecast");
        double curTime = state.schedule.getTime();
        for (int j = 0; j < forecastPoints; j++) {
            totalForecastSeries.add(curTime + j + 1.0, inputForecastSum[j]);
        }

        totalStatAtt = (TimeSeriesAttributes) chart.addSeries(totalStatSeries, null);
//        totalStatAtt = new TimeSeriesAttributes(chart, totalStatSeries, 0, null);
        totalStatAtt.setThickness(4);
        totalStatAtt.setStrokeColor(Color.RED);
        totalStatAtt.rebuildGraphicsDefinitions();
        totalForAtt = (TimeSeriesAttributes) chart.addSeries(totalForecastSeries, null);
//        totalForAtt = new TimeSeriesAttributes(chart, totalForecastSeries, 1, null);
        totalForAtt.setStrokeColor(totalStatAtt.getStrokeColor());
        totalForAtt.setDashPattern(TimeSeriesAttributes.PATTERN_DASH);
        totalForAtt.setThickness(4);
        totalForAtt.rebuildGraphicsDefinitions();

        MersenneTwisterFast rnd = state.random;
        for (int i = 0; i < seriesNumber; i++) {
            TimeSeriesAttributes statAtt = (TimeSeriesAttributes) chart.addSeries(statSeriesList.get(i), null);
            Color strokeColor = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
            statAtt.setStrokeColor(strokeColor);

            TimeSeriesAttributes forAtt = (TimeSeriesAttributes) chart.addSeries(forecastSeriesList.get(i), null);
            forAtt.setStrokeColor(statAtt.getStrokeColor());
            forAtt.setDashPattern(TimeSeriesAttributes.PATTERN_DASH);
            attr.add(statAtt);
            attr.add(forAtt);
        }
        JSplitPane splitPane = (JSplitPane) totalStatAtt.getParent().getParent().getParent().getParent().getParent().getParent();
        splitPane.setDividerLocation(0);
        splitPane.setDividerSize(1);
        JPanel rightComponent = (JPanel) splitPane.getRightComponent();
        rightComponent.getComponent(1).setVisible(false);
        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isAlive = false;
                e.getWindow().dispose();
            }
        });

    }


    public void checkForNewState() {
        if (!state.nodesMap.containsKey(agent.id)) {
            chartFrame.dispose();
            isAlive = false;
            return;
        }
        int agentX = state.nodes.getObjectLocation(agent).getX();
        ArrayList<SinusoidalOperatorAgent> curAgents = new ArrayList<>();
        for (int i = 1; i < state.nodes.getHeight(); i++) {
            Bag objects = state.nodes.getObjectsAtLocation(agentX, i);
            if (objects != null) {
                for (Object obj : objects) {
                    SinusoidalOperatorAgent op = (SinusoidalOperatorAgent) obj;
                    curAgents.add(op);
                }
            }
        }
        ArrayList<SinusoidalOperatorAgent> agentsToRemove = new ArrayList<>();
        ArrayList<XYSeries> statSeriesToRemove = new ArrayList<>();
        ArrayList<XYSeries> forSeriesToRemove = new ArrayList<>();
        ArrayList<Integer> seriesFromChartRemove = new ArrayList<>();
        ArrayList<TimeSeriesAttributes> attrToRemove = new ArrayList<>();
        for (SinusoidalOperatorAgent op : opAgents) {
            if (!curAgents.contains(op)) {
                int idx = opAgents.indexOf(op);
                XYSeries statSeries = statSeriesList.get(idx);
                XYSeries forSeries = forecastSeriesList.get(idx);
                TimeSeriesAttributes statAtt = attr.get(2*idx);
                TimeSeriesAttributes forAtt = attr.get(2*idx + 1);
                seriesFromChartRemove.add(statAtt.getSeriesIndex());
                seriesFromChartRemove.add(forAtt.getSeriesIndex());
                statSeriesToRemove.add(statSeries);
                forSeriesToRemove.add(forSeries);
                attrToRemove.add(statAtt);
                attrToRemove.add(forAtt);
                agentsToRemove.add(op);
            }
        }

        seriesFromChartRemove.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -1*Integer.compare(o1, o2);
            }
        });
        for (int i : seriesFromChartRemove) {
            chart.removeSeries(i);
        }

        for (TimeSeriesAttributes at : attrToRemove) {
            at.clear();
            attr.remove(at);
        }

        for (SinusoidalOperatorAgent op : agentsToRemove) {
            opAgents.remove(op);
        }
        for (XYSeries s : statSeriesToRemove) {
            statSeriesList.remove(s);
        }
        for (XYSeries s : forSeriesToRemove) {
            forecastSeriesList.remove(s);
        }

        for (int i = 0; i < attr.size(); i++) {
            TimeSeriesAttributes at = attr.get(i);
            at.setSeriesIndex(i+2);
            at.rebuildGraphicsDefinitions();
        }

        for (SinusoidalOperatorAgent op : curAgents) {
            if (!opAgents.contains(op)) {
                addNewSeries(op);
                opAgents.add(op);
            }
        }
    }

    public void addNewSeries(SinusoidalOperatorAgent op) {

        int seriesNumber = statSeriesList.size();
        statSeriesList.add(new XYSeries("stat " + op.id));
        forecastSeriesList.add(new XYSeries("forecast " + op.id));
        for (int j = 0; j < forecastPoints; j++) {
            forecastSeriesList.get(seriesNumber).add(op.outputForecast.get(j)[0], op.outputForecast.get(j)[1]);
        }

        TimeSeriesAttributes statAtt = (TimeSeriesAttributes) chart.addSeries(statSeriesList.get(seriesNumber), null);
        Color strokeColor = new Color(state.random.nextFloat(), state.random.nextFloat(), state.random.nextFloat());
        statAtt.setStrokeColor(strokeColor);

        TimeSeriesAttributes forAtt = (TimeSeriesAttributes) chart.addSeries(forecastSeriesList.get(seriesNumber), null);
        forAtt.setDashPattern(TimeSeriesAttributes.PATTERN_DASH);
        forAtt.setStrokeColor(statAtt.getStrokeColor());
        attr.add(statAtt);
        attr.add(forAtt);
    }

    @Override
    public void step(SimState simState) {
        if (isAlive) {
            checkForNewState();

            double sumInput = 0;
            double sumOutput = 0;
            for (int i = 0; i < forecastSeriesList.size(); i++) {
                SinusoidalOperatorAgent op = opAgents.get(i);
                int inputLength = op.outputStatistics.size();
                int outputLength = op.outputForecast.size();
                double[] inputPoint = op.outputStatistics.get(inputLength - 1);
                double[] inputForecastPoint = op.outputForecast.get(outputLength - 1);
                sumInput += inputPoint[1];
                sumOutput += inputForecastPoint[1];
                statSeriesList.get(i).add(inputPoint[0], inputPoint[1], true);
                forecastSeriesList.get(i).add(inputForecastPoint[0], inputForecastPoint[1], true);
                if (statSeriesList.get(i).getItemCount() > inputPoints) {
                    statSeriesList.get(i).remove(0);
                }
                forecastSeriesList.get(i).remove(0);
            }
            double curTime = simState.schedule.getTime();
            totalStatSeries.add(curTime, sumInput, true);
            totalForecastSeries.add(curTime + forecastPoints + 1, sumOutput, true);
            if (totalStatSeries.getItemCount() > inputPoints) {
                totalStatSeries.remove(0);
            }
            totalForecastSeries.remove(0);

            chart.updateChartLater(simState.schedule.getSteps());
        }
    }
}
