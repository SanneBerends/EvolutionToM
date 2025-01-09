package view;

import model.File;
import view.information.OverviewPane;
import view.simulation.SimulationPane;

import javax.swing.*;
import java.awt.*;

/**
 * The ProgramPanel class, which models panel of the GUI.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class ProgramPanel extends JPanel {
    /**
     * The constructor that creates a panel with a sub-pane for the simulation and ToM overview
     */
    public ProgramPanel(File file){
        setBackground(Color.lightGray);

        //Title
        JLabel title = new JLabel("Modeling the evolutionary development of ToM", SwingConstants.CENTER);
        Font heading = new Font("Times Roman", Font.BOLD, 24);
        title.setFont(heading);
        title.setPreferredSize(new Dimension(800, 50));
        title.setOpaque(false);
        add(title);

        //Add simulation pane
        SimulationPane simulationPane = new SimulationPane(file);
        add(simulationPane);

        //Add ToM overview pane
        OverviewPane overviewPane = new OverviewPane(file);
        add(overviewPane);
    }

    /*
    Methods
     */

    /**
     * Method used to draw the program panel
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.lightGray);
    }
}
