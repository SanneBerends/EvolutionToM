package model.agents;

import model.File;
import model.Negotiation;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;

/**
 * An abstract class that models an agent
 *
 * @author Sanne Berends
 * @date 2024
 */
public abstract class Agent {
    private static final int N_START_RESOURCES = 1;
    private static final int LOWER_BORDER = 0;
    private static final int UPPER_BORDER = 600;
    private static final int RESOURCE_THRESHOLD = 2;
    private static final int MAX_N_RESOURCES = 4;
    private static final double LEARNING_SPEED = 0.8; //0.2  0.5  0.8
    private static final int CHECK_INTERVAL = 2500;
    private static final double EPSILON = 0.000001;  //Small tolerance to handle floating-point imprecision of double arithmetic


    private final File file;
    private final int index;
    final int producingResource;
    int order;
    Negotiation negotiation;
    ArrayList<ArrayList<Double>> initialBeliefs;
    HashMap<ArrayList<Integer>, Double> b0;
    HashMap<ArrayList<Integer>, Double> b1;
    HashMap<ArrayList<Integer>, Double> b2;
    private ArrayList<Double> p1;
    private ArrayList<Double> p2;
    private double c1;
    private double c2;
    private int age;
    private double xLoc;
    private double yLoc;
    private int direction;
    private int resourceOne;
    private int resourceTwo;
    private int resourceThree;
    private int resourceFour;
    private boolean isNegotiating;
    private boolean shouldReduceResources;
    private boolean isDying;
    private boolean isUpdatingBelief;
    private int previousTradePartner;
    private ArrayList<ArrayList<Integer>> offersMade;
    private ArrayList<ArrayList<Integer>> offersAccepted;
    private ArrayList<Double> storeBestEV;
    private ArrayList<ArrayList<Integer>> storeBestOffer;
    private Map<ArrayList<Integer>, ArrayList<Integer>> guessedOffers;
    private Map<ArrayList<Integer>, Double> guessedEVs;
    private Map<Integer, ArrayList<ArrayList<Double>>> agentsInfo;
    private ArrayList<String> unsuccessfulHistory;





    /**
     * The default constructor that creates the agent
     * @param index     The unique integer of this agent
     * @param file      The file in which to place the agent
     */
    public Agent(int index, File file) {
        this.file = file;
        this.index = index;
        producingResource = ThreadLocalRandom.current().nextInt(0,4);
        initializeVariables();
    }

    /*
    Methods
     */

    /**
     * A method that initializes the variables of this agent
     */
    private void initializeVariables() {
        age = ThreadLocalRandom.current().nextInt(0,CHECK_INTERVAL);
        initializeLocation();
        direction = ThreadLocalRandom.current().nextInt(0,360);

        //Resources
        resetToInitialResources();

        //Negotiation
        isNegotiating = false;
        shouldReduceResources = false;
        isDying = false;
        negotiation = null;
        previousTradePartner = -1;

        guessedOffers = new HashMap<>();
        for (int a=0; a<3; a++) { //Orders
            for (int b=0; b<4;b++) {//Producing R
                guessedOffers.put(new ArrayList<>(Arrays.asList(a,b)), null);
            }
        }

        guessedEVs = new HashMap<>();
        for (int a=0; a<3; a++) { //Orders
            for (int b=0; b<4;b++) {//Producing R
                guessedEVs.put(new ArrayList<>(Arrays.asList(a,b)),-Double.MAX_VALUE);
            }
        }

        //Beliefs
        initialBeliefs = new ArrayList<>();
        initializeInitialBeliefs();
        isUpdatingBelief = false;
        storeBestEV = new ArrayList<>(Arrays.asList(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE));
        storeBestOffer = new ArrayList<>();
        storeBestOffer.add(new ArrayList<>());
        storeBestOffer.add(new ArrayList<>());
        storeBestOffer.add(new ArrayList<>());

        //Statistics
        offersMade = new ArrayList<>();
        offersAccepted = new ArrayList<>();
        initializePastExperiences(offersMade);
        initializePastExperiences(offersAccepted);
        agentsInfo = new HashMap<>();
        unsuccessfulHistory = new ArrayList<>();
    }

    /**
     * A method that initializes the location of the agent to a random location that is not occupied
     */
    private void initializeLocation() {
        xLoc = ThreadLocalRandom.current().nextInt(LOWER_BORDER,UPPER_BORDER);
        yLoc = ThreadLocalRandom.current().nextInt(LOWER_BORDER,UPPER_BORDER);

        double[] locations = file.checkInitialLocation(this,xLoc,yLoc);
        xLoc = locations[0];
        yLoc = locations[1];
    }

    /**
     * A method that initializes the (generalized) initial beliefs of the agent
     */
    private void initializeInitialBeliefs() {
        //Fill initialBeliefs with arrays of 1's
        for (int i = 0; i < 4 * MAX_N_RESOURCES; i++) {
            initialBeliefs.add(new ArrayList<>());
            for (int j = 0; j < 4 * MAX_N_RESOURCES; j++) {
                initialBeliefs.get(i).add(1.0);
            }
        }
    }

    /**
     * A method that initializes the necessary beliefs
     */
    public void initializeBeliefs() {
        switch(order) {
            case 2:
                b2 = new HashMap<>();
                p2 = new ArrayList<>(Arrays.asList(0.25,0.25,0.25,0.25));
                c2 = 1;
            case 1:
                b1 = new HashMap<>();
                p1 = new ArrayList<>(Arrays.asList(0.25,0.25,0.25,0.25));
                c1 = 1;
            case 0:
                b0 = new HashMap<>();
        }
    }

    /**
     * A method that initializes the list with 5's, representing that it has had 5
     * positive responses to all offers.
     * @param offers    List of either the offers that were made, or the offers that were accepted
     */
    private void initializePastExperiences(ArrayList<ArrayList<Integer>> offers) {
        for (int i = 0; i < 4 * MAX_N_RESOURCES; i++) {
            offers.add(new ArrayList<>());
            for (int j = 0; j < 4 * MAX_N_RESOURCES; j++) {
                offers.get(i).add(5);
            }
        }
    }

    /**
     * A method that specifies what happens when a time step is taken: a tick
     */
    public void step() {
        if (isNegotiating) {
            negotiate();
        } else {
            move();
        }
    }

    /**
     * A method that calls a new negotiation round
     */
    private void negotiate() {
        negotiation.newRound();
    }

    /**
     * A method that makes the agent move one step
     */
    private void move() {
        double xMove = cos(Math.toRadians(direction));
        double yMove = sin(Math.toRadians(direction));

        if (xLoc + xMove > (UPPER_BORDER-1) || xLoc + xMove < (LOWER_BORDER+1) || yLoc + yMove > (UPPER_BORDER-1) || yLoc + yMove < (LOWER_BORDER+1)) {
            //Running into wall, change direction randomly
            direction = ThreadLocalRandom.current().nextInt(0,360);
            while (xLoc + xMove > (UPPER_BORDER-1) || xLoc + xMove < (LOWER_BORDER+1) ||
                    yLoc + yMove > (UPPER_BORDER-1) || yLoc + yMove < (LOWER_BORDER+1)) {
                direction = ThreadLocalRandom.current().nextInt(0,360);
                xMove = cos(Math.toRadians(direction));
                yMove = sin(Math.toRadians(direction));
            }
        }
        checkMove(xMove, yMove);
    }

    /**
     * A method that checks if the move is possible (if there are other agents) and if
     * its state needs to be updated to a negotiating state.
     * @param xMove     The move in the x direction
     * @param yMove     The move in the y direction
     */
    private void checkMove(double xMove, double yMove) {
        //Check state of the new location: other agents around?
        switch (file.checkNewLocation(this,xLoc + xMove,yLoc + yMove)) {
            case 0: //There is another agent close to new location (no new negotiation)
                direction = ThreadLocalRandom.current().nextInt(0,360);
                break;
            case 1: //There is another agent close to new location: negotiate
                isNegotiating = true;
                break;
            case 2: //There is no agent around
                xLoc += xMove;
                yLoc += yMove;
                break;
        }
    }

    /**
     * A method that lets the agent make an initial offer
     * @param i         Integer that indicates whose turn it is
     * @param r         The producing resource of i
     */
    public void makeInitialOffer(int i, int r) {
        //Restore negotiation dependent info
        restoreSavedInfo();
        computeBestEV(i, r, getBeliefs(), order, false);


        double bestEV = negotiation.getBestEV(order);
        double piValue = computePiValue(getResourcesList(), producingResource);

        if (bestEV < piValue || Math.abs(bestEV - piValue) < EPSILON) {
            //Offer results in scenario worse than initial resources
            negotiation.setBestOffer(null, order);
            negotiation.setBestEV(-Double.MAX_VALUE, order);
        }
    }


    /**
     * A method that determines what the agent does during negotiation
     * @param i                 Integer that indicates whose turn it is
     * @param offerToSelf       The current offer, offered by other
     * @param r                 The producing resource of i
     * @param order             The order of ToM to be used
     * @param beliefs           The beliefs to be used
     * @param isAttributing     Boolean that indicates if the agent is attributing beliefs to trading agent
     * @return int              The decision of the agent
     */
    public int negotiateTurn(int i, ArrayList<Integer> offerToSelf, int r, int order, HashMap<ArrayList<Integer>, Double>  beliefs, boolean isAttributing) {
        if (this.order == order) {
            restoreSavedInfo();
        }
        if (betterOfferAvailable(i,offerToSelf,r,order, beliefs, isAttributing)) {
            return 0; //New offer
        } else if (isAcceptableOffer(i,offerToSelf,r, order)) {
            return 1; //Accept
        }
        return 2; //Withdraw
    }

    /**
     * A method that checks if there is a better offer available
     * @param i                 Integer that indicates whose turn it is
     * @param offerToSelf       The current offer, from POV of i
     * @param r                 The producing resource of i
     * @param order             The order of ToM to be used
     * @param beliefs           The beliefs to be used
     * @param isAttributing     Boolean that indicates if the agent is attributing beliefs to trading agent
     * @return boolean          True when it is the best offer
     */
    private boolean betterOfferAvailable(int i, ArrayList<Integer> offerToSelf, int r, int order, HashMap<ArrayList<Integer>, Double>  beliefs, boolean isAttributing) {
        if (guessedOffers.get(new ArrayList<>(Arrays.asList(order,r)))!=null) {
            if (isUpdatingBelief) {
                storeBestOffer.set(order,guessedOffers.get(new ArrayList<>(Arrays.asList(order,r))));
                storeBestEV.set(order,guessedEVs.get(new ArrayList<>(Arrays.asList(order,r))));
            } else {
                negotiation.setBestOffer(guessedOffers.get(new ArrayList<>(Arrays.asList(order,r))), order);
                negotiation.setBestEV(guessedEVs.get(new ArrayList<>(Arrays.asList(order,r))), order);
            }
        } else {
            computeBestEV(i, r, beliefs, order, isAttributing);
        }

        //Computing the new state, opponent made the offer
        ArrayList<Integer> afterOffer = afterOffer(mirrorOffer(offerToSelf),negotiation.getAgent(i).getResourcesList());

        if (!isUpdatingBelief) {
            double bestEV = negotiation.getBestEV(order);
            double piValueD0 = computePiValue(negotiation.getD_0().get(i), r);
            double piValueOfferItGot = computePiValue(afterOffer, r);

            //New offer is better than accepting/withdrawing
            return (bestEV > piValueD0 && Math.abs(bestEV - piValueD0) > EPSILON) &&
                    (bestEV > piValueOfferItGot && Math.abs(bestEV - piValueOfferItGot) > EPSILON);

        } else {
            double storeBestEVValue = storeBestEV.get(order);
            double piValueD0 = computePiValue(negotiation.getD_0().get(i), r);
            double piValueOffer = computePiValue(afterOffer, r);

            return (storeBestEVValue > piValueD0 && Math.abs(storeBestEVValue - piValueD0) > EPSILON) &&
                    (storeBestEVValue > piValueOffer && Math.abs(storeBestEVValue - piValueOffer) > EPSILON);
        }
    }

    /**
     * A method that checks if the offer is the best for this agent
     * @param i                 Integer that indicates whose turn it is
     * @param offerToSelf       The current offer, offered by other
     * @param r                 The producing resource of i
     * @param order             The order of ToM currently used
     * @return boolean          True when it is the best offer
     */
    private boolean isAcceptableOffer(int i, ArrayList<Integer> offerToSelf, int r, int order) {
        ArrayList<Integer> afterOffer = afterOffer(mirrorOffer(offerToSelf),negotiation.getAgent(i).getResourcesList() );

        double piValueAfterOffer = computePiValue(afterOffer, r);
        double piValueInitial = computePiValue(negotiation.getD_0().get(i), r);
        double bestEV = negotiation.getBestEV(order);
        double storedBestEV = storeBestEV.get(order);

        if (!isUpdatingBelief) {
            return (piValueAfterOffer > piValueInitial && Math.abs(piValueAfterOffer - piValueInitial) > EPSILON) &&
                    (piValueAfterOffer > bestEV && Math.abs(piValueAfterOffer - bestEV) > EPSILON);
        } else {
            return (piValueAfterOffer > piValueInitial && Math.abs(piValueAfterOffer - piValueInitial) > EPSILON) &&
                    (piValueAfterOffer > storedBestEV && Math.abs(piValueAfterOffer - storedBestEV) > EPSILON);
        }

    }

    /**
     * A method that computes the score of an offer by comparing it to the goal
     * [RESOURCE_THRESHOLD, RESOURCE_THRESHOLD, RESOURCE_THRESHOLD, RESOURCE_THRESHOLD]
     * @param resources     The list you want to assign a score
     * @param r             The producing resource of i
     * @return score        The score
     */
    double computePiValue(ArrayList<Integer> resources, int r) {
        int n;
        int score = 0;

        //Shortage weights heavier than surplus
        for (int j = 0; j < 4; j++) {
            n = resources.get(j);
            if (n < RESOURCE_THRESHOLD && j != r){ //Two points deduction for lack of resources
                score -= 2*(RESOURCE_THRESHOLD-n);
            } else if (n > RESOURCE_THRESHOLD && j != r) {//No points for self-producing resource
                score += (n-RESOURCE_THRESHOLD);
            }
            if (n > MAX_N_RESOURCES || n < 0) { //Resources cannot exceed capacity or be negative
                score = -Integer.MAX_VALUE;
            }
        }
        return score +12; //Map from [-12,6] to [0,18]
    }

    /**
     * A method that computes the highest Expected Value, using the specified ToM order
     * @param iIndex        Integer that indicates whose turn it is
     * @param r             The producing resource of i
     * @param beliefs       The beliefs used for the computation
     * @param order         The order of ToM to be used
     * @param isAttributing     Boolean that indicates if the agent is attributing beliefs to trading agent
     */
    protected void computeBestEV(int iIndex, int r, HashMap<ArrayList<Integer>, Double>  beliefs, int order, boolean isAttributing) {
        double bestEV = -Double.MAX_VALUE;
        double EV;
        ArrayList<Integer> bestOffer;

        Agent agent = negotiation.getAgent(iIndex);
        Agent otherAgent;
        if (iIndex == 0) {
            otherAgent = negotiation.getAgent(1);
        } else {
            otherAgent = negotiation.getAgent(0);
        }


        ArrayList<ArrayList< Integer>> offerOptions = new ArrayList<>();

        //Only offer your spare resources
        for (int a = 0; a <= min(MAX_N_RESOURCES-otherAgent.getResourceOne(),max(agent.getResourceOne() - RESOURCE_THRESHOLD, 0)); a++) {
            for (int b = 0; b <= min(MAX_N_RESOURCES-otherAgent.getResourceTwo(),max(agent.getResourceTwo() -RESOURCE_THRESHOLD, 0)); b++) {
                for (int c = 0; c <= min(MAX_N_RESOURCES-otherAgent.getResourceThree(),max(agent.getResourceThree()-RESOURCE_THRESHOLD, 0)); c++) {
                    for (int d = 0; d <= min(MAX_N_RESOURCES-otherAgent.getResourceFour(),max(agent.getResourceFour() - RESOURCE_THRESHOLD, 0)); d++) {
                        for (int e = 0; e <= min(otherAgent.getResourceOne(), (MAX_N_RESOURCES - agent.getResourceOne())); e++) {
                            for (int f = 0; f <= min(otherAgent.getResourceTwo(), (MAX_N_RESOURCES - agent.getResourceTwo())); f++) {
                                for (int g = 0; g <= min(otherAgent.getResourceThree(), (MAX_N_RESOURCES - agent.getResourceThree())); g++) {
                                    for (int h = 0; h <= min(otherAgent.getResourceFour(), (MAX_N_RESOURCES - agent.getResourceFour())); h++) {
                                        ArrayList<Integer> offerFromSelf = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h));
                                        //noinspection DuplicatedCode
                                        if (a+b+c+d+e+f+g+h == 0) { //All 0
                                            break;
                                        }
                                        if ((a>0 && e>0) || (b>0 && f>0) || (c>0 && g>0) || (d>0 && h>0) ) { //Interchanging same resources
                                            break;
                                        }
                                        EV = computeEV(offerFromSelf,iIndex,r,beliefs,order,isAttributing);

                                        if (EV > bestEV) { //The computed EV is best, so update
                                            bestEV = EV;

                                            offerOptions.clear();
                                            offerOptions.add(offerFromSelf);

                                        } else if (EV == bestEV) { //Found an offer that is equally good
                                            offerOptions.add(offerFromSelf);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (offerOptions.isEmpty()) {
            //No good offer found
            if (!isUpdatingBelief) {
                negotiation.setBestOffer(null, order);
                negotiation.setBestEV(-Double.MAX_VALUE, order);
            } else {
                storeBestEV.set(order, -Double.MAX_VALUE);
                storeBestOffer.set(order, null);
            }
            if (isAttributing) {
                guessedOffers.put(new ArrayList<>(Arrays.asList(order,r)), null);
                guessedEVs.put(new ArrayList<>(Arrays.asList(order,r)),-Double.MAX_VALUE);
            }

        } else {
            // Choose one of the offers with the highest EV
            if (offerOptions.size() == 1) {
                bestOffer = offerOptions.get(0);
            } else {
                bestOffer = offerOptions.get(ThreadLocalRandom.current().nextInt(0, offerOptions.size()));
            }

            if (!isUpdatingBelief) {
                negotiation.setBestOffer(bestOffer, order);
                negotiation.setBestEV(bestEV, order);

            } else {
                storeBestEV.set(order, bestEV);
                storeBestOffer.set(order, bestOffer);
            }
            if (isAttributing) {
                guessedOffers.put(new ArrayList<>(Arrays.asList(order,r)), bestOffer);
                guessedEVs.put(new ArrayList<>(Arrays.asList(order,r)),bestEV);
            }
        }

    }

    /**
     * A method that calls the correct EV computation method, depending on the order
     * of ToM to be used
     * @param offerFromSelf         The offer (from POV of this agent)
     * @param i                     Integer that indicates whose turn it is
     * @param r                     The producing resource of i
     * @param beliefs               The beliefs of the agent
     * @param order                 The order of ToM to be used
     * @param isAttributing     Boolean that indicates if the agent is attributing beliefs to trading agent
     * @return double               The computed EV
     */
    private double computeEV(ArrayList<Integer> offerFromSelf, int i, int r, HashMap<ArrayList<Integer>, Double>  beliefs, int order, boolean isAttributing) {
        switch (order) {
            case 0:
                return computeEVZero(offerFromSelf,i,r,beliefs);
            case 1:
                return computeEVOne(offerFromSelf,i,r,beliefs, isAttributing);
            case 2:
                return computeEVTwo(offerFromSelf,i,r,beliefs);
        }
        System.out.println("Error");
        return 0;
    }

    /**
     * A method that computes EV0
     * @param offerFromSelf         The offer
     * @param i                     Integer that indicates whose turn it is
     * @param r                     The producing resource of i
     * @param beliefs               The beliefs of the agent
     * @return The EV
     */
    double computeEVZero(ArrayList<Integer> offerFromSelf, int i, int r, HashMap<ArrayList<Integer>, Double>  beliefs) {
        ArrayList<Integer> D0_i = negotiation.getD_0().get(i);

        //Computing the new state, this agent made the offer
        ArrayList<Integer> afterOffer = afterOffer(offerFromSelf,negotiation.getAgent(i).getResourcesList());

        double belief;
        if (!beliefs.isEmpty() && beliefs.containsKey(offerFromSelf)) { //Previous info about this offer type
            belief = beliefs.get(offerFromSelf);
        } else { //Not encountered yet: use initial beliefs
            int give;
            int receive;
            give = offerFromSelf.get(0) + offerFromSelf.get(1) + offerFromSelf.get(2) + offerFromSelf.get(3);
            receive = offerFromSelf.get(4) + offerFromSelf.get(5) + offerFromSelf.get(6) + offerFromSelf.get(7);

            belief = initialBeliefs.get(give).get(receive);
        }

        double EVscore = belief * computePiValue(afterOffer,r) + (1-belief) * computePiValue(D0_i,r);
        //It cannot be higher than the components --> imprecision of floating-point arithmetic in Java
        EVscore = min(EVscore, max(computePiValue(afterOffer,r),computePiValue(D0_i,r)));

        return  EVscore;
    }

    /**
     * A method that computes EV1
     * @param offerFromSelf         The offer
     * @param i                     Integer that indicates whose turn it is
     * @param r                     The producing resource of i
     * @param beliefs               The beliefs used for computation
     * @param isAttributing         True if this agent is attributing
     * @return The EV1
     */
    double computeEVOne(ArrayList<Integer> offerFromSelf, int i, int r, HashMap<ArrayList<Integer>, Double>  beliefs, boolean isAttributing) {
        double tempC1 = c1;
        ArrayList<Double> tempP1 = p1;
        if (isAttributing) {
            tempC1 = 1.0;
            tempP1 = p2;
        }

        double zeroOrderEV;
        zeroOrderEV = (1-tempC1) * computeEVZero(offerFromSelf,i,r,b0);

        double firstOrderEV = 0;

        for (int rGuess = 0; rGuess < 4; rGuess++) {
            firstOrderEV  += tempP1.get(rGuess) * computeDirectEV(i,r,rGuess,offerFromSelf,beliefs,0);
        }
        return zeroOrderEV + tempC1 * firstOrderEV;
    }

    /**
     * A method that computes EV2
     * @param offerFromSelf         The offer
     * @param i                     Integer that indicates whose turn it is
     * @param r                     The producing resource of i
     * @param beliefs               The beliefs used for computation
     * @return The EV2
     */
    double computeEVTwo(ArrayList<Integer> offerFromSelf, int i, int r, HashMap<ArrayList<Integer>, Double>  beliefs) {
        double firstOrderEV;
        firstOrderEV = (1 - c2) * computeEVOne(offerFromSelf, i, r, b1, false);
        restoreGuessedOffers();

        //Compute the second order EV
        double secondOrderEV = 0;
        for (int rGuess = 0; rGuess < 4; rGuess++) {

            secondOrderEV += p1.get(rGuess) * computeDirectEV(i, r, rGuess, offerFromSelf, beliefs, 1);
        }

        restoreGuessedOffers();
        return firstOrderEV + c2 * secondOrderEV;
    }

    /**
     * A method that computes the EV with certainty of this ToM (so without the option of using a
     * lower order of ToM).
     *
     * @param i             Integer that indicates whose turn it is
     * @param r             The producing resource of i
     * @param rGuess        The guessed producing resource by other agent
     * @param offerFromSelf The current offer, from POV of i
     * @param beliefs       The beliefs used for computation
     * @param order         The order of ToM to use
     * @return EV               The expected value
     */
    private double computeDirectEV(int i, int r, int rGuess, ArrayList<Integer> offerFromSelf, HashMap<ArrayList<Integer>, Double>  beliefs, int order) {
        int choice;

        if (i == 0) {
            choice = negotiateTurn(1,offerFromSelf,rGuess,order,beliefs, true);
        } else {
            choice = negotiateTurn(0,offerFromSelf,rGuess,order,beliefs, true);
        }
        ArrayList<Integer> afterOffer;
        switch (choice) {
            case 2: //ToMk-1 would withdraw
                return computePiValue(negotiation.getD_0().get(i),r);
            case 1: //ToMk-1 would accept
                afterOffer = afterOffer(offerFromSelf,negotiation.getAgent(i).getResourcesList());
                return computePiValue(afterOffer,r);
            case 0: //ToMk-1 would make counteroffer
                ArrayList<Integer> guessedOffer;
                if (!isUpdatingBelief) {
                    guessedOffer = negotiation.getBestOffer(order);
                } else {
                    guessedOffer = storeBestOffer.get(order);
                }
                afterOffer = afterOffer(mirrorOffer(guessedOffer),negotiation.getAgent(i).getResourcesList());
                return max(computePiValue(afterOffer,r), computePiValue(negotiation.getD_0().get(i),r));
        }
        System.out.println("Error");
        return 0;
    }

    /**
     * A method that computes the resources of this agent if this offer was done
     * @param offerFromSelf         The offer from the POV of this agent (so this agent made the offer)
     * @param resources             The current resources of this agent
     * @return                      The resources if this agent would execute offerFromSelf
     */
    ArrayList<Integer> afterOffer(ArrayList<Integer> offerFromSelf,ArrayList<Integer> resources) {
        ArrayList<Integer> afterOffer = new ArrayList<>(4);
        //From perspective of own offer
        afterOffer.add(resources.get(0) + offerFromSelf.get(4) - offerFromSelf.get(0));
        afterOffer.add(resources.get(1) + offerFromSelf.get(5) - offerFromSelf.get(1));
        afterOffer.add(resources.get(2) + offerFromSelf.get(6) - offerFromSelf.get(2));
        afterOffer.add(resources.get(3) + offerFromSelf.get(7) - offerFromSelf.get(3));

        return afterOffer;
    }

    //Belief updates

    /**
     * A method that updates the information about offer acceptance
     * @param offerFromSelf         The offer that this agent made
     * @param response              The response of the other agent (reject 0, accept 1)
     */
    public void updateAcceptanceStatistics(ArrayList<Integer> offerFromSelf, int response) {
        //Offer was from this agent
        int give = offerFromSelf.get(0) + offerFromSelf.get(1) + offerFromSelf.get(2) + offerFromSelf.get(3);
        int receive = offerFromSelf.get(4) + offerFromSelf.get(5) + offerFromSelf.get(6) + offerFromSelf.get(7);

        int newNOffers = offersMade.get(give).get(receive) + 1;
        int newNAccepted = offersAccepted.get(give).get(receive);
        offersMade.get(give).set(receive, newNOffers);

        if (response == 1) { //If offer was accepted
            newNAccepted +=1;
            offersAccepted.get(give).set(receive, newNAccepted);
        }
    }

    /**
     * A method that updates the initial beliefs based on previous acceptance rate.
     */
    public void updateInitialBeliefs() {
        for (int i = 0; i< 4*MAX_N_RESOURCES; i++) {
            for (int j = 0; j< 4*MAX_N_RESOURCES; j++) {
                int nOffers = offersMade.get(i).get(j);
                int nAccepted = offersAccepted.get(i).get(j);
                initialBeliefs.get(i).set(j,(double)nAccepted/nOffers);
            }
        }
    }

    /**
     * A method that updates the beliefs of the agent after its offer was rejected, or it
     * received a counteroffer
     * @param otherAgent        The trading partner of this agent
     * @param offerFromSelf     The offer from the POV of this agent
     * @param beliefs           The beliefs that the agent currently has
     * @param scenario          Int representing if this update happens after rejection (0)
     *                          or after a received offer (1)
     * @param reverse           True if agent eneds to take role of trading partner
     */
    public void updateBeliefs(Agent otherAgent, ArrayList<Integer> offerFromSelf, HashMap<ArrayList<Integer>, Double> beliefs, int scenario, int reverse) {
        int m = 0;  //The number of resources for which the potential offer assigns at least as many to agent i as the offer offerFromSelf
                    // OR The number of resources for which the potential offer assigns fewer to the trading partner
                    //than the offer offerFromSelf from the trading partner

        int give = offerFromSelf.get(0) + offerFromSelf.get(1) + offerFromSelf.get(2) + offerFromSelf.get(3);
        int receive = offerFromSelf.get(4) + offerFromSelf.get(5) + offerFromSelf.get(6) + offerFromSelf.get(7);
        allPossibleOffers(otherAgent, offerFromSelf, beliefs, m, receive, give, scenario, reverse);
    }

    /**
     * A method that loops over the possible offers that this agent can make to update beliefs
     * @param otherAgent    The trading partner
     * @param offerFromSelf The offer that was rejected or offered as counter
     * @param beliefs       The beliefs that this agent currently has
     * @param m             The number of resources for which the potential offer assigns fewer to the trading partner
     *                      than the offer from the trading partner OR The number of resources for which the potential
     *                      offer assigns at least as many to agent i as the offer from the trading partner
     * @param receive       The number of resources the agent gets
     * @param give          The number of resources the agent gives
     * @param scenario      The case that should be used
     * @param reverse       True if the agent needs to act as if it is the other agent
     */
    private void allPossibleOffers (Agent otherAgent, ArrayList<Integer> offerFromSelf, HashMap<ArrayList<Integer>, Double> beliefs, int m, int receive, int give, int scenario, int reverse) {
        Agent agent;
        if (reverse == 0) {
            agent = this;
        } else {
            agent = otherAgent;
            otherAgent = this;
        }
        int index;
        int[] variables;

        for (int a = 0; a <= min(MAX_N_RESOURCES-otherAgent.getResourceOne(),max(agent.getResourceOne() - RESOURCE_THRESHOLD, 0)); a++) {
            for (int b = 0; b <= min(MAX_N_RESOURCES-otherAgent.getResourceTwo(),max(agent.getResourceTwo() -RESOURCE_THRESHOLD, 0)); b++) {
                for (int c = 0; c <= min(MAX_N_RESOURCES-otherAgent.getResourceThree(),max(agent.getResourceThree()-RESOURCE_THRESHOLD, 0)); c++) {
                    for (int d = 0; d <= min(MAX_N_RESOURCES-otherAgent.getResourceFour(),max(agent.getResourceFour() - RESOURCE_THRESHOLD, 0)); d++) {
                        for (int e = 0; e <= min(otherAgent.getResourceOne(), (MAX_N_RESOURCES - agent.getResourceOne())); e++) {
                            for (int f = 0; f <= min(otherAgent.getResourceTwo(), (MAX_N_RESOURCES - agent.getResourceTwo())); f++) {
                                for (int g = 0; g <= min(otherAgent.getResourceThree(), (MAX_N_RESOURCES - agent.getResourceThree())); g++) {
                                    for (int h = 0; h <= min(otherAgent.getResourceFour(), (MAX_N_RESOURCES - agent.getResourceFour())); h++) {
                                        if (a+b+c+d+e+f+g+h == 0) { //All 0
                                            break;
                                        }
                                        if ((a>0 && e>0) || (b>0 && f>0) || (c>0 && g>0) || (d>0 && h>0) ) { //Interchanging same resources
                                            break;
                                        }
                                        switch (scenario) {
                                            case 0: //Own offer was rejected
                                                variables = new int[]{e, f, g, h};
                                                index = 4; //Starting index offer list
                                                for (int value : variables) {
                                                    if (value >= offerFromSelf.get(index) && offerFromSelf.get(index)!=0) {
                                                        m++;
                                                    }
                                                    index++;
                                                }
                                                computeNewBelief(beliefs, m, receive, give, a, b, c, d, e, f, g, h);
                                                break;
                                            case 1: //Got an offer from trading partner
                                                variables = new int[]{a, b, c, d};
                                                index = 0; //Starting index of the offer list
                                                for (int value : variables) {
                                                    if (value < offerFromSelf.get(index)) {
                                                        m++;
                                                    }
                                                    index++;
                                                }
                                                computeNewBelief(beliefs, m, receive, give, a, b, c, d, e, f, g, h);
                                        }
                                        m=0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A method that computes the new belief for a certain offer
     * @param beliefs The beliefs that need to be updated
     * @param m       The number of resources for which the potential offer assigns fewer chips to the trading partner
     *                than the offer from the trading partner OR The number of colors for which the potential offer
     *                assigns at least as many chips to agent i as the offer from the trading partner
     * @param receive The amount of resources that the agent gets
     * @param give    The amount of resources that the agent gives
     * @param a       Give from r1
     * @param b       Give from r2
     * @param c       Give from r3
     * @param d       Give from r4
     * @param e       Get from r1
     * @param f       Get from r2
     * @param g       Get from r3
     * @param h       Get from r4
     */
    private void computeNewBelief(HashMap<ArrayList<Integer>, Double> beliefs, int m, int receive, 
                                  int give, int a, int b, int c, int d, int e, int f, int g, int h) {
        double belief;
        if (m > 0) {
            ArrayList<Integer> key = new ArrayList<>(Arrays.asList(a, b, c, d, e, f, g, h));
            if (beliefs.containsKey(key)) {
                belief  = Math.pow((1 - LEARNING_SPEED), m) * beliefs.get(key);
                beliefs.put(key,belief);
            } else {
                belief  = Math.pow((1 - LEARNING_SPEED), m) * initialBeliefs.get(give).get(receive);
                beliefs.put(key,belief);

            }
        }
    }

    /**
     * A method that computes P beliefs and updates c
     * @param offerToSelf Offer as if made by other agent
     * @param order       Either 2 or 1, depending on p2 or p1 update
     * @param otherAgent  The other agent
     * @param p           Either p1 or p2 beliefs
     * @param j           Int indicating other agent
     * @param beliefs     The beliefs used for the update: b1 for p1, b2 for p2
     */
    public void updatePBeliefs(ArrayList<Integer> offerToSelf, int order, Agent otherAgent, ArrayList<Double> p, int j, HashMap<ArrayList<Integer>, Double> beliefs) {
        //Reset variables and start updating

        isUpdatingBelief = true;
        restoreSavedInfo();

        //Compute P values
        ArrayList<Integer> afterOffer = afterOffer(offerToSelf,otherAgent.getResourcesList());

        int i = 0;
        if (j==0) i=1;
        double sumP = 0;

        for (int r=0; r<4; r++) {
            if (computePiValue(afterOffer, r) <= computePiValue(otherAgent.getResourcesList(), r)) {
                p.set(r,0.0);
            } else {
                computeBestEV(i,r,beliefs,order-1, false);
                double newBelief = 0;
                if (storeBestEV.get(order) != null) {
                    //What this agent thinks the other agent's EV is
                    double EVscore = computeEV(mirrorOffer(offerToSelf),i,r,beliefs,order-1,false);//b1 for p1, b2 for p2
                    newBelief = p.get(r) * max(0,((1+EVscore) /(1+storeBestEV.get(order-1))));

                }
                p.set(r,newBelief);
                sumP += newBelief;
            }
        }
        //P's must add to 1, else reset to initial p beliefs
        if (sumP > 0) {
            double beta = 1.0/sumP;
            for (int r=0; r < 4; r++) {
                p.set(r, beta*p.get(r));
            }
        } else {
            for (int r=0; r < 4; r++) {
                p.set(r, 1.0/4);
            }
        }

        isUpdatingBelief = false;
    }


    /**
     * A method that updates the c value, so the confidence of this agent in a ToM order.
     * @param offerFromSelf     The offer, from POV as if this agent offered it
     * @param i                 The int representing this agent
     * @param p                 The location beliefs to be used, p1 for c1, p2 for c2.
     * @param order             The order of ToM to be used: 1 for c1, 2 for c2
     * @param beliefs           The beliefs to be used: b1 for c1, b2 for c2
     */
    public void updateConfidence(ArrayList<Integer> offerFromSelf, int i, ArrayList<Double> p, int order, HashMap<ArrayList<Integer>, Double> beliefs) {
        //Reset variables and start updating c
        isUpdatingBelief = true;
        restoreSavedInfo();

        //Compute c
        double sum = 0;
        double bestEV;
        for (int r = 0; r < 4; r++) {
            //What this agent would do if it were that order and in that position
            computeBestEV(i, r, beliefs,order,false);
            bestEV = storeBestEV.get(order);
            restoreSavedInfo();
            if (bestEV != -Double.MAX_VALUE) {
                sum += p.get(r) * Math.max(0, (1 + computeEV(mirrorOffer(offerFromSelf), i, r, beliefs, order,false)))
                        / Math.max(0, (1 + bestEV));
            }

        }
        if (order == 1) {
            c1 = Math.min(1.0,(1.0 - LEARNING_SPEED) * c1 + LEARNING_SPEED * sum);
        } else { //c2 update
            c2 = Math.min(1.0,(1.0 - LEARNING_SPEED) * c2 + LEARNING_SPEED * sum);

        }
        isUpdatingBelief = false;
    }

    /**
     * A method that restores the saved information about a potential offer
     */
    private void restoreSavedInfo() {
        storeBestEV.set(0,-Double.MAX_VALUE);
        storeBestEV.set(1,-Double.MAX_VALUE);
        storeBestEV.set(2,-Double.MAX_VALUE);
        storeBestOffer.set(0,null);
        storeBestOffer.set(1,null);
        storeBestOffer.set(2,null);
        restoreGuessedOffers();
    }

    /**
     * A method that resets the guessed counteroffers
     */
    private void restoreGuessedOffers() {
        for (int a=0; a<3; a++) { //Orders
            for (int b=0; b<4;b++) {//Producing R
                guessedOffers.put(new ArrayList<>(Arrays.asList(a,b)), null);
            }
        }
        for (int a=0; a<3; a++) { //Orders
            for (int b=0; b<4;b++) {//Producing R
                guessedEVs.put(new ArrayList<>(Arrays.asList(a,b)),-Double.MAX_VALUE);
            }
        }
    }

    /**
     * A method that restores the negotiation dependent variables: the ideas about the producing
     * resource of other agent (p) and the confidence in a certain order ToM of other agent (c)
     */
    private void restoreNegotiationDependentVars() {
        switch(order) {
            case 2:
                p2 = new ArrayList<>(Arrays.asList(0.25,0.25,0.25,0.25));
                c2 = 1;
            case 1:
                p1 = new ArrayList<>(Arrays.asList(0.25,0.25,0.25,0.25));
                c1 = 1;
        }
    }

    /**
     * A methods that updates statistics and variables when a negotiation has ended
     * @param type              The type of negotiation that is finished
     * @param finalDecision     The type of ending
     * @param negotiateRound    How many rounds it lasted
     */
    private void handleNegotiationEnd(ArrayList<Integer> type, int negotiateRound, int finalDecision) {
        file.addToNegotiationLengths(type,negotiateRound);
        file.addToNegotiationEndings(type,finalDecision);

        if (shouldReduceResources) { //Reduce resources now if it had an evolutionary check
            reduceResources();
            shouldReduceResources = false;
        }

        if (file.getExpType() == 2) { //Save partner specific info for next encounter
            ArrayList<ArrayList<Double>> saveInfo = new ArrayList<>();
            if (order > 0) {
                saveInfo.add(new ArrayList<>(List.of(c1)));
                saveInfo.add(new ArrayList<>(p1)); //Copy of p1
            }
            if (order > 1) {
                saveInfo.add(new ArrayList<>(List.of(c2)));
                saveInfo.add(new ArrayList<>(p2)); //Copy of p2
            }
            agentsInfo.put(previousTradePartner, saveInfo);
            restoreNegotiationDependentVars();

            //Remove from list
            unsuccessfulHistory.remove(String.valueOf(previousTradePartner));
            if (finalDecision < 2) { //No successful outcome, add agent again to unsuccessful list
                unsuccessfulHistory.add(String.valueOf(previousTradePartner));
            }
        }
    }

    /**
     * A method that loads saved information about the agent if they negotiated before
     */
    private void checkAgent() {
        if (!(agentsInfo == null) && agentsInfo.containsKey(previousTradePartner)) { //Encountered agent before
            if (order>0) {
                c1 = agentsInfo.get(previousTradePartner).get(0).get(0);
                p1 = agentsInfo.get(previousTradePartner).get(1);
            }
            if (order>1) {
                c2 = agentsInfo.get(previousTradePartner).get(2).get(0);
                p2 = agentsInfo.get(previousTradePartner).get(3);
            }
        }
    }

    //Helper functions

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
     * A method that increases the resource that this agent produces to 4
     */
    public void generateNewResource() {
        switch (producingResource) {
            case 0:
                if (resourceOne<MAX_N_RESOURCES) resourceOne=MAX_N_RESOURCES;
                break;
            case 1:
                if (resourceTwo<MAX_N_RESOURCES) resourceTwo=MAX_N_RESOURCES;
                break;
            case 2:
                if (resourceThree<MAX_N_RESOURCES) resourceThree=MAX_N_RESOURCES;
                break;
            case 3:
                if (resourceFour<MAX_N_RESOURCES) resourceFour=MAX_N_RESOURCES;
                break;
        }
    }

    /**
     * A method that handles the outcome of the negotiation.
     * @param getOne How many of resourceOne the agent receives
     * @param getTwo How many of resourceTwo the agent receives
     * @param getThree How many of resourceThree the agent receives
     * @param getFour How many of resourceFour the agent receives
     * @param giveOne How many of resourceOne the agent gives away
     * @param giveTwo How many of resourceTwo the agent gives away
     * @param giveThree How many of resourceThree the agent gives away
     * @param giveFour How many of resourceFour the agent gives away
     * @return double   Difference between pi score at start and after deal
     */
    public double tradeResources(int getOne, int getTwo, int getThree, int getFour,
                               int giveOne, int giveTwo, int giveThree, int giveFour) {

        double startPi = computePiValue(getResourcesList(), producingResource);
        resourceOne += getOne;
        resourceOne -= giveOne;
        resourceTwo += getTwo;
        resourceTwo -= giveTwo;
        resourceThree += getThree;
        resourceThree -= giveThree;
        resourceFour += getFour;
        resourceFour -= giveFour;
        double endPi = computePiValue(getResourcesList(), producingResource);
        return endPi-startPi;
    }

    /**
     * A method that reduces the stock of the agent
     */
    public void reduceResources() {
        if (isNegotiating) { //Do not reduce resources during a negotiation
            shouldReduceResources = true;
            return;
        }
        if (resourceOne != 0) resourceOne--;
        if (resourceTwo != 0) resourceTwo--;
        if (resourceThree != 0) resourceThree--;
        if (resourceFour != 0) resourceFour--;
    }

    /**
     * A method that restores the resources of this initial agent to the initial values, i.e. 1 for all resources
     * except the producing resource.
     */
    public void resetToInitialResources() {
        resourceOne = N_START_RESOURCES;
        resourceTwo = N_START_RESOURCES;
        resourceThree = N_START_RESOURCES;
        resourceFour = N_START_RESOURCES;

        fillProducingResource();
    }

    /**
     * A method that sets the producing resource of the agent to the MAX_N_RESOURCES
     */
    public void fillProducingResource() {
        switch (producingResource) {
            case 0: resourceOne = MAX_N_RESOURCES;
                break;
            case 1: resourceTwo = MAX_N_RESOURCES;
                break;
            case 2: resourceThree = MAX_N_RESOURCES;
                break;
            case 3: resourceFour = MAX_N_RESOURCES;
        }
    }


    /**
     * A method that increments the age of this agent
     */
    public void incrementAge() {
        age++;
    }


    // Getters
    /**
     * A method that returns the x location of this agent
     * @return xLoc
     */
    public double getXLoc() {
        return xLoc;
    }

    /**
     * A method that returns the y location of this agent
     * @return yLoc
     */
    public double getYLoc() {
        return yLoc;
    }

    /**
     * A method that returns the resourceOne of this agent
     * @return resourceOne
     */
    public int getResourceOne() {
        return resourceOne;
    }

    /**
     * A method that returns the resourceTwo of this agent
     * @return resourceTwo
     */
    public int getResourceTwo() {
        return resourceTwo;
    }

    /**
     * A method that returns the resourceThree of this agent
     * @return resourceThree
     */
    public int getResourceThree() {
        return resourceThree;
    }

    /**
     * A method that returns the resourceFour of this agent
     * @return resourceFour
     */
    public int getResourceFour() {
        return resourceFour;
    }

    /**
     * A method that gives which resource this agent produces
     * @return producingResource Integer between 1 and 5
     */
    public int getProducingResource() {
        return producingResource;
    }

    /**
     * A method that returns the beliefs of the agent, differs per agent
     * @return The beliefs
     */
    public HashMap<ArrayList<Integer>, Double>  getBeliefs() {
        return b0;
    }

    /**
     * A method that returns the initial beliefs of the agent
     * @return initialBeliefs   The beliefs that are only updated once in a while
     */
    public ArrayList<ArrayList<Double>> getInitialBeliefs() {
        return initialBeliefs;
    }

    /**
     * A method that returns b0
     * @return  b0
     */
    public HashMap<ArrayList<Integer>, Double> getB0() {
        return b0;
    }

    /**
     * A method that returns b1
     * @return  b1
     */
    public HashMap<ArrayList<Integer>, Double> getB1() {
        return b1;
    }

    /**
     * A method that returns p1
     * @return p1
     */
    public ArrayList<Double> getP1() {
        return p1;
    }

    /**
     * A method that returns p2
     * @return p2
     */
    public ArrayList<Double> getP2() {
        return p2;
    }

    /**
     * A method that gives the order of ToM of this agent
     * @return order The ToM order
     */
    public int getOrder() {
        return order;
    }

    /**
     * A method that returns the previous partner of this agent
     * @return previousTradePartner The (index of) previous negotiation partner
     */
    public int getPreviousTradePartner() {
        return previousTradePartner;
    }

    /**
     * A method that returns the age of this agent
     * @return age The age of this agent.
     */
    public int getAge() {
        return age;
    }

    /**
     * A method that returns the state of isNegotiating
     * @return isNegotiating The state
     */
    public boolean isNegotiating() {
        return isNegotiating;
    }

    /**
     * A method that returns the index of the agent
     * @return index The integer index
     */
    public int getIndex() {
        return index;
    }

    /**
     * A method that gives a list of all the resource quantities of the agent
     * @return arraylist containing the quantities
     */
    public ArrayList<Integer> getResourcesList() {
        ArrayList<Integer> resources = new ArrayList<>();
        resources.add(resourceOne);
        resources.add(resourceTwo);
        resources.add(resourceThree);
        resources.add(resourceFour);
        return resources;
    }

    /**
     * A method that gives the number of offers made
     * @return offersMade
     */
    public ArrayList<ArrayList<Integer>> getOffersMade() {
        return offersMade;
    }

    /**
     * A method that gives the number of offers that were accepted
     * @return offersAccepted
     */
    public ArrayList<ArrayList<Integer>> getOffersAccepted() {
        return offersAccepted;
    }

    /**
     * A method that returns if the agent officially died during the negotiation
     * @return isDying  True if the agent should die
     */
    public boolean isDying() {
        return isDying;
    }

    // Setters
    /**
     * A method that sets the state to Negotiating, or not
     * @param negotiating Boolean
     */
    public void setNegotiating(boolean negotiating, int negotiateRound, ArrayList<Integer> type, int finalDecision) {
        isNegotiating = negotiating;
        if (!negotiating) {
            handleNegotiationEnd(type, negotiateRound, finalDecision);
        }
    }

    /**
     * A method that initializes a new negotiation between this agent and another
     * @param negotiation   The new negotiation
     */
    public void setNegotiation(Negotiation negotiation) {
        this.negotiation = negotiation;
        if (file.getExpState() == 1 && file.getExpType() == 2) {
            checkAgent();
        }
    }

    /**
     * A method that set the previousTradePartner
     * @param previousTradePartner The (index of) agent that this agent has negotiated with
     */
    public void setPreviousTradePartner(int previousTradePartner) {
        this.previousTradePartner = previousTradePartner;
    }

    /**
     * A method that returns the list of agents with which it had a negative occurrence in the past
     * @return unsuccessfulHistory
     */
    public ArrayList<String> getUnsuccessfulHistory() {
        return unsuccessfulHistory;
    }

    /**
     * A method that can set the age of an agent
     * @param age The new age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * A method that sets the initial beliefs to those of a parent
     * @param initialBeliefs    The beliefs of the parent.
     */
    public void setInitialBeliefs(ArrayList<ArrayList<Double>> initialBeliefs) {
        this.initialBeliefs = initialBeliefs;
    }

    /**
     * A method that sets b2 to b0, since environment is fully observable
     */
    public void setB2() {
        this.b2 = b0;
    }

    /**
     * A method that sets the number of offers that were made
     * @param offersMade        The number of offers made per type
     */
    public void setOffersMade(ArrayList<ArrayList<Integer>> offersMade) {
        this.offersMade = offersMade;
    }

    /**
     * A method that sets the number of offers that were accepted
     * @param offersAccepted        The number of offers that were accepted per type
     */
    public void setOffersAccepted(ArrayList<ArrayList<Integer>> offersAccepted) {
        this.offersAccepted = offersAccepted;
    }

    /**
     * A method that sets the boolean isDying to true if the agent died during negotiation
     * @param dying true if the agent was checked and did not reach threshold
     */
    public void setDying(boolean dying) {
        isDying = dying;
    }
}
