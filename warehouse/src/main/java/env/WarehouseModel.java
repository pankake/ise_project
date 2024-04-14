package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import utils.Utilities;

import java.util.*;

import static utils.Utilities.AgentType.ROBOT;
import static utils.Utilities.AgentType.ROBOT1;

/**
 * Jason provides a convenient GridWorldModel class representing the model of a
 * square environment consisting of a grid of tiles. Less conveniently, the
 * Javadoc is almost useless thus you should figure out by yourself (e.g. by
 * looking at comments in examples source code) how the things work.
 */
public class WarehouseModel extends GridWorldModel {
    public static final int RACK_1 = 16;
    public static final int RACK_2 = 128;
    public static final int RACK_3 = 64;
    public static final int DELIVERY_POINT = 32;
    // the grid size
    public static final int GSize = 7;
    // number of available goods
    int goodsNum = 9;
    int[] placed = {0,0,0};
    Map<Utilities.AgentType, Integer> chargeLevel = new HashMap<>();
    boolean reached = false;
    boolean catching = false;
    Utilities.AgentType brokenAg = null;
    Utilities.AgentType showBrokenAg = null;
    public final Random rnd = new Random();
    private static final int DELAY = 15000;
    private static final int PERIOD = 50000;

    // environment objects location
    Location lRack1 = new Location(0, WarehouseModel.GSize - 1);
    Location lRack2 = new Location(WarehouseModel.GSize / 2, WarehouseModel.GSize - 1);
    Location lRack3 = new Location(WarehouseModel.GSize - 1, WarehouseModel.GSize - 1);
    Location lDelivery = new Location(WarehouseModel.GSize / 2, 0);
    Location lWaitingZone = new Location(4, WarehouseModel.GSize - 4);
    Location lWaitingZone1 = new Location(2, WarehouseModel.GSize - 4);

    public WarehouseModel() {

        super(WarehouseModel.GSize, WarehouseModel.GSize, 2);

        // initial position of the agents
        this.setAgPos(Utilities.AgentsIds.ROBOT.getId(), lWaitingZone);
        this.setAgPos(Utilities.AgentsIds.ROBOT1.getId(), lWaitingZone1);

        // location of rack and delivery
        this.add(WarehouseModel.RACK_1, this.lRack1);
        this.add(WarehouseModel.RACK_2, this.lRack2);
        this.add(WarehouseModel.RACK_3, this.lRack3);
        this.add(WarehouseModel.DELIVERY_POINT, this.lDelivery);

        this.chargeLevel.put(ROBOT, -1);
        this.chargeLevel.put(ROBOT1, -1);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                robotBreakdown();
            }
        }, DELAY, PERIOD);
    }

    /*
     * All the following methods are invoked by the environment controller
     * so as to model changes in the environment, either spontaneous or due to agents
     * interaction. As such, they first check actions pre-conditions, then carry out
     * actions post-conditions.
     */

    boolean accessRack() {
        System.out.println("Access the rack");
        return true;
    }

    boolean freeRack() {

        System.out.println("Free the rack");
        return true;
    }

    boolean moveTowards(final Location dest, final int agentId) {
        Location r1 = this.getAgPos(agentId);

        // compute where to move
        moveTowardsDestination(dest, r1);

        //if delivery or the rack is busy it does not try to avoid the obstacle
        if ((isOccupied(r1, agentId))
                && (r1.equals(lRack1) || r1.equals(lRack2) || r1.equals(lRack3)
                || r1.equals(lDelivery))) {
            return true;
        }

        // try to reach the new position
        else if (moveIfNotOccupied(agentId, r1)) return true;

        // avoid the obstacle
        else {

            r1 = moveAroundTheObstacle(dest, agentId);
            if (moveIfNotOccupied(agentId, r1)) return true;
        }

        return true;
    }

    private synchronized boolean moveIfNotOccupied(int agentId, Location r1) {
        if (!isOccupied(r1, agentId)) {

            // updates the agent's coordinates by moving it
            this.setAgPos(agentId, r1);

            // repaint rack and delivery to update colors
            if (this.view != null) {
                this.view.update(this.lRack1.x, this.lRack1.y);
                this.view.update(this.lRack2.x, this.lRack2.y);
                this.view.update(this.lRack3.x, this.lRack3.y);
                this.view.update(this.lDelivery.x, this.lDelivery.y);
            }
            return true;
        }
        return false;
    }

    private Location moveAroundTheObstacle(Location dest, int agentId) {
        Location r1;
        r1 = this.getAgPos(agentId);

        boolean randomDirection = rnd.nextBoolean();
        int newx = 0;
        int newy = 0;

        // the destination is below
        if(r1.y < dest.y) {
            // can only move downwards
            newy = r1.y + 1;

            //same x: can try to move left or right
            if(r1.x == dest.x) {
                newx = randomDirection ? r1.x + 1 : r1.x - 1;
            }
            if(r1.x < dest.x) {
                newx = r1.x + (randomDirection ? 1 : 0);
            }
            if(r1.x > dest.x) {
                newx = r1.x - (randomDirection ? 1 : 0);
            }
        }

        // the destination is above
        if(r1.y > dest.y) {
            // can only move upwards
            newy = r1.y - 1;

            // same x: can try moving left or right
            if(r1.x == dest.x) {
                newx = randomDirection ? r1.x + 1 : r1.x - 1;
            }
            if(r1.x < dest.x) {
                newx = r1.x + (randomDirection ? 1 : 0);
            }
            if(r1.x > dest.x) {
                newx = r1.x - (randomDirection ? 1 : 0);
            }
        }

        // the destination is at the same height
        if(r1.y == dest.y) {

            //can go up or down
            newy = randomDirection ? r1.y + 1 : r1.y - 1;

            // dest to my right
            if(r1.x < dest.x) {
                newx = r1.x + (randomDirection ? 1 : 0);
            }

            //dest to my left
            if(r1.x > dest.x) {
                newx = r1.x - (randomDirection ? 1 : 0);
            }
            // dest with my same x
            if(r1.x == dest.x) {
                newx = randomDirection ? r1.x + 1 : r1.x - 1;
            }
        }

        r1.x = validateCoords(r1.x, newx);
        r1.y = validateCoords(r1.y, newy);
        return r1;
    }

    private void moveTowardsDestination(Location dest, Location r1) {
        if (r1.x < dest.x) {
            r1.x++;
        } else if (r1.x > dest.x) {
            r1.x--;
        }
        if (r1.y < dest.y) {
            r1.y++;
        } else if (r1.y > dest.y) {
            r1.y--;
        }
    }

    public int validateCoords(int currentCoord, int newCoord) {
        return newCoord < 0 || newCoord > WarehouseModel.GSize - 1 ? currentCoord : newCoord;
    }

    // checking whether a position is occupied by an agent other than the specified one
    private boolean isOccupied(Location loc, int agentId) {
        for (int i = 0; i < this.getNbOfAgs(); i++) {
            if (i != agentId && this.getAgPos(i).equals(loc)) {
                if(this.catching && Utilities.AgentsIds.isRobotOrRobot1(agentId))
                    this.reached = true;
                return true; // the position is occupied by another agent
            }
        }
        return false; // the position is free
    }

    boolean placeGoods(int index) {
        this.placed[index]++;
        if (this.view != null) {
            switch (index) {
                case 0: this.view.update(this.lRack1.x, this.lRack1.y); break;
                case 1: this.view.update(this.lRack2.x, this.lRack2.y); break;
                case 2: this.view.update(this.lRack3.x, this.lRack3.y); break;
            }
        }
        return true;
    }

    boolean deliverGoods(final int n) {
        this.goodsNum += n;
        if (this.view != null) {
            this.view.update(this.lDelivery.x, this.lDelivery.y);
        }
        return true;
    }

    boolean unloadGoods() {

        if(this.goodsNum > 0) {
            this.goodsNum--;
            if (this.view != null) {
                this.view.update(this.lDelivery.x, this.lDelivery.y);
            }
        }
        return true;
    }

    boolean chargeRobot(String agent, int level) {

        Utilities.AgentType robotType = Utilities.AgentType.valueOf(agent.toUpperCase());
        Location l = this.getAgPos(Utilities.AgentsIds.valueOf(agent.toUpperCase()).getId());

        if (this.view != null) {
            switch (robotType) {
                case ROBOT:
                    chargeLevel.put(ROBOT, level);
                    break;
                case ROBOT1:
                    chargeLevel.put(ROBOT1, level);
                    break;
            }
            this.view.update(l.x, l.y);
        }
        return true;
    }

    boolean catchAgent() {
        this.catching = true;
        return true;
    }

    boolean restoreVariables() {
        this.brokenAg = null;
        this.catching = false;
        this.reached = false;
        this.showBrokenAg = this.brokenAg;
        return true;
    }

    boolean showBrokenAg() {
        this.showBrokenAg = this.brokenAg;
        return true;
    }

    boolean showRecoveryAg(String ag) {
        Location pos = this.getAgPos(Utilities.AgentsIds.valueOf(ag.toUpperCase()).getId());
        updateView(pos.x, pos.y);
        return true;
    }

    public void updateView(int x, int y) {
        this.view.update(x, y);
    }

    private void robotBreakdown() {
        // Verifica se la mappa è vuota
        if (this.brokenAg == null) {

            int randomIndex = random.nextInt(2);
            Utilities.AgentType selectedRobot = randomIndex == 0 ?
                    Utilities.AgentType.ROBOT : Utilities.AgentType.ROBOT1;
            brokenAg = selectedRobot;
            System.out.println("Selected robot: " + selectedRobot);
        }
    }
}
