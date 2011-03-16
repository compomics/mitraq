package no.uib.mitraq.util;

import java.util.ArrayList;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * A simple LabelGenerator for the ratio plot.
 *
 * @author Harald Barsnes
 */
public class CustomLabelGenerator extends StandardCategoryItemLabelGenerator {

    /**
     * The labels to use.
     */
    ArrayList<String> labels;

    /**
     * Sets up the label generator.
     *
     * @param labels the labels to use
     */
    public CustomLabelGenerator(ArrayList<String> labels) {
        this.labels = labels;
    }

    @Override
    public String generateLabel(CategoryDataset dataset, int row, int column) {

        if (labels != null && labels.size() > column) {
            return this.labels.get(column);
        } else {
            return null;
        }
    }
}
