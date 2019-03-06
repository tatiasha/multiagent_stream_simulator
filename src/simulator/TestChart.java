package simulator;

import org.jfree.data.xy.XYSeries;
import sim.display.Controller;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by mikhail on 03.05.2017.
 */
class TestChart implements Steppable {

    private final ArrayList<XYSeries> seriesList;

    private final TimeSeriesChartGenerator chart;

    private ArrayList<TimeSeriesAttributes> attr = new ArrayList<>();

    public TestChart(Controller c, StreamStateWithUI gui) {
        StreamState state = (StreamState) gui.state;

        // init
        chart = new TimeSeriesChartGenerator();
        chart.setTitle("Test");
        chart.setXAxisLabel("Tuples per second");
        chart.setYAxisLabel("Time");
        JFrame chartFrame = chart.createFrame();
        chartFrame.setVisible(true);
        chartFrame.pack();
        c.registerFrame(chartFrame);
        // start
        chart.removeAllSeries();

        seriesList = new ArrayList<>();

        for (int i = 0; i < 10; i ++) {
            XYSeries series = new XYSeries("s" + i);
            seriesList.add(series);
            chart.addSeries(series,null);
        }

//        XYSeries series = new XYSeries("s1");
//        seriesList.add(series);
//        chart.addSeries(seriesList.get(0),null);
//
//        XYSeries series2 = new XYSeries("s2");
//        chart.addSeries(series2, null);
//        seriesList.add(series2);

//        TimeSeriesAttributes att = new TimeSeriesAttributes(chart, series2, 1, null);
//        att.setDashPattern(3);
//        att.setStrokeColor(Color.ORANGE);
//        attr.add(att);
    }

    @Override
    public void step(SimState simState) {
        double time = simState.schedule.getTime() / 100;
//        if (simState.schedule.getTime() == 50.0) {
//            TimeSeriesAttributes at = attr.get(0);
//            at.setSeriesIndex(0);
//            at.rebuildGraphicsDefinitions();
//        }
        for (int i = 0; i < seriesList.size(); i++) {
            XYSeries s = seriesList.get(i);
            s.add(time, Math.sin(2*time + i*Math.PI/seriesList.size()), true);
        }
        chart.updateChartLater(simState.schedule.getSteps());
    }
}
