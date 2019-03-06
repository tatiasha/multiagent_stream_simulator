package simulator;

import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by mikhail on 03.05.2017.
 */
class NodeAgentPortrayal extends SimplePortrayal2D {
    private final Color color;
    private final String label;

    private final LabelledPortrayal2D labelPort;
    private final RectanglePortrayal2D rectPort;

    public NodeAgentPortrayal(Color color, String label) {
        this.color = color;
        this.label = label;
        rectPort = new RectanglePortrayal2D(color, false);
        rectPort.scale=0.8;


        labelPort = new LabelledPortrayal2D(rectPort, label);
        labelPort.offsety=-15.0;
        labelPort.font = new Font(Font.DIALOG, Font.BOLD, 30);
        labelPort.align = LabelledPortrayal2D.ALIGN_CENTER;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        graphics.setStroke(new BasicStroke(6));
        labelPort.draw(object, graphics, info);
        graphics.setStroke(new BasicStroke(4));
    }

    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
        return labelPort.hitObject(object, range);
    }

    @Override
    public boolean handleMouseEvent(GUIState guistate, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type) {
        synchronized(guistate.state.schedule)
        {
            if (type == TYPE_HIT_OBJECT && event.getID() == MouseEvent.MOUSE_CLICKED) {
                System.out.println("Mouse clicked");
                NodeAgent agent = (NodeAgent) wrapper.getObject();
                StreamStateWithUI streamgui = (StreamStateWithUI) guistate;
                streamgui.runNodeChart(agent);
                return true;
            }
            return false;
        }
    }
}
