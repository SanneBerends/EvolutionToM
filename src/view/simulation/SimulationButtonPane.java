package view.simulation;

import controller.buttons.InitializeButton;
import controller.buttons.PauseButton;
import controller.buttons.StartButton;
import model.File;
import view.FileObserver;

import javax.swing.*;
import java.awt.*;

/**
 * The SimulationButtonPane class, which models the buttons of the simulation.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class SimulationButtonPane extends JPanel implements FileObserver {
    private final InitializeButton initializeButton;
    private final PauseButton pauseButton;
    private final StartButton startButton;

    /**
     * The constructor that creates a new SimulationButtonPane with all the necessary buttons.
     */
    public SimulationButtonPane(File file) {
        file.addObserver(this);
        setPreferredSize(new Dimension(700,100));
        setBackground(Color.black);

        initializeButton = new InitializeButton(file);
        pauseButton = new PauseButton(file);
        startButton = new StartButton(file);
        add(initializeButton);
        add(pauseButton);
        add(startButton);
        startButton.setVisible(false);
        pauseButton.setVisible(false);
    }


    /*
    Methods
     */

    /**
     * Method used to draws the background of this panel and the agents
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.darkGray);
    }

    /**
     * Method that specifies what changes to this pane when a change is observed in isRunning
     * @param newState boolean that specifies if there is a new state
     */
    @Override
    public void onStateChanged(boolean newState) {
        if (newState) {
            startButton.setVisible(false);
            pauseButton.setVisible(true);
        } else {
            startButton.setVisible(true);
            pauseButton.setVisible(false);
        }
    }

    @Override
    public void onInitializationChanged(int expState) {
        if (expState == -1 || expState == 2) {
            initializeButton.setVisible(false);
            startButton.setVisible(false);
            pauseButton.setVisible(false);
        } else if (expState == 0) {
            initializeButton.setVisible(true);
            startButton.setVisible(false);
            pauseButton.setVisible(false);
        } else if (expState == 1){
            startButton.setVisible(true);
            pauseButton.setVisible(false);
            initializeButton.setVisible(false);
        }
    }

    @Override
    public void onAgentsAdded() {
    }

    @Override
    public void onTimerChanged() {
    }
}

