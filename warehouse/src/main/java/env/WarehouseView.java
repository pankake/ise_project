package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import utils.Utilities;

import java.awt.*;

/**
 * Jason provides a convenient GridWorldView class representing the view of a
 * square environment consisting of a grid of tiles. Less conveniently, the
 * Javadoc is almost useless thus you should figure out by yourself (e.g. by
 * looking at comments in examples source code) how the things work.
 */
public class WarehouseView extends GridWorldView {

    Color RED_ALERT = new Color (240, 80, 80);
    Color ULTRA_RED = new Color(252, 108, 133);
    Color SALOMON_PINK = new Color(252, 148, 161);
    Color LIGHT_RED = new Color(255, 204, 203);
    Color TEA_GREEN = new Color(205, 255, 204);
    Color MENTHAL = new Color(176, 245, 171);
    Color LIGHT_GREEN = new Color(144, 239, 144);

    private WarehouseModel wModel;

    public WarehouseView(final WarehouseModel model) {
        super(model, "Warehouse", 700);
        this.wModel = model;
        this.defaultFont = new Font("Verdana", Font.BOLD, 13);
        this.setVisible(true);
        this.repaint();
    }

    // draw objects on the grid
    @Override
    public void draw(final Graphics g, final int x, final int y,
            final int object) {
        final Location lRobot = this.wModel.getAgPos(0);
        final Location lRobot1 = this.wModel.getAgPos(1);
        super.drawAgent(g, x, y, Color.lightGray, -1);

        switch (object) {
            case WarehouseModel.RACK_1:
                if (lRobot.equals(this.wModel.lRack1) || lRobot1.equals(this.wModel.lRack1)) {
                    super.drawAgent(g, x, y, Color.yellow, -1);
                }
                g.setColor(Color.black);
                this.drawString(g, x, y, this.defaultFont, Utilities.LocationType.RACK1.getValue()
                        +"("+ this.wModel.placed[0] + ")");
                break;
            case WarehouseModel.RACK_2:
                if (lRobot.equals(this.wModel.lRack2) || lRobot1.equals(this.wModel.lRack2)) {
                    super.drawAgent(g, x, y, Color.yellow, -1);
                }
                g.setColor(Color.black);
                this.drawString(g, x, y, this.defaultFont, Utilities.LocationType.RACK2.getValue()
                        +"("+ this.wModel.placed[1] + ")");
                break;
            case WarehouseModel.RACK_3:
                if (lRobot.equals(this.wModel.lRack3) || lRobot1.equals(this.wModel.lRack3)) {
                    super.drawAgent(g, x, y, Color.yellow, -1);
                }
                g.setColor(Color.black);
                this.drawString(g, x, y, this.defaultFont, Utilities.LocationType.RACK3.getValue()
                        +"("+ this.wModel.placed[2] + ")");
                break;
            case WarehouseModel.DELIVERY_POINT:
                if (lRobot.equals(this.wModel.lDelivery) || lRobot1.equals(this.wModel.lDelivery)) {
                    super.drawAgent(g, x, y, Color.yellow, -1);
                }
                g.setColor(Color.black);
                this.drawString(g, x, y, this.defaultFont, Utilities.LocationType.DELIVERY.getValue()
                        + " (" + this.wModel.goodsNum + ")");
                break;
            default:
                break;
        }
    }

    @Override
    public void drawAgent(final Graphics g, final int x, final int y, Color c, final int id) {
        final Location lRobot = this.wModel.getAgPos(id);
        if (!lRobot.equals(this.wModel.lDelivery) && !lRobot.equals(this.wModel.lRack1)
                && !lRobot.equals(this.wModel.lRack2) && !lRobot.equals(this.wModel.lRack3)) {

            super.drawAgent(g, x, y, new Color(198, 233, 249), -1);
            g.setColor(Color.black);
            this.drawString(g, x, y, this.defaultFont, this.wModel.reached ? "Recovery" : "R"+id);

            if(this.wModel.showBrokenAg != null
                    && Utilities.AgentsIds.getTypeById(id).hasSameType(this.wModel.brokenAg)) {
                drawBrokenAgent(g, x, y);
                this.drawString(g, x, y, this.defaultFont, "Broken");
            }

            if(Utilities.AgentsIds.ROBOT.getId() == id && this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT) != -1) {
                drawAgentInCharge(g, x, y, this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT));
                this.drawString(g, x, y, this.defaultFont, "Refill ("+this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT)+"%)");
            } else if(Utilities.AgentsIds.ROBOT1.getId() == id && this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT1) != -1) {
                drawAgentInCharge(g, x, y, this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT1));
                this.drawString(g, x, y, this.defaultFont, "Refill ("+this.wModel.chargeLevel.get(Utilities.AgentType.ROBOT1)+"%)");
            }
        }
    }

    private void drawAgentInCharge(final Graphics g, final int x, final int y, final int level) {
        Color c = null;

        switch (level) {
            case 0: c = ULTRA_RED; break;
            case 20: c = SALOMON_PINK; break;
            case 40: c = LIGHT_RED; break;
            case 60: c = TEA_GREEN; break;
            case 80: c = MENTHAL; break;
            case 100: c = LIGHT_GREEN; break;
        }
        super.drawAgent(g, x, y, c, -1);
        g.setColor(Color.black);
    }

    private void drawBrokenAgent(final Graphics g, final int x, final int y) {

        Color c = RED_ALERT;
        super.drawAgent(g, x, y, c, -1);
        g.setColor(Color.black);
    }
}
