package view.information;

import model.File;
import view.FileObserver;

import javax.swing.*;
import java.awt.*;

/**
 * The TimePane class, which models the pane with time info
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class TimePane extends JPanel implements FileObserver {
    private final File file;
    private final JLabel body;

    /**
     * The constructor that creates a sub-pane for the time pane
     */
    public TimePane(File file) {
        setPreferredSize(new Dimension(398,100));
        //setBackground(Color.red);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        this.file = file;
        file.addObserver(this);

        //Text
        body = new JLabel("<html><br>Ticks: "+ file.getTimer() + "</html>", SwingConstants.LEFT);
        Font bodyFont = new Font("Times", Font.PLAIN, 16);
        body.setFont(bodyFont);
        add(body);
    }
    /*
    Methods
     */

    /**
     * A method used to draw  this panel
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        body.setText("<html><br>Ticks: "+ file.getTimer() + "</html>");
    }

    @Override
    public void onStateChanged(boolean newState) {
    }

    @Override
    public void onAgentsAdded() {
    }

    @Override
    public void onTimerChanged() {
        this.repaint();
    }

    @Override
    public void onInitializationChanged(int hasInitialized) {
    }

}
