package view.information;

import model.File;

import javax.swing.*;
import java.awt.*;

/**
 * The OverviewPane class, which models the pane with ToM info
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class OverviewPane extends JPanel {
    /**
     * The constructor that creates a sub-pane for the ToM quantity pane
     */
    public OverviewPane(File file) {
        setPreferredSize(new Dimension(400,700));
        setBackground(Color.white);

        //Title
        JLabel title = new JLabel("Overview", SwingConstants.CENTER);
        Font titleFont = new Font("Times", Font.PLAIN, 20);
        title.setFont(titleFont);
        add(title);

        //Add time pane
        TimePane timePane = new TimePane(file);
        add(timePane);
        //Add ToM quantity pane
        ToMQuantityPane toMQuantityPane = new ToMQuantityPane(file);
        add(toMQuantityPane);
        //Add ToM quantity graph
        GraphPane graphPane = new GraphPane(file);
        add(graphPane);
    }

    /*
    Methods
     */

    /**
     * Method used to draw the background of this panel
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.white);
        g.setColor(Color.black);
        g.drawRect(0, 0, 400, 699);
    }
}
