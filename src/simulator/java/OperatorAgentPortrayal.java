import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by mikhail on 02.05.2017.
 */
public class OperatorAgentPortrayal extends SimplePortrayal2D {

    public Color color;
    public String label;

    public LabelledPortrayal2D labelPort;
    public OvalPortrayal2D ovalPort;
    public OvalPortrayal2D outOvalPort;

    public OperatorAgentPortrayal(Color color, String label) {
        this.color = color;
        this.label = label;
        ovalPort = new OvalPortrayal2D(color);
        labelPort = new LabelledPortrayal2D(ovalPort, label);
        labelPort.offsety=-15.0;
        labelPort.align = LabelledPortrayal2D.ALIGN_CENTER;
        Font font = new Font(Font.DIALOG, Font.BOLD, 26);
        labelPort.font = font;

        Color outColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
        outOvalPort = new OvalPortrayal2D(outColor, false);
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        SinusoidalOperatorAgent agent = (SinusoidalOperatorAgent) object;
        ovalPort.filled = agent.isExecuting;
        labelPort.draw(object, graphics, info);
        outOvalPort.draw(object,graphics, info);

    }

    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
        return labelPort.hitObject(object, range);
    }

    @Override
    public boolean handleMouseEvent(GUIState guistate, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type) {
        synchronized(guistate.state.schedule)
        {
            if (type == TYPE_HIT_OBJECT && event.getID() == event.MOUSE_CLICKED) {
                SinusoidalOperatorAgent agent = (SinusoidalOperatorAgent) wrapper.getObject();
                StreamStateWithUI streamgui = (StreamStateWithUI) guistate;
                streamgui.runOperatorChart(agent);
                return true;
            }
            return false;
        }
    }
}
