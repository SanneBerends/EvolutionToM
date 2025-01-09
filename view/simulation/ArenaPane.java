package view.simulation;

import model.*;
import model.agents.*;
import view.FileObserver;

import javax.swing.*;
import java.awt.*;

/**
 * The ArenaPane class, which models the pane of the arena
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class ArenaPane extends JPanel implements FileObserver {
    private final File file;
    /**
     * The constructor that creates a sub-sub-pane for the arena
     */
    public ArenaPane(File file) {
        this.file = file;
        file.addObserver(this);
        setPreferredSize(new Dimension(600,600));
        setBackground(new Color(80, 200, 120));
    }

    /*
    Methods
     */

    /**
     * Method used to draw the arena
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(new Color(80, 200, 120));
        g.setColor(Color.black);
        g.drawRect(0, 0, 600, 600);
        drawToM0Agents(g);
        drawToM1Agents(g);
        drawToM2Agents(g);
    }

    /**
     * A method that draws an ToM0 agents (black dot)
     * @param g Graphics object needed for drawing
     */
    private void drawToM0Agents(Graphics g) {
        g.setColor(Color.black);
        if (!file.getToM0Agents().isEmpty()) {
            for (int i = 0; i< file.getToM0Agents().size();i++ ) {
                g.drawOval((int) file.getToM0Agents().get(i).getXLoc(), (int) file.getToM0Agents().get(i).getYLoc(), 2, 2);
            }
        }
    }

    /**
     * A method that draws an ToM1 agents (red dot)
     * @param g Graphics object needed for drawing
     */
    private void drawToM1Agents(Graphics g) {
        g.setColor(Color.red);
        if (!file.getToM1Agents().isEmpty()) {
            for (ToM1Agent toM1Agent : file.getToM1Agents()) {
                g.drawOval((int) toM1Agent.getXLoc(), (int) toM1Agent.getYLoc(), 2, 2);
            }
        }
    }

    /**
     * A method that draws an ToM2 agents (blue dot)
     * @param g Graphics object needed for drawing
     */
    private void drawToM2Agents(Graphics g) {
        g.setColor(Color.blue);
        if (!file.getToM2Agents().isEmpty()) {
            for (ToM2Agent toM2Agent : file.getToM2Agents()) {
                g.drawOval((int) toM2Agent.getXLoc(), (int) toM2Agent.getYLoc(), 2, 2);
            }
        }
    }

    @Override
    public void onStateChanged(boolean newState) {
    }

    @Override
    public void onAgentsAdded() {
        this.repaint();
    }

    @Override
    public void onTimerChanged() {
        this.repaint();
    }

    @Override
    public void onInitializationChanged(int hasInitialized) {
    }

}
