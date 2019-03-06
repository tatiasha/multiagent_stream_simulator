import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikhail on 25.04.2017.
 */
public class SinusoidalOperatorAgent implements Steppable {

    public String id;
    public double baseline;
    public double amplitude;
    public double period;
    public double phase;

    public List<double[]> inputStatistics;
    public List<double[]> inputForecast;
    public List<double[]> outputStatistics;
    public List<double[]> outputForecast;
    public List<double[]> queueStatistics;

    public boolean isExecuting;

    public int forecastPoints = 50;
    private double peak; // TODO just for mock. Get from merged signal from schedule.

    public double getInput() {
        return inputStatistics.get(inputStatistics.size()-1)[1];
    }
    public double getOutput() {
        return outputStatistics.get(outputStatistics.size()-1)[1];
    }

    public SinusoidalOperatorAgent(String id, double baseline, double amplitude, double period, double phase, double initTime) {
        this.id = id;
        this.baseline = baseline;
        this.amplitude = amplitude;
        this.period = period;
        this.phase = phase;
        inputStatistics = new ArrayList<>();
        inputForecast = new ArrayList<>(forecastPoints);
        for (int i = 0; i < forecastPoints; i++) {
            inputForecast.add(new double[]{initTime+i+1.0, 100.0});
        }
        outputStatistics = new ArrayList<>();
        outputForecast = new ArrayList<>(forecastPoints);
        for (int i = 0; i < forecastPoints; i++) {
            outputForecast.add(new double[]{initTime+i+1.0, 100.0});
        }
        queueStatistics = new ArrayList<>();
        isExecuting = false;
        peak = baseline * 1.25;
//        peak = baseline * 1.15;
    }

    public void step(SimState state) {
        StreamState streamState = (StreamState) state;
        double currentTime = streamState.schedule.getTime();

        double inputTuples = baseline + amplitude * Math.sin(2.0 * Math.PI / period * currentTime + phase) + 5*streamState.random.nextDouble();

        inputStatistics.add(new double[]{currentTime, inputTuples});
        double queueTuples = 0.0;
        if (!queueStatistics.isEmpty()) {
            queueTuples = queueStatistics.get(queueStatistics.size() - 1)[1];
        }
        double totalInput = inputTuples + queueTuples;
        double processedTuples = 0.0;
        if (isExecuting) {
            processedTuples = Math.min(totalInput, peak); // TODO merge signals on node, estimate the peak for this operator
        }
        outputStatistics.add(new double[]{currentTime, processedTuples});
        double notProcessedTuples = totalInput - processedTuples;
        if (notProcessedTuples < 0) {
            throw new IllegalStateException("negative queue");
        }
        queueStatistics.add(new double[]{currentTime, notProcessedTuples});

        inputForecast.remove(0);
        inputForecast.add(new double[]{currentTime + forecastPoints, predcitInput(currentTime)});
        outputForecast.remove(0);
        outputForecast.add(new double[]{currentTime + forecastPoints, predictOutput(currentTime)});
    }

    public double predcitInput(double time) {
        // TODO prediction by using python ML server
        return baseline + amplitude * Math.sin(2.0 * Math.PI / period * (time + forecastPoints) + phase);
    }

    public double predictOutput(double time) {
        // TODO prediction by using python ML server
        double value =  baseline + amplitude * Math.sin(2.0 * Math.PI / period * (time + forecastPoints) + phase);
        if (outputStatistics.size() > (int)forecastPoints) {
            int timeDiff = forecastPoints - (int)period;
            value=outputStatistics.get(outputStatistics.size()-(int)period + timeDiff-1)[1];
        }
        return Math.min(peak, value);
    }
}
