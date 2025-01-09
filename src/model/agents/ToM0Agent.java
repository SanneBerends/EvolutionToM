package model.agents;

import model.File;

/**
 * A class that models an ToM0 agent.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class ToM0Agent extends Agent {
    private static final int ORDER = 0;

    /**
     * The constructor that creates a ToM0-agent, which gets resources and a location
     * @param index The unique integer of this agent
     * @param file  The file in which to place the agent
     */
    public ToM0Agent(int index, File file) {
        super(index,file);
        super.order = ORDER;
    }

}
