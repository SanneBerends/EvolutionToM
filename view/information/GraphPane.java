package view.information;

import model.File;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import view.FileObserver;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * The GraphPane class, which models the pane with the ToM line graph
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class GraphPane extends JPanel implements FileObserver{
    private final File file;
    private final DefaultCategoryDataset dataset;

    /**
     * The constructor that creates a sub-sub-pane for the graph pane
     */
    public GraphPane(File file) {
        this.file = file;
        setPreferredSize(new Dimension(398,300));
        setBackground(Color.white);
        file.addObserver(this);

        // Create and add the line chart to this panel
        dataset = new DefaultCategoryDataset();
        addLineChart();
    }

    /*
    Methods
     */

    /**
     * Method to create and add the line chart to this panel
     */
    private void addLineChart() {
        // Create a line chart
        JFreeChart graph = ChartFactory.createLineChart("ToM evolution",
                "Time steps", "Quantity", dataset);

        CategoryPlot plot = (CategoryPlot) graph.getPlot();

        // Set custom colors for each series
        plot.getRenderer().setSeriesPaint(0, Color.black); // ToM0
        plot.getRenderer().setSeriesPaint(1, Color.red); // ToM1
        plot.getRenderer().setSeriesPaint(2, Color.blue); // ToM2

        // Disable ticks
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);

        ChartPanel chartPanel = new ChartPanel(graph);
        chartPanel.setPreferredSize(new Dimension(398, 300));
        chartPanel.setBackground(Color.white); // Set chart panel background color
        add(chartPanel);
    }
    /**
     * Method used to draw and update this panel
     * @param g Graphics object needed for drawing
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (file.getState() && (file.getExpState()==1 || file.getExpState()==-1)) {
            adjustLineChart();
        }
    }

    /**
     * A method that changes the content of the graph
     */
    private void adjustLineChart() {
        //Update the dataset (first clear it)
        dataset.clear();
        int totalSteps = file.getTimer();
        int startIndex = totalSteps > 100 ? totalSteps - 100 : 0;
        for (int i = startIndex; i <= totalSteps; i++) {
            if (file.getHistoryData() != null) {
                int[] agentCounts = file.getHistoryData().get(i);
                dataset.addValue((Number) agentCounts[0], "ToM0", i);
                dataset.addValue((Number) agentCounts[1], "ToM1", i);
                dataset.addValue((Number) agentCounts[2], "ToM2", i);
            }
        }
    }

    @Override
    public void onStateChanged(boolean newState) {
    }

    /**
     * A method that calls the drawing method when agents were added
     */
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
