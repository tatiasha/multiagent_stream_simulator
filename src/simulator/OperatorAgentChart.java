package simulator;

import org.jfree.data.xy.XYSeries;
import sim.display.Controller;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by mikhail on 02.05.2017.
 */
class OperatorAgentChart implements Steppable {
    private boolean isAlive = true;
    private final XYSeries inputSeries;
    private final XYSeries inputForecastSeries;
    private final XYSeries outputSeries;
    private final XYSeries outputForecastSeries;
    private final TimeSeriesChartGenerator chart;
    private final SinusoidalOperatorAgent agent;


    public OperatorAgentChart(Controller c, SinusoidalOperatorAgent agent, StreamStateWithUI gui) {
        StreamStateWithUI gui1 = gui;
        this.agent = agent;
        int outputPoints = agent.forecastPoints;
        // init
        chart = new TimeSeriesChartGenerator();
        chart.setTitle("Input\\Output for Operator: " + agent.id);
        chart.setRangeAxisLabel("Tuples per second");
        chart.setDomainAxisLabel("Time");
        JFrame chartFrame = chart.createFrame();
        chartFrame.setVisible(true);
        chartFrame.pack();
        c.registerFrame(chartFrame);

        // start
        chart.removeAllSeries();
        inputSeries = new XYSeries("Input data", false);
        inputForecastSeries = new XYSeries("Predicted input data", false);
        outputSeries = new XYSeries("Output data", false);
        outputForecastSeries = new XYSeries("Predicted output data", false);
        for (int i = 0; i < agent.outputForecast.size(); i++) {
            outputForecastSeries.add(agent.outputForecast.get(i)[0], agent.outputForecast.get(i)[1], false);
        }
        for (int i = 0; i < agent.inputForecast.size(); i++) {
            inputForecastSeries.add(agent.inputForecast.get(i)[0], agent.inputForecast.get(i)[1], false);
        }
        chart.addSeries(inputForecastSeries, null);
        chart.addSeries(inputSeries, null);
        chart.addSeries(outputForecastSeries, null);
        chart.addSeries(outputSeries, null);

        TimeSeriesAttributes inForAtt = new TimeSeriesAttributes(chart, inputForecastSeries, 0, null);
        inForAtt.setDashPattern(TimeSeriesAttributes.PATTERN_STRETCH_DASH);
        inForAtt.setStrokeColor(Color.blue);
        inForAtt.rebuildGraphicsDefinitions();

        TimeSeriesAttributes inAtt = new TimeSeriesAttributes(chart, inputSeries, 1, null);
        inAtt.setStrokeColor(Color.red);
        inAtt.rebuildGraphicsDefinitions();

        TimeSeriesAttributes outForAtt = new TimeSeriesAttributes(chart, outputForecastSeries, 2, null);
        outForAtt.setDashPattern(TimeSeriesAttributes.PATTERN_STRETCH_DASH);
        outForAtt.setStrokeColor(Color.orange);
        outForAtt.rebuildGraphicsDefinitions();
        outForAtt.setThickness(4);

        TimeSeriesAttributes outAtt = new TimeSeriesAttributes(chart, outputSeries, 3, null);
        outAtt.setStrokeColor(Color.green);
        outAtt.rebuildGraphicsDefinitions();
        outAtt.setThickness(4);


        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isAlive = false;
                e.getWindow().dispose();
            }
        });

    }

    @Override
    public void step(SimState simState) {
        if (isAlive) {
            int inputLength = agent.inputStatistics.size();
            int outputLength = agent.inputForecast.size();
            double[] inputPoint = agent.inputStatistics.get(inputLength - 1);
            double[] inputForecastPoint = agent.inputForecast.get(outputLength - 1);
            double[] outputPoint = agent.outputStatistics.get(inputLength - 1);
            double[] outputForecastPoint = agent.outputForecast.get(outputLength - 1);
            inputSeries.add(inputPoint[0], inputPoint[1], true);
            inputForecastSeries.add(inputForecastPoint[0], inputForecastPoint[1], true);
            outputSeries.add(outputPoint[0], outputPoint[1], true);
            outputForecastSeries.add(outputForecastPoint[0], outputForecastPoint[1], true);
            int inputPoints = 200;
            if (inputLength > inputPoints) {
                inputSeries.remove(0);
                outputSeries.remove(0);
            }
            outputForecastSeries.remove(0);
            inputForecastSeries.remove(0);
            chart.updateChartLater(simState.schedule.getSteps());
        }
    }


}
