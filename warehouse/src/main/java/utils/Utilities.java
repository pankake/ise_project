package utils;

public class Utilities {

    public enum AgentsIds {
        ROBOT(0),
        ROBOT1(1);

        private final int id;

        AgentsIds(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static AgentsIds getTypeById(int id) {
            for (AgentsIds type : AgentsIds.values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Nessun tipo di agente corrispondente all'ID: " + id);
        }

        public boolean hasSameType(AgentType agentType) {
            return this.name().equals(agentType.getValue().toUpperCase());
        }

        public static boolean isRobotOrRobot1(int id) {
            AgentsIds type = AgentsIds.getTypeById(id);
            return type == AgentsIds.ROBOT || type == AgentsIds.ROBOT1;
        }
    }

    public enum AgentType {
        ROBOT("robot"),
        ROBOT1("robot1"),
        DELIVERY("delivery"),
        TRUCK("truck");

        private final String value;

        AgentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum LocationType {
        ROBOT("Robot"),
        ROBOT1("Robot1"),
        DELIVERY("Delivery"),
        WAITINGZONE("WaitingZone"),
        WAITINGZONE1("WaitingZone1"),
        BROKEN("Broken"),
        RACK1("Rack1"), RACK2("Rack2"), RACK3("Rack3");

        private final String value;

        LocationType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
