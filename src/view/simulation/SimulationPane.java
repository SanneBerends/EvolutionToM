package view.simulation;

import model.File;

import javax.swing.*;
import java.awt.*;

/**
 * The SimulationPane class, which models the pane of the simulation.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class SimulationPane extends JPanel {
    /**
     * The constructor that creates a sub-pane for the simulation
     */
    public SimulationPane(File file) {
        //Create  pane for the simulation
        setPreferredSize(new Dimension(700,700));
        setBackground(Color.darkGray);

        //Add arena pane
        ArenaPane arenaPane = new ArenaPane(file);
        add(arenaPane);

        //Add buttons pane
        SimulationButtonPane simulationButtonPane = new SimulationButtonPane(file);
        add(simulationButtonPane);
    }
}
