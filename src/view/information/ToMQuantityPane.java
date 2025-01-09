package view.information;

import model.File;
import view.FileObserver;

import javax.swing.*;
import java.awt.*;

/**
 * The ToMQuantityPane class, which models the pane with ToM quantity information
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class ToMQuantityPane extends JPanel implements FileObserver {
    private final File file;
    private final JLabel body;

    /**
     * The constructor that creates a sub-sub-pane for the ToM quantity pane
     */
    public ToMQuantityPane(File file) {
        setPreferredSize(new Dimension(398,200));
        //setBackground(Color.blue);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        this.file = file;
        file.addObserver(this);

        //Text
        body = new JLabel("<html><br>ToM0 (black): "+ file.getToM0Agents().size() + "<br> ToM1 (red): " + file.getToM1Agents().size() +
                "<br> ToM2 (blue): " + file.getToM2Agents().size() + "</html>", SwingConstants.LEFT);
        Font bodyFont = new Font("Times", Font.PLAIN, 16);
        body.setFont(bodyFont);
        add(body);
    }

    /*
    Methods
     */

    /**
     * A method used to draw the outline of this panel
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //setBackground(Color.blue);
        body.setText("<html><br>ToM0 (black): "+ file.getToM0Agents().size() + "<br> ToM1 (red): " + file.getToM1Agents().size() +
                "<br> ToM2 (blue): " + file.getToM2Agents().size() + "</html>");
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
    }

    @Override
    public void onInitializationChanged(int hasInitialized) {
    }
}
