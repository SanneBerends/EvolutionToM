package model;

import model.agents.*;
import view.FileObserver;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The file class, which models the program
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class File {
    private static final int N_AGENTS = 30; //*3 in total
    private static final double AGENT_RADIUS_THRESHOLD = 10;
    private static final int SLEEP_TIME = 0;
    private static final int RESOURCE_THRESHOLD = 2;
    private static final int MUTATION_CHANCE = 2; //2 out of 100
    private static final int CHECK_INTERVAL = 2500;
    private static final int INIT_TIME = 1_000_000; //1 000 000 in exp
    private static final int EXP_LENGTH = 2500*1500;  //1500 gens
    private static final int LOWER_BORDER = 0;
    private static final int UPPER_BORDER = 600;
    private static final boolean SAVE_STEPS = true; //change if you want all the data

    private final String fileName;
    private final boolean GUI; //1 if enabled, 0 otherwise
    private final int expType; //1 or 2

    private final List<FileObserver> observers;
    private final ArrayList<ToM0Agent> toM0Agents;
    private final ArrayList<ToM1Agent> toM1Agents;
    private final ArrayList<ToM2Agent> toM2Agents;
    private final ArrayList<Agent> allAgents;
    private final List<int[]> historyData;
    private ArrayList<Integer> dominanceFrequency;
    private Map<Integer, List<Integer>> agentAges;
    private Map<ArrayList<Integer>, List<Integer>> negotiationLengths;
    private Map<ArrayList<Integer>, List<Integer>> negotiationEndings;
    private Map<ArrayList<Integer>, List<List<Double>>> piGains;

    private int expState; //0 if not initialized yet, -1 if busy initializing, 1 if experimenting, 2 if done experiment
    private boolean isRunning;
    private int timer;
    private int deathCnt;
    private int maxIndex;


    /**
     * The constructor that creates a file with observers
     */
    public File(boolean GUI, String fileName, int expType) {
        this.GUI = GUI;
        this.fileName = fileName;
        this.expType = expType;

        observers = new ArrayList<>();
        toM0Agents = new ArrayList<>();
        toM1Agents = new ArrayList<>();
        toM2Agents = new ArrayList<>();
        allAgents = new ArrayList<>();

        isRunning = false;
        timer = 0;
        deathCnt = 0;
        maxIndex = 0;
        dominanceFrequency = new ArrayList<>(Arrays.asList(0, 0, 0));
        historyData = new ArrayList<>();
        agentAges = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            agentAges.put(i, new ArrayList<>());
        }
        negotiationLengths = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationLengths.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        negotiationEndings = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationEndings.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        piGains = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                ArrayList<Integer> key = new ArrayList<>(Arrays.asList(a, b));
                List<List<Double>> value = new ArrayList<>();

                piGains.put(key, value);
            }
        }

        createInitialPopulation();
    }

        /*
    Methods
     */


    /**
     * A method that creates the initial population of ToM0 agents
     */
    private void createInitialPopulation() {
        for (int i = 0; i < N_AGENTS * 2; i++) {
            updateAllAgents();
            ToM0Agent toM0Agent = new ToM0Agent(maxIndex, this);
            addToM0Agent(toM0Agent);
            maxIndex++;
        }
        updateAllAgents();
        addAgentCountsToHistoryData();
        notifyAgentsObservers();
    }

    /**
     * A method that creates a new file
     */
    public void newFile() {
        pauseSimulation();
        // Wait for the run thread to exit
        synchronized (this) {
            while (isRunning) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("Waiting for run loop to stop was interrupted");
                    throw new RuntimeException(e);
                }
            }
        }
        emptyFile();
        createInitialPopulation();
        notifyRunningObservers();
        notifyTimerObservers();
        notifyExpStateObservers();
        notifyAgentsObservers();
    }

    /**
     * A method that cleans the screen and resets parameters
     */
    @SuppressWarnings("DuplicatedCode")
    public void emptyFile() {
        isRunning = false;
        timer = 0;
        expState = 0;
        toM0Agents.clear();
        toM1Agents.clear();
        toM2Agents.clear();
        deathCnt = 0;
        maxIndex = 0;
        historyData.clear();
        dominanceFrequency = new ArrayList<>(Arrays.asList(0, 0, 0));
        agentAges = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            agentAges.put(i, new ArrayList<>());
        }
        negotiationLengths = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationLengths.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        negotiationEndings = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationEndings.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        piGains = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                ArrayList<Integer> key = new ArrayList<>(Arrays.asList(a, b));
                List<List<Double>> value = new ArrayList<>();

                piGains.put(key, value);
            }
        }
    }

    /**
     * A method that initializes the experiment by creating the initialBeliefs
     */
    public void initializeExperiment() {
        System.out.println("Initialize experiment");
        expState = -1;
        notifyExpStateObservers();

        startSimulation();
    }

    /**
     * A method that pauses the simulation
     */
    public void pauseSimulation() {
        System.out.println("Paused");
        stopLoop();
        notifyRunningObservers();
    }

    /**
     * A method that starts the simulation
     */
    public void startSimulation() {
        System.out.println("Started");
        startLoop();
    }

    /**
     * Clears allAgents and then concatenates the lists of agents into one list
     */
    private void updateAllAgents() {
        if (allAgents != null) {
            allAgents.clear();
        }
        if (allAgents != null) {
            allAgents.addAll(toM0Agents);
        }
        assert allAgents != null;
        allAgents.addAll(toM1Agents);
        allAgents.addAll(toM2Agents);
    }

    /**
     * A method that initializes the population: it adds N_AGENTS of each type
     */
    public void initializeAgents() {
        for (int i = 0; i < N_AGENTS; i++) {
            updateAllAgents();
            ToM0Agent toM0Agent = new ToM0Agent(maxIndex, this);
            addToM0Agent(toM0Agent);
            maxIndex++;
            updateAllAgents();
            ToM1Agent toM1Agent = new ToM1Agent(maxIndex, this);
            addToM1Agent(toM1Agent);
            maxIndex++;
            updateAllAgents();
            ToM2Agent toM2Agent = new ToM2Agent(maxIndex, this);
            addToM2Agent(toM2Agent);
            maxIndex++;
            updateAllAgents();
        }
    }

    /**
     * A method that checks if the new location of an agent is allowed
     *
     * @param currentAgent The agent whose new location needs to be checked
     * @param x            The new x location
     * @param y            The new y location
     * @return 0, 1 or 2    Indicates the scenario that applies
     */
    public int checkNewLocation(Agent currentAgent, double x, double y) {
        updateAllAgents();
        for (Agent agent : allAgents) {
            if (agent.getIndex() != currentAgent.getIndex()) {
                double distance = Math.sqrt(Math.pow(agent.getXLoc() - x, 2) + Math.pow(agent.getYLoc() - y, 2));
                if (distance < AGENT_RADIUS_THRESHOLD) {//There is another agent within negotiating distance
                    if (agent.isNegotiating() || agent.getIndex() == currentAgent.getPreviousTradePartner() ||
                            currentAgent.getIndex() == agent.getPreviousTradePartner()) { //Not suitable trading partner
                        return 0;
                    }
                    if (expType == 2) { //Use experiences
                        List<String> lastFiveElements = getLast5Experiences(currentAgent);
                        if (lastFiveElements.contains(String.valueOf(agent.getIndex()))) { //Avoid this agent
                            return 0;
                        }
                    }
                    startNegotiating(agent, currentAgent);
                    return 1;
                }
            }
        }
        // No other agent found at the new location
        return 2;
    }

    /**
     * A method that returns the last 5 bad experiences of the current agent
     * @param currentAgent          The agent looking for a new position
     * @return  lastFiveElements    A list of the last 5 indices
     */
    private static List<String> getLast5Experiences(Agent currentAgent) {
        List<String> lastFiveElements;
        ArrayList<String> unsuccessfulHistory = currentAgent.getUnsuccessfulHistory();
        int size = unsuccessfulHistory.size();
        if (size <= 5) {
            lastFiveElements = unsuccessfulHistory.subList(0, size);
        } else {
            lastFiveElements = unsuccessfulHistory.subList(size - 5, size);
        }
        return lastFiveElements;
    }

    /**
     * A method that starts a new negotiation between two agents
     *
     * @param agent        One of the two agents of the negotiation
     * @param currentAgent The agent who just arrived at location of the other agent
     */
    private void startNegotiating(Agent agent, Agent currentAgent) {
        //Agents pick each other for negotiation
        agent.setNegotiating(true, 0, null, -2);
        currentAgent.updateInitialBeliefs();
        agent.updateInitialBeliefs();
        currentAgent.setPreviousTradePartner(agent.getIndex());
        agent.setPreviousTradePartner(currentAgent.getIndex());

        currentAgent.generateNewResource();
        agent.generateNewResource();

        //Randomize who makes the initial offer
        int random = ThreadLocalRandom.current().nextInt(0, 2);
        Negotiation negotiation;
        if (random == 0) {
            negotiation = new Negotiation(currentAgent, agent, this);
        } else {
            negotiation = new Negotiation(agent, currentAgent, this);
        }

        //Start the negotiation
        currentAgent.setNegotiation(negotiation);
        agent.setNegotiation(negotiation);
    }

    /**
     * A method that checks if the initial locations of an agent are allowed in the file
     *
     * @param currentAgent The agent of which the location needs to be checked
     * @param xLoc         Its x location
     * @param yLoc         Its y location
     * @return double[] {xLoc, yLoc} The allowed location
     */
    public double[] checkInitialLocation(Agent currentAgent, double xLoc, double yLoc) {
        boolean freeLocation = false;
        while (!freeLocation) {
            freeLocation = true;
            for (Agent agent : allAgents) {
                if (agent != currentAgent) {
                    double distance = Math.sqrt(Math.pow(agent.getXLoc() - xLoc, 2) + Math.pow(agent.getYLoc() - yLoc, 2));
                    if (distance <= AGENT_RADIUS_THRESHOLD) {//There is another agent at the new location
                        xLoc = ThreadLocalRandom.current().nextInt(LOWER_BORDER, UPPER_BORDER);
                        yLoc = ThreadLocalRandom.current().nextInt(LOWER_BORDER, UPPER_BORDER);
                        freeLocation = false;
                    }
                }
            }
        }
        return new double[]{xLoc, yLoc};
    }

    /**
     * A method to start the run loop
     */
    public void startLoop() {
        isRunning = true;
        new Thread(this::run).start(); //Start the loop in a new thread
    }

    /**
     * A method that stops the run loop
     */
    public void stopLoop() {
        isRunning = false;
        notifyRunningObservers();
    }

    /**
     * A method that makes all agents take a step
     */
    private synchronized void run() {
        //Each agent takes one step (either move or negotiate)
        while (isRunning) {
            updateAllAgents();
            for (int i = 0; i < allAgents.size(); i++) {
                allAgents.get(i).step();
            }

            addAgentCountsToHistoryData();
            incrementTimer();
            for (Agent agent : allAgents) {
                if (!agent.isDying()) { //Agent is officially already dead
                    agent.incrementAge();
                }
            }
            if (expState == 1) handleEvolutionaryChecks(allAgents);
            if (expState == -1) reduceResourcesIfNecessary(allAgents);

            //Notify and next run
            notifyRunningObservers();
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                System.out.println("Wait does not work");
                throw new RuntimeException(e);
            }
            deathCnt = 0;

            if (expState == -1 && timer >= INIT_TIME) { //Ready for experiment
                startExperiment();
            } else if (expState == 1 && (timer >= EXP_LENGTH || oneTypeLeft() || allTypesDead())) { //Experiment is done
                System.out.println("Either time is up : " + timer + ">" + EXP_LENGTH + ", all agents are dead (" + allTypesDead() + ") or only 1 type is left.");
                addAgentCountsToHistoryData();
                stopExperiment();
            }

        }
    }

    /**
     * A method that reduces the resources of an agent during the initialization phase if it has [4,4,4,4]
     *
     * @param allAgents All agents in the initial population
     */
    private void reduceResourcesIfNecessary(ArrayList<Agent> allAgents) {
        for (int i = 0; i < allAgents.size(); i++) {
            for (Agent agent : allAgents) {
                if (agent.getResourceOne() == 4 && agent.getResourceTwo() == 4 && agent.getResourceThree() == 4 &&
                        agent.getResourceFour() == 4) {
                    agent.resetToInitialResources();
                }
            }
        }
    }

    /**
     * A method that determines if there is only one type of ToM left
     *
     * @return boolean True if one type left
     */
    private boolean oneTypeLeft() {
        if (toM0Agents.isEmpty() && toM1Agents.isEmpty()) {
            return true;
        }
        if (toM0Agents.isEmpty() && toM2Agents.isEmpty()) {
            return true;
        }
        return toM1Agents.isEmpty() && toM2Agents.isEmpty();
    }

    /**
     * A method that returns if all agents died during an evolutionary check
     *
     * @return True if there are no agents anymore
     */
    private boolean allTypesDead() {
        updateAllAgents();
        return allAgents.isEmpty();
    }

    /**
     * A method that finalises the initialization period and starts the experiment
     */
    public void startExperiment() {
        System.out.println("Start experiment");
        if (GUI) pauseSimulation();
        expState = 1;

        deathCnt = 0;
        timer = 0;
        maxIndex = 0;
        historyData.clear();
        dominanceFrequency = new ArrayList<>(Arrays.asList(0, 0, 0));
        agentAges = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            agentAges.put(i, new ArrayList<>());
        }
        negotiationLengths = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationLengths.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        negotiationEndings = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                negotiationEndings.put(new ArrayList<>(Arrays.asList(a, b)), new ArrayList<>());
            }
        }
        piGains = new HashMap<>();
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                ArrayList<Integer> key = new ArrayList<>(Arrays.asList(a, b));
                List<List<Double>> value = new ArrayList<>();

                piGains.put(key, value);
            }
        }

        ArrayList<ToM0Agent> initialAgents = new ArrayList<>(toM0Agents);
        toM0Agents.clear();

        updateAllAgents();
        initializeAgents(); //Create all agents
        addAgentCountsToHistoryData();
        notifyExpStateObservers();

        //Set initialBeliefs to baseline
        for (int i = 0; i < allAgents.size(); i++) {
            Random r = new Random();
            Agent copyAgent = initialAgents.get(r.nextInt(initialAgents.size())); //Select random 'parent'
            allAgents.get(i).setInitialBeliefs(copyAgent.getInitialBeliefs());
            allAgents.get(i).setOffersMade(copyAgent.getOffersMade());
            allAgents.get(i).setOffersAccepted(copyAgent.getOffersAccepted());
        }

        notifyAgentsObservers();
        notifyTimerObservers();
    }

    /**
     * A method that finalises the experiment and saves the results
     */
    private void stopExperiment() {
        pauseSimulation();
        for (Agent agent : allAgents) {
            agentAges.get(agent.getOrder()).add(agent.getAge());
        }

        writeOutput();
        expState = 2;
        notifyExpStateObservers();
        if (!GUI) {
            System.exit(0);
        }
    }

    /**
     * A method that writes the results (the history data) to a file named results.ToM
     */
    private void writeOutput() {
        //Open writer
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName + "_" + 0 + ".csv");
            System.out.println("Created write object");
        } catch (IOException e) {
            System.out.println("Could not write file");
        }

        //Write
        String save = String.format("%d \n", timer) + "\nToM0, ToM1, ToM2\n";

        //Write date lists to file
        save = writeHistoryData(save);
        save += "\n Dominance frequency \n";
        save = writeDominanceFreqs(save);
        System.out.println("Wrote dom freqs");
        save += "\n  Ages \n";
        save = writeAges(save);
        System.out.println("Wrote dom ages");
        save += "\n Negotiation Lengths \n";
        save = writeNegotiationLengths(save);
        System.out.println("Wrote neg lengths");
        save += "\n Negotiation Endings ( -1: too long, 0: 0 withdrew, 1: 1 withdrew, 2: 0 accepted, 3: 1 accepted)\n ";
        save = writeNegotiationEndings(save);
        System.out.println("Wrote endings");
        save += "\n Pi Gains (left is initiating agent, right is other agent) \n";
        save = writePiGains(save);
        save += "\n";


        try {
            if (writer != null) {
                writer.write(save, 0, save.length());
            }
        } catch (IOException e) {
            System.out.println("Could not write file");
        }

        //Close writer
        try {
            assert writer != null;
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not write file");
        }
        System.out.println("Closed writer");

    }

    /**
     * A method that writes the gain of each agent for each type of negotiation
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writePiGains(String save) {
        save += " 0-0,0-1,0-2,1-0,1-1,1-2,2-0,2-1,2-2 \n";
        int maxLength = 0;

        // Find the maximum size of the lists in the map
        for (List<List<Double>> gains : piGains.values()) {
            if (gains != null && gains.size() > maxLength) {
                maxLength = gains.size();
            }
        }

        // Iterate and write every two values of each list in the CSV
        for (int i = 0; i < maxLength; i++) {
            StringBuilder row = new StringBuilder();
            for (int a = 0; a <= 2; a++) {
                for (int b = 0; b <= 2; b++) { // Iterate through negotiation types
                    List<List<Double>> gains = piGains.get(Arrays.asList(a, b));
                    if (gains != null && i < gains.size()) {
                        List<Double> gainPair = gains.get(i);
                        row.append(gainPair.get(0) + "-" + gainPair.get(1));
                    }
                    row.append(',');
                }
            }
            if (!row.isEmpty()) {
                row.deleteCharAt(row.length() - 1);
            }
            row.append('\n');
            save += row.toString();
        }

        return save;
    }

    /**
     * A method that writes the fraction of the time that each agent was dominant
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writeDominanceFreqs(String save) {
        save += dominanceFrequency.get(0) / (2 + (double) timer) + "," + dominanceFrequency.get(1) / (2 + (double) timer) + "," + dominanceFrequency.get(2) / (2 + (double) timer) + "\n";
        return save;
    }

    /**
     * A method that writes the outcome of the negotiations
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writeNegotiationEndings(String save) {
        save += "Negotiation Endings \n 0-0, 0-1,0-2,1-0,1-1,1-2,2-0,2-1,2-2 \n";
        // Determine the maximum length of the lists
        int maxLength = 0;
        for (List<Integer> lengths : negotiationEndings.values()) {
            if (lengths != null && lengths.size() > maxLength) {
                maxLength = lengths.size();
            }
        }

        // Write the data rows
        for (int i = 0; i < maxLength; i = i + 2) { //Skip double indications of same negotiation
            StringBuilder row = new StringBuilder();
            for (int a = 0; a <= 2; a++) {
                for (int b = 0; b <= 2; b++) {
                    List<Integer> lengths = negotiationEndings.get(Arrays.asList(a, b));
                    if (lengths != null && i < lengths.size()) {
                        row.append(lengths.get(i));
                    }
                    row.append(',');
                }
            }
            // Remove the trailing comma and add a newline
            if (!row.isEmpty()) {
                row.deleteCharAt(row.length() - 1);
            }
            row.append('\n');
            save += row.toString();
        }
        return save;

    }

    /**
     * A method that writes the length negotiations
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writeNegotiationLengths(String save) {
        save += "Negotiation lengths \n 0-0, 0-1,0-2,1-0,1-1,1-2,2-0,2-1,2-2 \n";
        // Determine the maximum length of the lists
        int nNegotiations = 0;
        int maxLength = 0;
        for (List<Integer> lengths : negotiationLengths.values()) {
            if (lengths != null && lengths.size() > maxLength) {
                maxLength = lengths.size();
            }
        }

        // Write the data rows
        for (int i = 0; i < maxLength; i = i + 2) { //Skip double indications of same negotiation
            StringBuilder row = new StringBuilder();
            for (int a = 0; a <= 2; a++) {
                for (int b = 0; b <= 2; b++) {
                    List<Integer> lengths = negotiationLengths.get(Arrays.asList(a, b));
                    if (lengths != null && i < lengths.size()) {
                        row.append(lengths.get(i));
                        nNegotiations++;
                    }
                    row.append(',');
                }
            }
            // Remove the trailing comma and add a newline
            if (!row.isEmpty()) {
                row.deleteCharAt(row.length() - 1);
            }
            row.append('\n');
            save += row.toString();
        }
        save += "\n" + (nNegotiations) + "\n";
        return save;

    }

    /**
     * A method that writes the ages of the agents when they died/ when the
     * experiment ended to a file
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writeAges(String save) {
        // Determine the maximum length of the lists
        int maxLength = 0;
        for (List<Integer> ages : agentAges.values()) {
            if (ages.size() > maxLength) {
                maxLength = ages.size();
            }
        }

        // Write the data rows
        for (int i = 0; i < maxLength; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j <= 2; j++) {
                List<Integer> ages = agentAges.get(j);
                if (i < ages.size()) {
                    row.append(ages.get(i));
                }
                row.append(',');
            }
            // Remove the trailing comma and add a newline
            save += (row.substring(0, row.length() - 1) + "\n");
        }
        return save;
    }

    /**
     * A method that writes the history of the existing species
     *
     * @param save The string to which to add the new data
     * @return save The string including the new data
     */
    private String writeHistoryData(String save) {
        System.out.println("Writing history data");
        if (SAVE_STEPS) {
            for (int i = 0; i < historyData.size(); i += 5) {  // Only save 1 in every 5
                int[] data = historyData.get(i);
                String historyLine = String.join(",", Arrays.stream(data).mapToObj(String::valueOf).toArray(String[]::new));
                save += historyLine + "\n";
            }
        } else {
            if (!historyData.isEmpty()) {
                int[] lastData = historyData.get(historyData.size() - 2);  // Get the last element
                String historyLine = String.join(",", Arrays.stream(lastData).mapToObj(String::valueOf).toArray(String[]::new));
                save += historyLine + "\n";  // Save the last line with a newline
                lastData = historyData.get(historyData.size() - 1);  // Get the last element
                historyLine = String.join(",", Arrays.stream(lastData).mapToObj(String::valueOf).toArray(String[]::new));
                save += historyLine + "\n";  // Save the last line with a newline
            }
        }
        return save;
    }

    /**
     * A method that adds the number of agents per type to historyData
     */
    private void addAgentCountsToHistoryData() {
        int[] agentCounts = new int[]{toM0Agents.size(), toM1Agents.size(), toM2Agents.size()};
        historyData.add(agentCounts);
        int oldValue;
        if (toM0Agents.size() >= toM1Agents.size() && toM0Agents.size() >= toM2Agents.size()) {
            oldValue = dominanceFrequency.get(0);
            dominanceFrequency.set(0, oldValue + 1);
        }
        if (toM1Agents.size() >= toM0Agents.size() && toM1Agents.size() >= toM2Agents.size()) {
            oldValue = dominanceFrequency.get(1);
            dominanceFrequency.set(1, oldValue + 1);
        }
        if (toM2Agents.size() >= toM0Agents.size() && toM2Agents.size() >= toM1Agents.size()) {
            oldValue = dominanceFrequency.get(2);
            dominanceFrequency.set(2, oldValue + 1);
        }
    }

    /**
     * A method that adds a negotiation length to the list when a  negotiation has ended
     *
     * @param type   The two agents that were negotiating (10 means ToM1 and ToM0)
     * @param length The number of negotiation rounds.
     */
    public void addToNegotiationLengths(ArrayList<Integer> type, int length) {
        negotiationLengths.get(type).add(length);
    }

    /**
     * A method that adds a negotiation length to the list when a  negotiation has ended
     *
     * @param type   The two agents that were negotiating (10 means ToM1 and ToM0)
     * @param ending The number ending of the negotiation, i.e. // -1: too long, 0: 0 withdrew, 1: 1 withdrew,
     *               2: 0 accepted, 3: 1 accepted.
     */
    public void addToNegotiationEndings(ArrayList<Integer> type, int ending) {
        negotiationEndings.get(type).add(ending);
    }

    /**
     * A method that adds a pi gain to the list when a  negotiation has ended
     *
     * @param type  The two agents that were negotiating (10 means ToM1 and ToM0)
     * @param gain0 The gain in pi of the agent that started
     * @param gain1 The gain of the other agent
     */
    public void addToPiGains(ArrayList<Integer> type, double gain0, double gain1) {
        piGains.get(type).add(Arrays.asList(gain0, gain1));
    }

    /**
     * Handles periodic checks such as evolutionary checks and adding agents
     *
     * @param allAgents The concatenated list of all agents
     */
    private void handleEvolutionaryChecks(ArrayList<Agent> allAgents) {
        updateAllAgents();
        deathCnt = 0;
        for (Agent agent : allAgents) {
            if (!agent.isNegotiating() && agent.isDying()) { //Agent should die
                removeAgent(agent);
                agentAges.get(agent.getOrder()).add(agent.getAge());
                deathCnt++;
            } else if (agent.getAge() % CHECK_INTERVAL == 0) { //Every so many ticks it gets checked
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    System.out.println("sleep went wrong");
                }
                evolutionaryCheck(agent);
            }
        }
        updateAllAgents();

        for (int i = 0; i < deathCnt; i++) { //Replace the removed agents
            addAgent();
        }
        updateAllAgents();
    }

    /**
     * A method that checks if the agent survives, if not, it is replaced
     *
     * @param agent The agent that is checked
     */
    private void evolutionaryCheck(Agent agent) {
        //If the experiment is running and threshold is not reached (for non-producing resources), dead

        if (agent.getResourceOne() < RESOURCE_THRESHOLD && agent.getProducingResource() != 0) {
            if (agent.isNegotiating()) { //Agent dies after negotiation
                agent.setDying(true);
                return;
            }
            removeAgent(agent);
            agentAges.get(agent.getOrder()).add(agent.getAge());
            deathCnt++;
        } else if (agent.getResourceTwo() < RESOURCE_THRESHOLD && agent.getProducingResource() != 1) {
            if (agent.isNegotiating()) { //Agent dies after negotiation
                agent.setDying(true);
                return;
            }
            removeAgent(agent);
            agentAges.get(agent.getOrder()).add(agent.getAge());
            deathCnt++;
        } else if (agent.getResourceThree() < RESOURCE_THRESHOLD && agent.getProducingResource() != 2) {
            if (agent.isNegotiating()) { //Agent dies after negotiation
                agent.setDying(true);
                return;
            }
            removeAgent(agent);
            agentAges.get(agent.getOrder()).add(agent.getAge());
            deathCnt++;
        } else if (agent.getResourceFour() < RESOURCE_THRESHOLD && agent.getProducingResource() != 3) {
            if (agent.isNegotiating()) { //Agent dies after negotiation
                agent.setDying(true);
                return;
            }
            removeAgent(agent);
            agentAges.get(agent.getOrder()).add(agent.getAge());
            deathCnt++;
        } else {
            agent.reduceResources();
        }
    }

    /**
     * A method that creates a new agent based on a parent or through mutation
     *
     * @return Agent The newly created (mutated or descendant) agent
     */
    private Agent createNewAgent() {
        int epsilon = ThreadLocalRandom.current().nextInt(0, 100);
        Agent newAgent;

        if (epsilon >= MUTATION_CHANCE) {
            newAgent = createAgentFromParent();
        } else {
            newAgent = createMutatedAgent();
        }
        return newAgent;
    }

    /**
     * A method that creates a new agent by copying the ToM order from a parent agent
     *
     * @return Agent The new agent
     */
    private Agent createAgentFromParent() {
        Random r = new Random();
        updateAllAgents();
        Agent copyAgent = allAgents.get(r.nextInt(allAgents.size())); //Find parent
        Class<? extends Agent> agentClass = copyAgent.getClass();
        updateAllAgents();

        try {
            maxIndex++;
            Constructor<? extends Agent> constructor = agentClass.getDeclaredConstructor(int.class, File.class);
            Agent newAgent = constructor.newInstance(maxIndex - 1, this);
            newAgent.setInitialBeliefs(copyAgent.getInitialBeliefs()); //Copy initialBeliefs from parent
            newAgent.setOffersMade(copyAgent.getOffersMade());
            newAgent.setOffersAccepted(copyAgent.getOffersAccepted());
            return newAgent;

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) { //DEBUG
            System.out.println("new agent could not be created!");
            System.exit(0);
            return null;
        }
    }

    /**
     * A method that creates a new agent through mutation
     *
     * @return Agent The mutated agent
     */
    private Agent createMutatedAgent() {
        int toMOrder = ThreadLocalRandom.current().nextInt(0, 3);
        updateAllAgents();
        Random r = new Random();
        Agent copyAgent = allAgents.get(r.nextInt(allAgents.size())); //Find parent
        switch (toMOrder) { //New agent may be of extinct type
            case 0 -> {
                maxIndex++;
                ToM0Agent mutatedAgent = new ToM0Agent(maxIndex - 1, this);
                mutatedAgent.setInitialBeliefs(copyAgent.getInitialBeliefs());
                mutatedAgent.setOffersMade(copyAgent.getOffersMade());
                mutatedAgent.setOffersAccepted(copyAgent.getOffersAccepted());
                return mutatedAgent;
            }
            case 1 -> {
                maxIndex++;
                ToM1Agent mutatedAgent = new ToM1Agent(maxIndex - 1, this);
                mutatedAgent.setInitialBeliefs(copyAgent.getInitialBeliefs());
                mutatedAgent.setOffersMade(copyAgent.getOffersMade());
                mutatedAgent.setOffersAccepted(copyAgent.getOffersAccepted());
                return mutatedAgent;
            }
            case 2 -> {
                maxIndex++;
                ToM2Agent mutatedAgent = new ToM2Agent(maxIndex - 1, this);
                mutatedAgent.setInitialBeliefs(copyAgent.getInitialBeliefs());
                mutatedAgent.setOffersMade(copyAgent.getOffersMade());
                mutatedAgent.setOffersAccepted(copyAgent.getOffersAccepted());
                return mutatedAgent;
            }
        }
        System.exit(0);
        return null;
    }

    /**
     * A method that increments the timer
     */
    public void incrementTimer() {
        timer++;
        if (timer % 500 == 0) {
            System.out.println(timer);
        }
        if (GUI) {
            notifyTimerObservers();
        }
    }

    /**
     * A method that adds a new agent to the environment
     *
     * @param newAgent The agent that needs to be added
     */
    private void addAgentToEnvironment(Agent newAgent) {
        newAgent.setAge(0);
        if (newAgent instanceof ToM0Agent) {
            addToM0Agent((ToM0Agent) newAgent);
        } else if (newAgent instanceof ToM1Agent) {
            addToM1Agent((ToM1Agent) newAgent);
        } else if (newAgent instanceof ToM2Agent) {
            addToM2Agent((ToM2Agent) newAgent);
        }
    }


    /**
     * A method that adds a new agent to the environment because another one has died
     */
    private void addAgent() {
        updateAllAgents();
        if (allAgents.isEmpty()) {
            pauseSimulation();
            return;
        }
        Agent newAgent = createNewAgent();
        if (newAgent != null) {
            addAgentToEnvironment(newAgent);
        }
    }

    /**
     * A method that adds a ToM0 agent to the population
     *
     * @param toM0Agent The agent that needs to be added
     */
    public void addToM0Agent(ToM0Agent toM0Agent) {
        toM0Agents.add(toM0Agent);
    }

    /**
     * A method that adds a ToM1 agent to the population
     *
     * @param toM1Agent The agent that needs to be added
     */
    public synchronized void addToM1Agent(ToM1Agent toM1Agent) {
        toM1Agents.add(toM1Agent);
    }

    /**
     * A method that adds a ToM2 agent to the population
     *
     * @param toM2Agent The agent that needs to be added
     */
    public synchronized void addToM2Agent(ToM2Agent toM2Agent) {
        toM2Agents.add(toM2Agent);
    }

    /**
     * A method that moves an agent because it has died
     *
     * @param agent The agent that is to be replaced
     */
    private void removeAgent(Agent agent) {
        if (agent.getOrder() == 0) {
            removeToM0Agent((ToM0Agent) agent);
        }
        if (agent.getOrder() == 1) {
            removeToM1Agent((ToM1Agent) agent);
        }
        if (agent.getOrder() == 2) {
            removeToM2Agent((ToM2Agent) agent);
        }
    }

    /**
     * A method that removes a ToM0 agent from the population
     *
     * @param toM0Agent The agent that needs to be removed
     */
    public synchronized void removeToM0Agent(ToM0Agent toM0Agent) {
        toM0Agents.remove(toM0Agent);
    }

    /**
     * A method that removes a ToM1 agent from the population
     *
     * @param toM1Agent The agent that needs to be removed
     */
    public synchronized void removeToM1Agent(ToM1Agent toM1Agent) {
        toM1Agents.remove(toM1Agent);
    }

    /**
     * A method that removes a ToM2 agent from the population
     *
     * @param toM2Agent The agent that needs to be removed
     */
    public synchronized void removeToM2Agent(ToM2Agent toM2Agent) {
        toM2Agents.remove(toM2Agent);
    }

    // Getters

    /**
     * A method that returns the ToM0 agents
     *
     * @return toM0agents
     */
    public ArrayList<ToM0Agent> getToM0Agents() {
        return toM0Agents;
    }

    /**
     * A method that returns the ToM1 agents
     *
     * @return toM1agents
     */
    public ArrayList<ToM1Agent> getToM1Agents() {
        return toM1Agents;
    }

    /**
     * A method that returns the ToM2 agents
     *
     * @return toM2agents
     */
    public ArrayList<ToM2Agent> getToM2Agents() {
        return toM2Agents;
    }

    /**
     * A method that returns the timer
     *
     * @return timer The number of ticks since the start
     */
    public int getTimer() {
        return timer;
    }

    /**
     * A method that returns the history data
     *
     * @return historyData The list of history data
     */
    public List<int[]> getHistoryData() {
        return historyData;
    }

    /**
     * A method that returns the state of the experiment
     *
     * @return hasInitialized
     */
    public int getExpState() {
        return expState;
    }

    /**
     * A method that returns the experiment type (1 or 2)
     * @return expType
     */
    public int getExpType() {
        return expType;
    }

    /**
     * A method that returns the state of file
     *
     * @return isRunning
     */
    public boolean getState() {
        return isRunning;
    }


    // Observer handling

    /**
     * A method that adds an observer to this file
     *
     * @param observer The observing class
     */
    public void addObserver(FileObserver observer) {
        observers.add(observer);
    }

    /**
     * A method that notifies all observers that isRunning has been changed
     */
    private void notifyRunningObservers() {
        if (GUI) {
            for (FileObserver observer : observers) {
                observer.onStateChanged(isRunning);
            }
        }
    }

    /**
     * A method that notifies all observers that an agent has been added
     */
    private void notifyAgentsObservers() {
        if (GUI) {
            for (FileObserver observer : observers) {
                observer.onAgentsAdded();
            }
        }
    }

    /**
     * A method that notifies all observers that the timer has been changed
     */
    private void notifyTimerObservers() {
        if (GUI) {
            for (FileObserver observer : observers) {
                observer.onTimerChanged();
            }
        }
    }

    /**
     * A method that notifies all observers that the initialization state has been changed
     */
    private void notifyExpStateObservers() {
        if (GUI) {
            for (FileObserver observer : observers) {
                observer.onInitializationChanged(expState);
            }
        }
    }

}
