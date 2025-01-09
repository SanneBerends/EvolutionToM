package model.agents;

import model.File;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A class that models an ToM1 agent.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class ToM1Agent extends Agent {
    private static final int ORDER = 1;


    /**
     * The constructor that creates a ToM1-agent, which gets resources and a location
     * @param index The unique integer of this agent
     * @param file  The file in which to place the agent
     */
    public ToM1Agent(int index, File file) {
        super(index,file);
        super.order = ORDER;
    }


    /*
    Methods
     */


    /**
     * A method that returns the beliefs of this agent
     * @return b1
     */
    @Override
    public HashMap<ArrayList<Integer>, Double> getBeliefs() {
        return b1;
    }
}
