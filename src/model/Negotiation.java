package model;


import model.agents.Agent;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class that models the logic of a negotiation between agents
 *
 * @author Sanne Berends
 * @date 2024
 */
@SuppressWarnings("DuplicatedCode")
public class Negotiation {
    private final File file;
    private static final int MAX_N_ROUNDS = 50;

    private final ArrayList<Agent> N;
    private final ArrayList<ArrayList<Integer>> D_0;
    private ArrayList<ArrayList<Double>> bestEV;
    private final ArrayList<ArrayList<ArrayList<Integer>>> bestOffer;
    private int negotiateRound;
    private int finalDecision; // -1: too long, 0: 0 withdrew, 1: 1 withdrew, 2: 0 accepted, 3: 1 accepted.


    /**
     * The default constructor that creates the negotiation
     * @param i One of the agents of this negotiation (makes initial offer)
     * @param j The other agent of this negotiation
     */
    public Negotiation(Agent i, Agent j, File file) {
        this.file = file;
        N = new ArrayList<>(); //Set of agents
        D_0 = new ArrayList<>(); //Initial resources
        bestEV = new ArrayList<>(); //BestEV according to each agent, for each order of ToM
        bestOffer = new ArrayList<>();//Best Offer according to each agent
        negotiateRound = 0;

        initializeVariables(i,j);

    }


    /*
    Methods
     */

    /**
     * A method that handles the initialization of the variables of the negotiation
     * @param i One of the agents of this negotiation (makes initial offer)
     * @param j The other agent of this negotiation
     */
    private void initializeVariables(Agent i, Agent j) {
        i.initializeBeliefs();
        j.initializeBeliefs();
        if (i.getOrder() > 1) {
            i.setB2();
        }
        if (j.getOrder() > 1) {
            j.setB2();
        }
        N.add(i);
        N.add(j);
        bestEV.add(new ArrayList<>(Arrays.asList(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE)));
        bestEV.add(new ArrayList<>(Arrays.asList(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE)));
        bestOffer.add(new ArrayList<>(Arrays.asList(null, null, null)));
        bestOffer.add(new ArrayList<>(Arrays.asList(null, null, null)));

        D_0.add(i.getResourcesList());
        D_0.add(j.getResourcesList());
    }

    /**
     * A method that models a round of the negotiation
     */
    public void newRound() {
        negotiateRound++;
        if (negotiateRound == 1) { //Initial offer
            startFirstRound();
            bestEV = new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE)),
                    new ArrayList<>(Arrays.asList(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE))));
            return;
        }
        if (negotiateRound >= MAX_N_ROUNDS) { //Final round reached
            finalDecision = -1;
            stopNegotiation();
            return;
        }

        determineMove();
        bestEV = new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE)),
                new ArrayList<>(Arrays.asList(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE))));
    }

    /**
     * A method that starts the first round of the negotiation, so it calls the initialOffer function
     * from the agents whose turn it is, and checks if that agent immediately withdraws
     */
    private void startFirstRound() {
        Agent agent = N.get(whoseTurn());
        Agent otherAgent = N.get(opponentsTurn());
        agent.makeInitialOffer(whoseTurn(),agent.getProducingResource());
        if (bestOffer.get(whoseTurn()).get(agent.getOrder()) == null) {//Withdraw immediately
            finalDecision = whoseTurn(); //0 if 0 withdraw, 1 if 1 withdrew
            stopNegotiation();
        } else {
            //Other agent receives this offer
            manageBeliefsNewOffer(agent,otherAgent);
        }
    }

    /**
     * A method that handles the choice of the agent whose turn it is. The options are
     * to counteroffer, accept or withdraw
     */
    private void determineMove() {
        Agent agent = N.get(whoseTurn());
        Agent otherAgent = N.get(opponentsTurn());
        int choice;
        choice = agent.negotiateTurn(whoseTurn(),bestOffer.get(opponentsTurn()).get(otherAgent.getOrder()),
                agent.getProducingResource(),agent.getOrder(), agent.getBeliefs(),false);

        switch (choice) {
            case 0: //Counteroffer
                counterOffer(agent, otherAgent);
                break;
            case 1: //Accept
                acceptOffer(agent, otherAgent);
                break;
            case 2: //Withdraw
                withdraw(agent, otherAgent);
                break;
        }
    }

    /**
     * A method that manages the logic of a counterOffer
     * @param agent         The agent that made the counteroffer
     * @param otherAgent    The trading partner
     */
    private void counterOffer(Agent agent, Agent otherAgent) {
        //The (previous) offer of the other agent was rejected
        ArrayList<Integer> previousOffer = bestOffer.get(opponentsTurn()).get(otherAgent.getOrder());
        manageBeliefsRejection(agent, otherAgent, previousOffer);

        //This agent knows that other agent gets rejection info
        if (agent.getOrder()>0) {
            agent.updateBeliefs(otherAgent, bestOffer.get(opponentsTurn()).get(otherAgent.getOrder()), agent.getB1(),0, 1);
        }
        manageBeliefsNewOffer(agent, otherAgent);

    }

    private void manageBeliefsRejection(Agent agent, Agent otherAgent, ArrayList<Integer> previousOffer) {
        otherAgent.updateAcceptanceStatistics(previousOffer, 0);
        otherAgent.updateBeliefs(agent,previousOffer, otherAgent.getB0(),0,0);
    }

    /**
     * A method that manages beliefs and location beliefs (p) when an offer is received by an agent
     * @param agent         The agent that made the new offer
     * @param otherAgent    The other agent
     */
    private void manageBeliefsNewOffer(Agent agent, Agent otherAgent) {
        ArrayList<Integer> offerToOtherAgent = bestOffer.get(whoseTurn()).get(agent.getOrder());


        //Other agent receives this (new) offer
        //System.out.println("update beliefs of other agent now");
        otherAgent.updateBeliefs(agent, mirrorOffer(offerToOtherAgent),otherAgent.getB0(),1,0);
        if (otherAgent.getOrder()>0) {
            otherAgent.updatePBeliefs(offerToOtherAgent, 1, agent, otherAgent.getP1(), whoseTurn(), otherAgent.getB1());
            otherAgent.updateConfidence(mirrorOffer(offerToOtherAgent),whoseTurn(),otherAgent.getP1(),1,otherAgent.getB1());
        }
        if (otherAgent.getOrder()>1) {
            otherAgent.updateConfidence(mirrorOffer(offerToOtherAgent),whoseTurn(),otherAgent.getP2(),2,otherAgent.getB0());
        }

        //This agent knows that other agent receives this (new) offer
        if (agent.getOrder()>0) {
            agent.updateBeliefs(otherAgent, offerToOtherAgent, agent.getB1(),1, 1);
        }
        if (agent.getOrder()>1) {
            agent.updatePBeliefs(mirrorOffer(offerToOtherAgent), 2, otherAgent, agent.getP2(), opponentsTurn(), agent.getB0()); //b0=b2
        }
    }

    /**
     * A method that manages the logic of the acceptance of an offer
     * @param agent         The agent that accepted
     * @param otherAgent    The trading partner
     */
    private void acceptOffer(Agent agent, Agent otherAgent) {
        ArrayList<Integer> previousOffer = bestOffer.get(opponentsTurn()).get(otherAgent.getOrder());

        otherAgent.updateAcceptanceStatistics(previousOffer,1);

        startTrade(previousOffer, agent, otherAgent);
        finalDecision = 2 + whoseTurn(); // 2 if 0 accepted, 3 if 1 accepted
        stopNegotiation();
    }

    /**
     * A method that manages the logic of a withdrawal
     * @param agent         The agent that made withdraws
     * @param otherAgent    The trading partner
     */
    private void withdraw(Agent agent, Agent otherAgent) {
        ArrayList<Integer> previousOffer = bestOffer.get(opponentsTurn()).get(otherAgent.getOrder());

        //Offer from other agent was rejected
        manageBeliefsRejection(otherAgent, otherAgent, previousOffer);

        //This agent knows that other agent gets rejection info
        if (agent.getOrder()>0) {
            agent.updateBeliefs(otherAgent, previousOffer, agent.getB1(),0, 1);
        }
        finalDecision = whoseTurn(); //0 if 0 withdrew, 1 if 1 withdrew.
        stopNegotiation();
    }

    /**
     * A method that stops the negotiation
     */
    private void stopNegotiation() {
        ArrayList<Integer> type = new ArrayList<>(Arrays.asList(N.get(0).getOrder(),N.get(1).getOrder()));

        N.get(0).setNegotiating(false,negotiateRound,type, finalDecision);
        N.get(1).setNegotiating(false,negotiateRound,type, finalDecision);

    }

    /**
     * A method that starts the trade of resources when the offer is accepted
     * @param offerToSelf   The offer to the agent
     * @param agent         The agent whose turn it is
     * @param otherAgent    The trading partner of agent
     */
    private void startTrade(ArrayList<Integer> offerToSelf, Agent agent, Agent otherAgent) {
        ArrayList<Integer> type = new ArrayList<>(Arrays.asList(N.get(0).getOrder(),N.get(1).getOrder()));

        double gain0;
        double gain1;
        if (whoseTurn() == 0) {
            gain0 = agent.tradeResources(offerToSelf.get(0), offerToSelf.get(1), offerToSelf.get(2),offerToSelf.get(3), offerToSelf.get(4),
                    offerToSelf.get(5), offerToSelf.get(6), offerToSelf.get(7));
            gain1 = otherAgent.tradeResources(offerToSelf.get(4), offerToSelf.get(5), offerToSelf.get(6), offerToSelf.get(7),
                    offerToSelf.get(0), offerToSelf.get(1), offerToSelf.get(2), offerToSelf.get(3));
        } else {
            gain1 = agent.tradeResources(offerToSelf.get(0), offerToSelf.get(1), offerToSelf.get(2),offerToSelf.get(3), offerToSelf.get(4),
                    offerToSelf.get(5), offerToSelf.get(6), offerToSelf.get(7));
            gain0 = otherAgent.tradeResources(offerToSelf.get(4), offerToSelf.get(5), offerToSelf.get(6), offerToSelf.get(7),
                    offerToSelf.get(0), offerToSelf.get(1), offerToSelf.get(2), offerToSelf.get(3));
        }
        file.addToPiGains(type, gain0, gain1);
    }

    /**
     * A method that mirrors the offer O as if it was made by the other agent than it was
     * @param O             The original offer
     * @return mirroredO    The mirrored offer
     */
    public ArrayList<Integer> mirrorOffer (ArrayList<Integer> O) {
        ArrayList<Integer> mirroredO = new ArrayList<>();
        mirroredO.add(O.get(4));
        mirroredO.add(O.get(5));
        mirroredO.add(O.get(6));
        mirroredO.add(O.get(7));
        mirroredO.add(O.get(0));
        mirroredO.add(O.get(1));
        mirroredO.add(O.get(2));
        mirroredO.add(O.get(3));
        return mirroredO;
    }

    /**
     * A method that returns whose turn it is
     * @return 0 or 1
     */
    public int whoseTurn() {
        if ((negotiateRound -1) % 2 == 0) {
            return 0; //Agent i
        } else {
            return 1; //Agent j
        }
    }

    /**
     * A method that gives the index of the opponent of the person whose turn it is
     * @return 0 or 1
     */
    public int opponentsTurn() {
        if ((negotiateRound) % 2 == 0) {
            return 0; //Agent i
        } else {
            return 1; //Agent j
        }
    }

    // Getters

    /**
     * A method that returns D_0
     * @return D_0  The 2D matrix with initial resources
     */
    public ArrayList<ArrayList<Integer>> getD_0() {
        return D_0;
    }

    /**
     * A method that returns Agent i
     * @param i     Integer that indicates which agent to access
     * @return      Agent i
     */
    public Agent getAgent(int i) {
        return N.get(i);
    }

    /**
     * A method that returns the bestEV of Agent i
     *
     * @param order The order of ToM currently used
     * @return bestEV   The bestEV according to i
     */
    public double getBestEV(int order) {
        return bestEV.get(whoseTurn()).get(order);
    }

    public ArrayList<Integer> getBestOffer(int order) {
        return bestOffer.get(whoseTurn()).get(order);
    }


    // Setters

    /**
     * A method that sets the bestOffer of Agent i
     * @param bestOffer The new bestOffer according to i
     * @param order     The order of ToM currently used
     */
    public void setBestOffer(ArrayList<Integer> bestOffer, int order) {
        this.bestOffer.get(whoseTurn()).set(order,bestOffer);
    }

    /**
     * A method that sets the bestEV of Agent i
     *
     * @param bestEV The new bestEV according to i
     * @param order  The order of ToM currently used
     */
    public void setBestEV(double bestEV, int order) {
        this.bestEV.get(whoseTurn()).set(order,bestEV);
    }
}
