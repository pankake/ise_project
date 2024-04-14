package env;

import jason.NoValueException;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import utils.Utilities;

import java.util.logging.Logger;

/**
 * Any Jason environment "entry point" should extend
 * jason.environment.Environment class to override methods init(),
 * updatePercepts() and executeAction().
 */

//ambiente in cui gli agenti interagiscono
public class WarehouseEnv extends Environment {

    // azioni
    public static final Literal access_rack = Literal.parseLiteral("access(rack)");
    public static final Literal free_rack = Literal.parseLiteral("free(rack)");
    public static final Literal unload_goods = Literal.parseLiteral("unload(goods)");
    public static final Literal eg = Literal.parseLiteral("end(goods)");

    static Logger logger = Logger.getLogger(WarehouseEnv.class.getName());

    WarehouseModel model;

    @Override
    public void init(final String[] args) {
        this.model = new WarehouseModel();
        if ((args.length == 1) && args[0].equals("gui")) {
            final WarehouseView view = new WarehouseView(this.model);
            this.model.setView(view);
        }
        this.updatePercepts(Utilities.AgentType.ROBOT.getValue());
        this.updatePercepts(Utilities.AgentType.ROBOT1.getValue());
    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (WarehouseModel)
     */

    void updatePercepts(String ag) {

        this.clearPercepts(ag);
        this.clearPercepts(Utilities.AgentType.DELIVERY.getValue());

        // gets the location of the robot agent from the model
        Location lRobot = new Location(0, 0);
        Utilities.AgentType robotType = Utilities.AgentType.valueOf(ag.toUpperCase());

        // finished the goods! activates the @waitfor condition
        if(this.model.goodsNum <= 0) {
            this.addPercept(Utilities.AgentType.DELIVERY.getValue(), WarehouseEnv.eg);
        }

        // random robot breakdown
        if(this.model.brokenAg != null) {

            // the working robot goes to recover the broken one
            String rescuer = this.model.brokenAg.equals(Utilities.AgentType.ROBOT)
                    ? Utilities.AgentType.ROBOT1.getValue()
                    : Utilities.AgentType.ROBOT.getValue();

            // the agent perceives that he is broken
            this.addPercept(this.model.brokenAg.getValue(), Literal.parseLiteral("broken(" +
                    rescuer+")"));

            // the repair work can begin
            if(this.model.reached) {
                this.addPercept(rescuer, Literal.parseLiteral("reach_robot(" +
                        this.model.brokenAg.getValue()+")"));
                this.addPercept(this.model.brokenAg.getValue(), Literal.parseLiteral("fault_resolved(" +
                        rescuer+")"));
            }
        }

        switch (robotType) {
            case ROBOT:
                lRobot = this.model.getAgPos(Utilities.AgentsIds.ROBOT.getId());
                break;

            case ROBOT1:
                lRobot = this.model.getAgPos(Utilities.AgentsIds.ROBOT1.getId());
                break;
        }

        this.addPercept(
                Utilities.AgentType.DELIVERY.getValue(),
                Literal.parseLiteral("stock(goods,"
                        + this.model.goodsNum + ")"));

        // robot si trova al rack
        if (lRobot.equals(this.model.lRack1)) {
            this.addPercept(ag, Literal.parseLiteral("at("+ag+", 1)"));

        } else if (lRobot.equals(this.model.lRack2)) {
            this.addPercept(ag, Literal.parseLiteral("at("+ag+", 2)"));

        } else if (lRobot.equals(this.model.lRack3)) {
            this.addPercept(ag, Literal.parseLiteral("at("+ag+", 3)"));
        }

        if (lRobot.equals(this.model.lDelivery)) {
            this.addPercept(ag, Literal.parseLiteral("at("+ag+", "+ Utilities.AgentType.DELIVERY.getValue()+")"));
        }

        if (lRobot.equals(this.model.lWaitingZone) || lRobot.equals(this.model.lWaitingZone1)) {
            this.addPercept(ag, Literal.parseLiteral("at("+ag+", waitingZone)"));
        }
    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */

    @Override
    public boolean executeAction(final String ag, final Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        boolean result = false;

        if (action.equals(WarehouseEnv.access_rack)) { // of = access(rack)
            result = this.model.accessRack();
        } else if (action.equals(WarehouseEnv.free_rack)) { // clf = free(rack)
            result = this.model.freeRack();
        }

        // if the action is a movement it extracts the correct destination
        // and call the movement method
        else if (action.getFunctor().equals("move_towards") && !ag.equals(Utilities.AgentType.TRUCK.getValue())) {
            final String l = action.getTerm(0).toString();

            Location dest = l.matches("[1-3]") ? getRackLocation(l) : getLocation(l, ag);

            int agentNumber = Utilities.AgentsIds.valueOf(ag.toUpperCase()).getId();
            result = this.model.moveTowards(dest, agentNumber);


        } else if (action.getFunctor().equals("place")) {

            try {
                int index = (int) ((NumberTerm) action
                        .getTerm(0)).solve();
                result = this.model.placeGoods(index - 1);
            } catch (NoValueException e) {
                e.printStackTrace();
            }

        } else if (action.equals(WarehouseEnv.unload_goods)) {
            result = this.model.unloadGoods();
        }

        // truck delivers the goods
        else if (action.getFunctor().equals("deliver")) {
            // simulate delivery time
            try {
                Thread.sleep(1500);

                result = this.model.deliverGoods((int) ((NumberTerm) action
                        .getTerm(1)).solve());

            } catch (final Exception e) {
                logger.info("Failed to execute action deliver!" + e);
            }
        }

        else if (action.getFunctor().equals("charge")) {

            try {
                int level = (int) ((NumberTerm) action
                        .getTerm(0)).solve();
                result = this.model.chargeRobot(ag, level);
            } catch (NoValueException e) {
                e.printStackTrace();
            }
        }
        else if(action.getFunctor().equals("catch")) {
            result = this.model.catchAgent();
        }

        else if (action.getFunctor().equals("restore")) {
            result = this.model.restoreVariables();
        }
        else if (action.getFunctor().equals("show_broken")) {
            result = this.model.showBrokenAg();
        }

        else if (action.getFunctor().equals("show_recovery")) {
            result = this.model.showRecoveryAg(ag);
        }

        else {
            WarehouseEnv.logger.info("Failed to execute action " + action);
        }

        //if the action was successful, it updates the agents' perceptions
        if (result) {
                this.updatePercepts(ag);
            try {
                Thread.sleep(100);
            } catch (final Exception e) { }
        }
        return result;
    }

    private Location getLocation(String location, String ag) {

        Utilities.LocationType locationType = Utilities.LocationType.valueOf(location.toUpperCase());

        Location dest = null;
        switch (locationType) {
            case ROBOT:
            case ROBOT1:
                dest = this.model.getAgPos(location.equals(Utilities.AgentType.ROBOT.getValue())
                        ? Utilities.AgentsIds.ROBOT.getId()
                        : Utilities.AgentsIds.ROBOT1.getId());
                break;
            case DELIVERY:
                dest = this.model.lDelivery;
                break;
            case WAITINGZONE:
                dest = ag.equals(Utilities.AgentType.ROBOT1.getValue()) ?
                        this.model.lWaitingZone1 : this.model.lWaitingZone;
                break;
            case BROKEN:
                int robot1X = this.model.rnd.nextInt(WarehouseModel.GSize - 3);
                int robot0X = this.model.rnd.nextInt(WarehouseModel.GSize - 3) + 3;
                dest = ag.equals(Utilities.AgentType.ROBOT1.getValue())
                        ? new Location(robot1X, this.model.rnd.nextInt(WarehouseModel.GSize - 2) + 1)
                        : new Location(robot0X, this.model.rnd.nextInt(WarehouseModel.GSize - 2) + 1);
                break;
            default:
                logger.info("ERROR: not recognized statement");
                break;
        }
        return dest;
    }

    private Location getRackLocation(String location) {
        Location dest = null;
        switch (location) {
            case "1":
                dest = this.model.lRack1;
                break;
            case "2":
                dest = this.model.lRack2;
                break;
            case "3":
                dest = this.model.lRack3;
                break;
        }
        return dest;
    }
}
