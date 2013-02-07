package no.uib.mitraq.gui;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import no.uib.mitraq.util.Protein;
import no.uib.mitraq.util.BareBonesBrowserLaunch;
import no.uib.mitraq.util.TsvFileFilter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import no.uib.jexpress_modularized.TestPanelJFrame;
import no.uib.jexpress_modularized.core.model.DataSet;
import no.uib.jexpress_modularized.core.model.Selection;
import no.uib.jexpress_modularized.core.model.SelectionChangeListener;
import no.uib.jexpress_modularized.core.model.SelectionManager;
import no.uib.jexpress_modularized.somclust.computation.ClusterParameters;
import no.uib.jexpress_modularized.somclust.computation.ClusterResults;
import no.uib.jexpress_modularized.somclust.computation.SOMClustCompute;
import no.uib.jexpress_modularized.somclust.visualization.HierarchicalClusteringPanel;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.extra.HtmlLinksRenderer;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.util.BarChartColorRenderer;
import no.uib.jsparklines.renderers.util.StatisticalBarChartColorRenderer;
import no.uib.mitraq.util.CustomLabelGenerator;
import no.uib.mitraq.util.Util;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.inference.TestUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 * MiTRAQ main frame.
 *
 * @author Harald Barsnes
 */
public class MiTRAQ extends javax.swing.JFrame implements ProgressDialogParent, SelectionChangeListener {

    /**
     * The clustering dataset.
     */
    private DataSet dataset;
    /**
     * Number of members in group A.
     */
    private int groupANumberOfMembers = 0;
    /**
     * Number of members in group B.
     */
    private int groupBNumberOfMembers = 0;
    /**
     * The default line width for the line plots.
     */
    public static final float LINE_WIDTH = 4;

    /**
     * Turns of the gradient painting for the bar charts.
     */
    static {
        XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter());
    }
    /**
     * The progress dialog.
     */
    private ProgressDialog progressDialog;
    /**
     * The last selected folder.
     */
    private String lastSelectedFolder = "user.home";
    /**
     * The fold change plot.
     */
    private XYPlot foldChangeplot;
    /**
     * Arraylist of the currently selected proteins, i.e., the ones that have
     * been manually validated and checked in the last column.
     */
    private ArrayList<String> selectedProteins;
    /**
     * Arraylist of the currently filtered proteins, i.e., not used in the
     * analysis.
     */
    private ArrayList<String> removedProteins;
    /**
     * If true the ratio plot is shown as lines as opposed to bars of not
     * selected.
     */
    private boolean showRatioPlotAsLines = false;
    /**
     * The minimum number of peptides for a ratio to be used.
     */
    private int minNumUniquePeptides = 1;
    /**
     * The minimum number of spectra for a ratio to be used.
     */
    private int minNumUniqueSpectra = 1;
    /**
     * The minimum number of experiemtns a protein has to be found in for a
     * ratio to be used.
     */
    private int minNumberOfExperiments = 1;
    /**
     * The current iTRAQReference.
     */
    private String currentITraqReference;
    /**
     * The current number of experiments.
     */
    private Integer numberOfExperiments;
    /**
     * The experimental design table.
     */
    private JTable experimentalDesignJTable;
    /**
     * The current iTRAQ type (4-plex or 8-plex).
     */
    private String currentITraqType;
    /**
     * If true, the ratio plot will use the log 2 values, otherwise the normal
     * non-log values will be used.
     */
    private boolean useRatioLog2 = true;
    /**
     * Set to true if using the old data output format.
     */
    private boolean oldDataFormat = false;
    /**
     * The significance level to use for the t-test.
     */
    private double equallyExpressedSignificanceLevel = 0.05;
    /**
     * t-test scoring higher than this value are considered differentially
     * expressed and just in the q-value calculation.
     */
    private double differentiallyExpressedSignificanceLevel = 0.05;
    /**
     * The list of all proteins to be used in the analysis.
     */
    private ArrayList<Protein> allValidProteins;
    /**
     * The label used for group A.
     */
    private String groupALabel;
    /**
     * The label used for group A.
     */
    private String groupBLabel;
    /**
     * The color to use for group A.
     */
    private Color groupAColor = Color.RED;
    /**
     * The color to use for group A.
     */
    private Color groupBColor = Color.BLUE;
    /**
     * The current file the ratios are extracted from.
     */
    private String currentRatioFile = null;
    /**
     * The current text filter values.
     */
    private String[] currentFilterValues = {"", "", "", "", "", "", "", "", ""};
    /**
     * The current settings for the radio buttons for the filters.
     */
    private Integer[] currrentFilterRadioButtonSelections = {0, 0, 0, 0, 0, 2, 2};
    /**
     * Set if the row filter is to take the absolute value of the fold change.
     */
    private boolean foldChangeAbsoluteValue = true;
    /**
     * The results table column header tips.
     */
    private Vector columnHeaderToolTips;
    /**
     * If true the cells in the results table are shown as numbers, false
     * displays bar charts.
     */
    private boolean showSparklines = false;
    /**
     * If true the values and the bar charts are shown in the cells, false
     * displays the bar charts only.
     */
    private boolean showValuesAndCharts = true;
    /**
     * If true error bars are shown for the average value bar in the plot.
     */
    private boolean showErrorBars = true;
    /**
     * If true background hightlighting is added to the average value bars.
     */
    private boolean highlightAverageBars = false;
    /**
     * If true each bar in the bar chart will be labelled with its value, if
     * false the label will be the number of peptides or spectra.
     */
    private boolean showBarChartLabelsAsRatios = true;
    /**
     * If true the bar chart labels will be shown.
     */
    private boolean showBarChartLabels = true;
    /**
     * The current chart panel.
     */
    private ChartPanel ratioChartPanel = null;
    /**
     * The current fold change chart panel.
     */
    private ChartPanel foldChangeChartPanel = null;
    /**
     * The color to use for the HTML tags for the selected rows, in HTML color
     * code.
     */
    public String selectedRowHtmlTagFontColor = "#FFFFFF";
    /**
     * The color to use for the HTML tags for the rows that are not selected, in
     * HTML color code.
     */
    public String notSelectedRowHtmlTagFontColor = "#0101DF";
    /**
     * If set to true all messages will be sent to a log file.
     */
    private static boolean useLogFile = true;

    /**
     * Sets up the MiTRAQ main frame.
     */
    public MiTRAQ() {

        // set up the log file
        setUpLogFile();

        initComponents();

        this.setTitle(this.getTitle() + " - v" + getVersion() + " beta");   // @TODO: remove beta when releasing v1.0
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/MiTRAQ.gif")));

        // set the result table details
        setUpResultsTable();
        setExtendedState(MAXIMIZED_BOTH);

        setLocationRelativeTo(null);
    }

    /**
     * Sets up the results table.
     */
    private void setUpResultsTable() {
        
        // correct the color for the upper right corner
        JPanel proteinCorner = new JPanel();
        proteinCorner.setBackground(resultsJTable.getTableHeader().getBackground());
        resultsTableJScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, proteinCorner);

        resultsTableJScrollPane.getViewport().setOpaque(false);

        // sparklines cell renderers
        resultsJTable.getColumn("FC").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                PlotOrientation.HORIZONTAL, -1.0, 1.0, groupBColor, groupAColor, Color.GRAY, new Double(significanceLevelJSpinner.getValue().toString())));
        resultsJTable.getColumn("Peptides").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, true));
        resultsJTable.getColumn("Coverage").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, true));
        resultsJTable.getColumn("Exp. Count").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, true));
        resultsJTable.getColumn("Quant. Count").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, true));
        resultsJTable.getColumn("p-value").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, false));
        resultsJTable.getColumn("q-value").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, false));
        resultsJTable.getColumn("Accession").setCellRenderer(new HtmlLinksRenderer(selectedRowHtmlTagFontColor, notSelectedRowHtmlTagFontColor));

        // add the true/false cell renderer
        resultsJTable.getColumn("Significant").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")), new ImageIcon(this.getClass().getResource("/icons/Error_3.png"))));
        resultsJTable.getColumn("Bonferroni").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")), new ImageIcon(this.getClass().getResource("/icons/Error_3.png"))));

        // required cell renderer for checkbox columns in Nimbus
        resultsJTable.getColumn("  ").setCellRenderer(new NimbusCheckBoxRenderer());

        // turn off column reordering
        resultsJTable.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultsJTable.getModel());
        resultsJTable.setRowSorter(sorter);

        // set the widths of the columns
        resultsJTable.getColumn(" ").setMaxWidth(40);
        resultsJTable.getColumn(" ").setMinWidth(40);
        resultsJTable.getColumn("  ").setMaxWidth(40);
        resultsJTable.getColumn("  ").setMinWidth(40);
        resultsJTable.getColumn("Protein").setMinWidth(300);

        // set the column header tooltips
        columnHeaderToolTips = new Vector();
        columnHeaderToolTips.add(null);
        columnHeaderToolTips.add("Protein Description");
        columnHeaderToolTips.add("Protein Accession Number");
        columnHeaderToolTips.add("Fold Change - Group 1 / Group 2");
        columnHeaderToolTips.add("Number of Unique Peptides");
        columnHeaderToolTips.add("Sequence Coverage");
        columnHeaderToolTips.add("Experiment Identification Counter");
        columnHeaderToolTips.add("Quantification Ratio Counter");
        columnHeaderToolTips.add("p-value for t-test");
        columnHeaderToolTips.add("q-value");
        columnHeaderToolTips.add("Significant/Not Significant t-test");
        columnHeaderToolTips.add("Significant/Not Significant t-test - Bonferroni Corrected");
        columnHeaderToolTips.add("Manually Validated");
    }

    /**
     * Set up the log file.
     */
    private void setUpLogFile() {
        if (useLogFile && !getJarFilePath().equalsIgnoreCase(".")) {
            try {
                String path = getJarFilePath() + "/resources/MiTRAQ.log";

                File file = new File(path);
                System.setOut(new java.io.PrintStream(new FileOutputStream(file, true)));
                System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));

                // creates a new log file if it does not exist
                if (!file.exists()) {
                    file.createNewFile();

                    FileWriter w = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(w);

                    bw.close();
                    w.close();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null, "An error occured when trying to create MiTRAQ.log.",
                        "Error Creating Log File", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the last selected folder.
     *
     * @param lastSelectedFolder
     */
    public void setLastSelectedFolder(String lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
    }

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public String getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        String path = this.getClass().getResource("MiTRAQ.class").getPath();

        if (path.lastIndexOf("/MiTRAQ-") != -1) {
            // remove starting 'file:' tag if there
            if (path.startsWith("file:")) {
                path = path.substring("file:".length(), path.lastIndexOf("/MiTRAQ-"));
            } else {
                path = path.substring(0, path.lastIndexOf("/MiTRAQ-"));
            }
            path = path.replace("%20", " ");
        } else {
            path = ".";
        }

        return path;
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of MiTRAQ
     */
    private String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("mitraq.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("mitraq.version");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelButtonGroup = new javax.swing.ButtonGroup();
        removeJPopupMenu = new javax.swing.JPopupMenu();
        removeJMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        resultsJPanel = new javax.swing.JPanel();
        accessiobNumbersJScrollPane = new javax.swing.JScrollPane();
        accessionNumbersJEditorPane = new javax.swing.JEditorPane();
        resultsJSplitPane = new javax.swing.JSplitPane();
        resultsTableJPanel = new javax.swing.JPanel();
        resultsTableJScrollPane = new javax.swing.JScrollPane();
        resultsJTable = new JTable() {
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        tip = (String) columnHeaderToolTips.get(realIndex);
                        return tip;
                    }
                };
            }
        };
        significanceLevelJLabel = new javax.swing.JLabel();
        significanceLevelJSpinner = new javax.swing.JSpinner();
        filterResultsJButton = new javax.swing.JButton();
        exportProteinListJButton = new javax.swing.JButton();
        clearFilterResultsJButton = new javax.swing.JButton();
        hcButton = new javax.swing.JButton();
        chartsJPanel = new javax.swing.JPanel();
        chartsJSplitPane = new javax.swing.JSplitPane();
        ratioChartJPanel = new javax.swing.JPanel();
        foldChangeChartJPanel = new javax.swing.JPanel();
        exportPlotJButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileJMenu = new javax.swing.JMenu();
        openJMenuItem = new javax.swing.JMenuItem();
        saveJMenuItem = new javax.swing.JMenuItem();
        saveAsJMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        exitJMenuItem = new javax.swing.JMenuItem();
        editJMenu = new javax.swing.JMenu();
        preferencesJMenuItem = new javax.swing.JMenuItem();
        removedJMenuItem = new javax.swing.JMenuItem();
        filterJMenuItem = new javax.swing.JMenuItem();
        exportJMenu = new javax.swing.JMenu();
        exportAllPlotsJMenuItem = new javax.swing.JMenuItem();
        viewJMenu = new javax.swing.JMenu();
        viewSparklinesJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        valuesAndChartJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        errorBarsJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        highlightAveragesJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        ratioLogJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        labelsJMenu = new javax.swing.JMenu();
        ratioLabelJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        peptideAndSpectraJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        noLabelJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        linesJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        foldChangePlotJMenu = new javax.swing.JMenu();
        currentProteinsJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        sd1JCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        sd2JCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpJMenu = new javax.swing.JMenu();
        helpJMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        removeJMenuItem.setText("Remove Protein(s)");
        removeJMenuItem.setToolTipText("Remove selected protein(s) from consideration");
        removeJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeJMenuItemActionPerformed(evt);
            }
        });
        removeJPopupMenu.add(removeJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MiTRAQ - Multiple iTRAQ Data Analysis");
        setMinimumSize(new java.awt.Dimension(800, 700));

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        resultsJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results"));
        resultsJPanel.setOpaque(false);

        accessionNumbersJEditorPane.setContentType("text/html");
        accessionNumbersJEditorPane.setEditable(false);
        accessionNumbersJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                accessionNumbersJEditorPaneHyperlinkUpdate(evt);
            }
        });
        accessiobNumbersJScrollPane.setViewportView(accessionNumbersJEditorPane);

        resultsJSplitPane.setBorder(null);
        resultsJSplitPane.setDividerLocation(350);
        resultsJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        resultsJSplitPane.setResizeWeight(1.0);
        resultsJSplitPane.setOpaque(false);

        resultsTableJPanel.setOpaque(false);

        resultsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein", "Accession", "FC", "Peptides", "Coverage", "Exp. Count", "Quant. Count", "p-value", "q-value", "Significant", "Bonferroni", "  "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, XYDataPoint.class, java.lang.Integer.class, java.lang.Integer.class,
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class,
                java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsJTable.setOpaque(false);
        resultsJTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                resultsJTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultsJTableMouseReleased(evt);
            }
        });
        resultsJTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                resultsJTableMouseMoved(evt);
            }
        });
        resultsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resultsJTableKeyReleased(evt);
            }
        });
        resultsTableJScrollPane.setViewportView(resultsJTable);

        significanceLevelJLabel.setFont(significanceLevelJLabel.getFont().deriveFont((significanceLevelJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        significanceLevelJLabel.setText("Significance Level:");
        significanceLevelJLabel.setToolTipText("Significance Level Used for the t-test");

        significanceLevelJSpinner.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 1.0d, 0.01d));
        significanceLevelJSpinner.setToolTipText("Significance Level Used for the t-test");
        significanceLevelJSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                significanceLevelJSpinnerStateChanged(evt);
            }
        });

        filterResultsJButton.setText("Filter");
        filterResultsJButton.setToolTipText("Filter the Protein Results");
        filterResultsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterResultsJButtonActionPerformed(evt);
            }
        });

        exportProteinListJButton.setText("Export");
        exportProteinListJButton.setToolTipText("Export Protein Results to CSV File");
        exportProteinListJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportProteinListJButtonActionPerformed(evt);
            }
        });

        clearFilterResultsJButton.setText("Clear");
        clearFilterResultsJButton.setToolTipText("Clear the Filter of the Protein Results");
        clearFilterResultsJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterResultsJButtonActionPerformed(evt);
            }
        });

        hcButton.setText("HC");
        hcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hcButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout resultsTableJPanelLayout = new javax.swing.GroupLayout(resultsTableJPanel);
        resultsTableJPanel.setLayout(resultsTableJPanelLayout);
        resultsTableJPanelLayout.setHorizontalGroup(
            resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsTableJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(significanceLevelJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(significanceLevelJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(hcButton)
                .addGap(18, 18, 18)
                .addComponent(filterResultsJButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterResultsJButton)
                .addGap(18, 18, 18)
                .addComponent(exportProteinListJButton)
                .addContainerGap())
            .addComponent(resultsTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1153, Short.MAX_VALUE)
        );

        resultsTableJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearFilterResultsJButton, exportProteinListJButton, filterResultsJButton});

        resultsTableJPanelLayout.setVerticalGroup(
            resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsTableJPanelLayout.createSequentialGroup()
                .addComponent(resultsTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(significanceLevelJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(significanceLevelJLabel)
                    .addComponent(exportProteinListJButton)
                    .addComponent(filterResultsJButton)
                    .addComponent(clearFilterResultsJButton)
                    .addComponent(hcButton))
                .addContainerGap())
        );

        resultsJSplitPane.setTopComponent(resultsTableJPanel);

        chartsJPanel.setMaximumSize(new java.awt.Dimension(4, 200));
        chartsJPanel.setOpaque(false);

        chartsJSplitPane.setBorder(null);
        chartsJSplitPane.setDividerLocation(700);
        chartsJSplitPane.setResizeWeight(0.75);
        chartsJSplitPane.setOpaque(false);

        ratioChartJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Ratio Plot"));
        ratioChartJPanel.setOpaque(false);
        ratioChartJPanel.setLayout(new javax.swing.BoxLayout(ratioChartJPanel, javax.swing.BoxLayout.LINE_AXIS));
        chartsJSplitPane.setLeftComponent(ratioChartJPanel);

        foldChangeChartJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Fold Change Plot"));
        foldChangeChartJPanel.setOpaque(false);
        foldChangeChartJPanel.setLayout(new javax.swing.BoxLayout(foldChangeChartJPanel, javax.swing.BoxLayout.LINE_AXIS));
        chartsJSplitPane.setRightComponent(foldChangeChartJPanel);

        javax.swing.GroupLayout chartsJPanelLayout = new javax.swing.GroupLayout(chartsJPanel);
        chartsJPanel.setLayout(chartsJPanelLayout);
        chartsJPanelLayout.setHorizontalGroup(
            chartsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartsJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1153, Short.MAX_VALUE)
        );
        chartsJPanelLayout.setVerticalGroup(
            chartsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chartsJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
        );

        resultsJSplitPane.setRightComponent(chartsJPanel);

        exportPlotJButton.setText("<html> <p align=center> Export<br>Plots</p> </html>");
        exportPlotJButton.setToolTipText("Export the selected plots to file");
        exportPlotJButton.setEnabled(false);
        exportPlotJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPlotJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout resultsJPanelLayout = new javax.swing.GroupLayout(resultsJPanel);
        resultsJPanel.setLayout(resultsJPanelLayout);
        resultsJPanelLayout.setHorizontalGroup(
            resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultsJSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsJPanelLayout.createSequentialGroup()
                        .addComponent(accessiobNumbersJScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPlotJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        resultsJPanelLayout.setVerticalGroup(
            resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(accessiobNumbersJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportPlotJButton))
                .addContainerGap())
        );

        resultsJPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {accessiobNumbersJScrollPane, exportPlotJButton});

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        fileJMenu.setMnemonic('F');
        fileJMenu.setText("File");

        openJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openJMenuItem.setMnemonic('O');
        openJMenuItem.setText("Open");
        openJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openJMenuItemActionPerformed(evt);
            }
        });
        fileJMenu.add(openJMenuItem);

        saveJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveJMenuItem.setMnemonic('S');
        saveJMenuItem.setText("Save");
        saveJMenuItem.setToolTipText("Save the selections and settings");
        saveJMenuItem.setEnabled(false);
        saveJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveJMenuItemActionPerformed(evt);
            }
        });
        fileJMenu.add(saveJMenuItem);

        saveAsJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsJMenuItem.setMnemonic('S');
        saveAsJMenuItem.setText("Save As");
        saveAsJMenuItem.setToolTipText("Save the selections and settings");
        saveAsJMenuItem.setEnabled(false);
        saveAsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsJMenuItemActionPerformed(evt);
            }
        });
        fileJMenu.add(saveAsJMenuItem);
        fileJMenu.add(jSeparator3);

        exitJMenuItem.setText("Exit");
        exitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitJMenuItemActionPerformed(evt);
            }
        });
        fileJMenu.add(exitJMenuItem);

        menuBar.add(fileJMenu);

        editJMenu.setMnemonic('E');
        editJMenu.setText("Edit");

        preferencesJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        preferencesJMenuItem.setMnemonic('P');
        preferencesJMenuItem.setText("Preferences");
        preferencesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesJMenuItemActionPerformed(evt);
            }
        });
        editJMenu.add(preferencesJMenuItem);

        removedJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        removedJMenuItem.setText("Removed Proteins");
        removedJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removedJMenuItemActionPerformed(evt);
            }
        });
        editJMenu.add(removedJMenuItem);

        filterJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        filterJMenuItem.setText("Filter Proteins");
        filterJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterJMenuItemActionPerformed(evt);
            }
        });
        editJMenu.add(filterJMenuItem);

        menuBar.add(editJMenu);

        exportJMenu.setMnemonic('X');
        exportJMenu.setText("Export");

        exportAllPlotsJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        exportAllPlotsJMenuItem.setMnemonic('E');
        exportAllPlotsJMenuItem.setText("Export All Selected Plots");
        exportAllPlotsJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAllPlotsJMenuItemActionPerformed(evt);
            }
        });
        exportJMenu.add(exportAllPlotsJMenuItem);

        menuBar.add(exportJMenu);

        viewJMenu.setMnemonic('V');
        viewJMenu.setText("View");

        viewSparklinesJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        viewSparklinesJCheckBoxMenuItem.setMnemonic('L');
        viewSparklinesJCheckBoxMenuItem.setSelected(true);
        viewSparklinesJCheckBoxMenuItem.setText("JSparklines");
        viewSparklinesJCheckBoxMenuItem.setToolTipText("View the results as sparklines or numbers");
        viewSparklinesJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSparklinesJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(viewSparklinesJCheckBoxMenuItem);

        valuesAndChartJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        valuesAndChartJCheckBoxMenuItem.setMnemonic('B');
        valuesAndChartJCheckBoxMenuItem.setSelected(true);
        valuesAndChartJCheckBoxMenuItem.setText("JSparklines & Values");
        valuesAndChartJCheckBoxMenuItem.setToolTipText("Show the values and the bar charts");
        valuesAndChartJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valuesAndChartJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(valuesAndChartJCheckBoxMenuItem);
        viewJMenu.add(jSeparator1);

        errorBarsJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        errorBarsJCheckBoxMenuItem.setMnemonic('E');
        errorBarsJCheckBoxMenuItem.setSelected(true);
        errorBarsJCheckBoxMenuItem.setText("Error Bars");
        errorBarsJCheckBoxMenuItem.setToolTipText("Show the error bars for the average value");
        errorBarsJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorBarsJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(errorBarsJCheckBoxMenuItem);

        highlightAveragesJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        highlightAveragesJCheckBoxMenuItem.setMnemonic('H');
        highlightAveragesJCheckBoxMenuItem.setText("Highlight Averages");
        highlightAveragesJCheckBoxMenuItem.setToolTipText("Adds background hightlighting for the average value bars");
        highlightAveragesJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightAveragesJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(highlightAveragesJCheckBoxMenuItem);

        ratioLogJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ratioLogJCheckBoxMenuItem.setMnemonic('L');
        ratioLogJCheckBoxMenuItem.setSelected(true);
        ratioLogJCheckBoxMenuItem.setText("Ratios as Log");
        ratioLogJCheckBoxMenuItem.setToolTipText("Show the rartios as log values");
        ratioLogJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratioLogJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(ratioLogJCheckBoxMenuItem);

        labelsJMenu.setMnemonic('A');
        labelsJMenu.setText("Ratio Plot Labels");

        ratioLabelJRadioButtonMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        labelButtonGroup.add(ratioLabelJRadioButtonMenuItem);
        ratioLabelJRadioButtonMenuItem.setMnemonic('R');
        ratioLabelJRadioButtonMenuItem.setSelected(true);
        ratioLabelJRadioButtonMenuItem.setText("Ratios");
        ratioLabelJRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratioLabelJRadioButtonMenuItemActionPerformed(evt);
            }
        });
        labelsJMenu.add(ratioLabelJRadioButtonMenuItem);

        peptideAndSpectraJRadioButtonMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        labelButtonGroup.add(peptideAndSpectraJRadioButtonMenuItem);
        peptideAndSpectraJRadioButtonMenuItem.setMnemonic('P');
        peptideAndSpectraJRadioButtonMenuItem.setText("Peptides & Spectra");
        peptideAndSpectraJRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                peptideAndSpectraJRadioButtonMenuItemActionPerformed(evt);
            }
        });
        labelsJMenu.add(peptideAndSpectraJRadioButtonMenuItem);

        noLabelJRadioButtonMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        labelButtonGroup.add(noLabelJRadioButtonMenuItem);
        noLabelJRadioButtonMenuItem.setMnemonic('N');
        noLabelJRadioButtonMenuItem.setText("No Labels");
        noLabelJRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noLabelJRadioButtonMenuItemActionPerformed(evt);
            }
        });
        labelsJMenu.add(noLabelJRadioButtonMenuItem);

        viewJMenu.add(labelsJMenu);
        viewJMenu.add(jSeparator2);

        linesJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        linesJCheckBoxMenuItem.setMnemonic('L');
        linesJCheckBoxMenuItem.setText("Line Plot Type");
        linesJCheckBoxMenuItem.setToolTipText("Show the rartio plot as a line or bar chart");
        linesJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linesJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(linesJCheckBoxMenuItem);
        viewJMenu.add(jSeparator4);

        foldChangePlotJMenu.setText("Fold Change Plot");

        currentProteinsJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        currentProteinsJCheckBoxMenuItem.setSelected(true);
        currentProteinsJCheckBoxMenuItem.setText("Current Proteins");
        currentProteinsJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentProteinsJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        foldChangePlotJMenu.add(currentProteinsJCheckBoxMenuItem);

        sd1JCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        sd1JCheckBoxMenuItem.setText("1 SD");
        sd1JCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sd1JCheckBoxMenuItemActionPerformed(evt);
            }
        });
        foldChangePlotJMenu.add(sd1JCheckBoxMenuItem);

        sd2JCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        sd2JCheckBoxMenuItem.setSelected(true);
        sd2JCheckBoxMenuItem.setText("2 SD");
        sd2JCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sd2JCheckBoxMenuItemActionPerformed(evt);
            }
        });
        foldChangePlotJMenu.add(sd2JCheckBoxMenuItem);

        viewJMenu.add(foldChangePlotJMenu);

        menuBar.add(viewJMenu);

        helpJMenu.setMnemonic('H');
        helpJMenu.setText("Help");

        helpJMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpJMenuItem.setMnemonic('H');
        helpJMenuItem.setText("Help");
        helpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpJMenuItemActionPerformed(evt);
            }
        });
        helpJMenu.add(helpJMenuItem);

        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpJMenu.add(aboutMenuItem);

        menuBar.add(helpJMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the results table selection.
     */
    public void updateResultTableSelection() {

        if (resultsJTable.getRowCount() > 0) {
            resultsJTableMouseReleased(null);
        } else {
            ratioChartJPanel.removeAll();

            // remove old fold change interval markers
            removeFoldChangeMarkers();

            // remove old data point annotations
            removeDataPointAnnotations();

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    foldChangeChartJPanel.repaint();
                    ratioChartJPanel.repaint();
                }
            });
        }

        ((TitledBorder) resultsJPanel.getBorder()).setTitle("Results (" + resultsJTable.getRowCount() + ")");
        resultsJPanel.revalidate();
        resultsJPanel.repaint();
    }

    /**
     * Updates the ratio plot according to the currently selected row in the
     * protein table.
     *
     * @param evt
     */
    private void resultsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultsJTableKeyReleased
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_resultsJTableKeyReleased

    /**
     * Opens a dialog with the results filter options.
     *
     * @param evt
     */
    private void filterResultsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterResultsJButtonActionPerformed
        new ResultsFilter(this, false, currentFilterValues, currrentFilterRadioButtonSelections, foldChangeAbsoluteValue, true);
}//GEN-LAST:event_filterResultsJButtonActionPerformed

    /**
     * Tries to open the accession number link in the default web browser.
     *
     * @param evt
     */
    private void accessionNumbersJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_accessionNumbersJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            BareBonesBrowserLaunch.openURL(evt.getDescription());
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_accessionNumbersJEditorPaneHyperlinkUpdate

    /**
     * Opens the experimental design dialog to open a new project.
     *
     * @param evt
     */
    private void openJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openJMenuItemActionPerformed
        new ExperimentalDesign(this, true, currentRatioFile);
    }//GEN-LAST:event_openJMenuItemActionPerformed

    /**
     * Closes the tool.
     *
     * @param evt
     */
    private void exitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitJMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitJMenuItemActionPerformed

    /**
     * Opens the Help dialog.
     *
     * @param evt
     */
    private void helpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpJMenuItemActionPerformed
        new HelpWindow(this, getClass().getResource("/helpFiles/MiTRAQ.html"));
    }//GEN-LAST:event_helpJMenuItemActionPerformed

    /**
     * Opens the About MiTRAQ dialog.
     *
     * @param evt
     */
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new HelpWindow(this, getClass().getResource("/helpFiles/AboutMiTRAQ.html"));
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * Updated the significance tests in the results table according to the
     * currently selected significance level. And update the color coding in the
     * fold change plot.
     *
     * @param evt
     */
    private void significanceLevelJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_significanceLevelJSpinnerStateChanged
        equallyExpressedSignificanceLevel = new Double(significanceLevelJSpinner.getValue().toString());
        differentiallyExpressedSignificanceLevel = new Double(significanceLevelJSpinner.getValue().toString());

        for (int i = 0; i < ((DefaultTableModel) resultsJTable.getModel()).getRowCount(); i++) {
            double pValue = (Double) ((DefaultTableModel) resultsJTable.getModel()).getValueAt(i, resultsJTable.getColumn("p-value").getModelIndex());
            ((DefaultTableModel) resultsJTable.getModel()).setValueAt(pValue < equallyExpressedSignificanceLevel, i, resultsJTable.getColumn("Significant").getModelIndex());
            ((DefaultTableModel) resultsJTable.getModel()).setValueAt(pValue < equallyExpressedSignificanceLevel / allValidProteins.size(), i, resultsJTable.getColumn("Bonferroni").getModelIndex());
        }

        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setSignificanceLevel(differentiallyExpressedSignificanceLevel);

        resultsJTable.revalidate();
        resultsJTable.repaint();
    }//GEN-LAST:event_significanceLevelJSpinnerStateChanged

    /**
     * Tries to export the currently shown proteins in the results table to a
     * tab separated text file.
     *
     * @param evt
     */
    private void exportProteinListJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportProteinListJButtonActionPerformed

        int option = JOptionPane.showConfirmDialog(this, "Export only selected proteins?", "Export Selected?", JOptionPane.YES_NO_OPTION);

        boolean exportSelected = (option == JOptionPane.YES_OPTION);

        JFileChooser chooser = new JFileChooser(currentRatioFile);
        chooser.setDialogTitle("Select the Export File");
        chooser.setSelectedFile(new File(currentRatioFile));
        chooser.setFileFilter(new TsvFileFilter());

        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = (chooser.getSelectedFile().getAbsoluteFile().getPath());

            if (!path.endsWith(".tsv")) {
                path = path + ".tsv";
            }

            boolean export = true;

            if (new File(path).exists()) {
                int value = JOptionPane.showConfirmDialog(this, "The file already exists. Overwrite?", "Overwrite?",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                export = value == JOptionPane.YES_OPTION;
            }

            if (export) {

                try {
                    FileWriter f = new FileWriter(path);
                    BufferedWriter w = new BufferedWriter(f);

                    // add the current filter settings at the top of the file
                    addFilterSettings(w);

                    if (exportSelected) {
                        w.write("Note that only the selected proteins were exported.");
                    }

                    w.write("\nIndex\tProtein Description\tAccession Number\tAccession Numbers\tUnique Peptides\t"
                            + "Coverage\tExperiment Counter\tFold Change\tp-value\tq-value\tSignificant\tBonferroni\t");

                    if (resultsJTable.getRowCount() > 0) {

                        int index = new Integer("" + resultsJTable.getValueAt(0, 0)) - 1;
                        Protein firstProtein = allValidProteins.get(index);

                        for (int i = 0; i < firstProtein.getRatiosGroupA().size(); i++) {
                            if (useRatioLog2) {
                                w.write("Ratio Log2 " + groupALabel + (i + 1) + "\t");
                            } else {
                                w.write("Ratio " + groupALabel + (i + 1) + "\t");
                            }
                        }

                        for (int i = 0; i < firstProtein.getRatiosGroupB().size(); i++) {
                            if (useRatioLog2) {
                                w.write("Ratio Log2 " + groupBLabel + (i + 1) + "\t");
                            } else {
                                w.write("Ratio " + groupBLabel + (i + 1) + "\t");
                            }
                        }
                    }

                    w.write("\n");

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

                    for (int i = 0; i < resultsJTable.getRowCount(); i++) {

                        int index = new Integer("" + resultsJTable.getValueAt(i, 0)) - 1;
                        Protein currentProtein = allValidProteins.get(index);

                        if ((exportSelected && (Boolean) resultsJTable.getValueAt(i, resultsJTable.getColumn("  ").getModelIndex()))
                                || !exportSelected) {

                            w.write((index + 1) + "\t" + currentProtein.getProteinName() + "\t" + currentProtein.getAccessionNumber()
                                    + "\t" + currentProtein.getAccessionNumbersAll() + "\t" + currentProtein.getNumberUniquePeptides()
                                    + "\t" + currentProtein.getPercentCoverage() + "\t" + currentProtein.getNumExperimentsDetected()
                                    + "\t" + currentProtein.getFoldChange() + "\t" + currentProtein.getPValue()
                                    + "\t" + currentProtein.getQValue()
                                    + "\t" + resultsJTable.getValueAt(i, resultsJTable.getColumn("Significant").getModelIndex())
                                    + "\t" + resultsJTable.getValueAt(i, resultsJTable.getColumn("Bonferroni").getModelIndex()) + "\t");

                            for (int j = 0; j < currentProtein.getRatiosGroupA().size(); j++) {
                                if (currentProtein.getRatiosGroupA().get(j) == null) {
                                    w.write("\t");
                                } else {
                                    w.write(currentProtein.getRatiosGroupA().get(j) + "\t");
                                }
                            }

                            for (int j = 0; j < currentProtein.getRatiosGroupB().size(); j++) {
                                if (currentProtein.getRatiosGroupB().get(j) == null) {
                                    w.write("\t");
                                } else {
                                    w.write(currentProtein.getRatiosGroupB().get(j) + "\t");
                                }
                            }

                            w.write("\n");
                        }
                    }

                    w.close();
                    f.close();

                    JOptionPane.showMessageDialog(this, "Results successfully exported.", "Results Exported", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "An error occured when exporting the results. See ../resources/conf/MiTRAQ.log for details.");
                    e.printStackTrace();
                }

                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_exportProteinListJButtonActionPerformed

    /**
     * Turns the display of the underlying numbers in the cells in the results
     * table on or off. When turned off the bar charts are shown.
     *
     * @param evt
     */
    private void viewSparklinesJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSparklinesJCheckBoxMenuItemActionPerformed
        showSparklines = viewSparklinesJCheckBoxMenuItem.isSelected();

        valuesAndChartJCheckBoxMenuItem.setEnabled(showSparklines);

        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Peptides").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Exp. Count").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Quant. Count").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Coverage").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("p-value").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("q-value").getCellRenderer()).showNumbers(!showSparklines);

        resultsJTable.revalidate();
        resultsJTable.repaint();
    }//GEN-LAST:event_viewSparklinesJCheckBoxMenuItemActionPerformed

    /**
     * Turns the display of the error bars on or off.
     *
     * @param evt
     */
    private void errorBarsJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorBarsJCheckBoxMenuItemActionPerformed
        showErrorBars = errorBarsJCheckBoxMenuItem.isSelected();
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_errorBarsJCheckBoxMenuItemActionPerformed

    /**
     * Turns the display of the highlighting of the average value bars on or
     * off.
     *
     * @param evt
     */
    private void highlightAveragesJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightAveragesJCheckBoxMenuItemActionPerformed
        highlightAverageBars = highlightAveragesJCheckBoxMenuItem.isSelected();
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_highlightAveragesJCheckBoxMenuItemActionPerformed

    /**
     * Opens a dialog where the user can select the format to export to plot to.
     *
     * @param evt
     */
    private void exportPlotJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPlotJButtonActionPerformed
        ArrayList<ChartPanel> chartPanels = new ArrayList<ChartPanel>();
        chartPanels.add(ratioChartPanel);
        chartPanels.add(foldChangeChartPanel);
        new ExportPlot(this, true, chartPanels);
    }//GEN-LAST:event_exportPlotJButtonActionPerformed

    /**
     * Turns the display of the values together with the charts in the cells in
     * the results table on or off. When turned off the bar charts are shown.
     *
     * @param evt
     */
    private void valuesAndChartJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valuesAndChartJCheckBoxMenuItemActionPerformed
        showValuesAndCharts = valuesAndChartJCheckBoxMenuItem.isSelected();

        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Peptides").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Exp. Count").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Quant. Count").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Coverage").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("p-value").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("q-value").getCellRenderer()).showNumberAndChart(showValuesAndCharts, 50);

        resultsJTable.revalidate();
        resultsJTable.repaint();
    }//GEN-LAST:event_valuesAndChartJCheckBoxMenuItemActionPerformed

    /**
     * Updates the result to show either log 2 or normal ratios.
     *
     * @param evt
     */
    private void ratioLogJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratioLogJCheckBoxMenuItemActionPerformed
        useRatioLog2 = ratioLogJCheckBoxMenuItem.isSelected();
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_ratioLogJCheckBoxMenuItemActionPerformed

    /**
     * Clear the current filter, i.e., show all proteins.
     *
     * @param evt
     */
    private void clearFilterResultsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterResultsJButtonActionPerformed
        currentFilterValues = new String[]{"", "", "", "", "", "", "", "", ""};

        List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();
        RowFilter<Object, Object> allFilters = RowFilter.andFilter(filters);
        ((TableRowSorter) resultsJTable.getRowSorter()).setRowFilter(allFilters);

        if (resultsJTable.getRowCount() > 0) {
            resultsJTable.setRowSelectionInterval(0, 0);
        }

        updateResultTableSelection();

        saveSettings(false);
    }//GEN-LAST:event_clearFilterResultsJButtonActionPerformed

    /**
     * Updates the ratio plot according to the currently selected rows in the
     * protein table.
     *
     * @param evt
     */
    private void resultsJTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsJTableMouseReleased
        if (evt != null && evt.getButton() == MouseEvent.BUTTON3) {
            removeJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else {

            if (evt != null && evt.getButton() == MouseEvent.BUTTON1 && resultsJTable.getSelectedRow() != -1
                    && resultsJTable.getSelectedColumn() == 2
                    && ((String) resultsJTable.getValueAt(resultsJTable.getSelectedRow(),
                    resultsJTable.getSelectedColumn())).indexOf("<html>") != -1) {

                String link = (String) resultsJTable.getValueAt(resultsJTable.getSelectedRow(), resultsJTable.getSelectedColumn());
                link = link.substring(link.indexOf("\"") + 1);
                link = link.substring(0, link.indexOf("\""));

                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(link);
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }

            if (resultsJTable.getSelectedRow() != -1) {

                String title = " ";
                JFreeChart ratioChart = null;

                DefaultCategoryDataset ratioLog2Dataset = new DefaultCategoryDataset();
                DefaultCategoryDataset ratioDataset = new DefaultCategoryDataset();

                if (resultsJTable.getSelectedRows().length > 1) {
                    linesJCheckBoxMenuItem.setSelected(true);
                    linesJCheckBoxMenuItem.setEnabled(false);
                    labelsJMenu.setEnabled(false);
                    errorBarsJCheckBoxMenuItem.setEnabled(false);
                    highlightAveragesJCheckBoxMenuItem.setEnabled(false);
                    showRatioPlotAsLines = true;
                } else {
                    linesJCheckBoxMenuItem.setEnabled(true);
                    labelsJMenu.setEnabled(true);
                    errorBarsJCheckBoxMenuItem.setEnabled(true);
                    highlightAveragesJCheckBoxMenuItem.setEnabled(true);
                }

                // remove old fold change interval markers
                removeFoldChangeMarkers();

                // remove old data point annotations
                removeDataPointAnnotations();

                for (int rowCounter = 0; rowCounter < resultsJTable.getSelectedRows().length; rowCounter++) {

                    // add marker in the fold change plot
                    if (currentProteinsJCheckBoxMenuItem.isSelected()) {
                        double foldChange = ((XYDataPoint) resultsJTable.getValueAt(resultsJTable.getSelectedRows()[rowCounter], 3)).getX();

                        if (foldChange < 0) {
                            foldChange = -Math.log(-foldChange) / Math.log(2);
                        } else {
                            foldChange = Math.log(foldChange) / Math.log(2);
                        }

                        double markerWidth = 0.05;

                        IntervalMarker marker = new IntervalMarker(foldChange - (markerWidth / 2), foldChange + (markerWidth / 2), new Color(1f, 0f, 0f, 0.5f));
                        foldChangeplot.addDomainMarker(marker, Layer.FOREGROUND);

                        // add annotation of the current fold change
                        if (resultsJTable.getSelectedRows().length == 1) {
                            foldChangeplot.addAnnotation(new XYTextAnnotation(
                                    "Current: " + Util.roundDouble(foldChange, 2),
                                    foldChangeplot.getDomainAxis().getUpperBound() * 0.75,
                                    foldChangeplot.getRangeAxis().getUpperBound() * 0.75));
                        }
                    }


                    int index = new Integer("" + resultsJTable.getValueAt(resultsJTable.getSelectedRows()[rowCounter], 0)) - 1;
                    Protein currentProtein = allValidProteins.get(index);
                    StringTokenizer tok = new StringTokenizer(currentProtein.getAccessionNumbersAll(), "|");

                    String dataSeriesTitle = currentProtein.getProteinName() + " - " + currentProtein.getAccessionNumber();

                    if (resultsJTable.getSelectedRows().length == 1) {
                        title = dataSeriesTitle;
                        ((TitledBorder) ratioChartJPanel.getBorder()).setTitle("Ratio Plot (" + title + ")");
                        ratioChartJPanel.repaint();
                        title = null;
                    } else {
                        ((TitledBorder) ratioChartJPanel.getBorder()).setTitle("Ratio Plot (multiple selections)");
                    }

                    DefaultStatisticalCategoryDataset datasetLog2Errors = new DefaultStatisticalCategoryDataset();
                    DefaultStatisticalCategoryDataset datasetErrors = new DefaultStatisticalCategoryDataset();

                    ArrayList<String> labels = new ArrayList<String>();

                    String accessionNumberLinks = "<html>All Accession Numbers: ";

                    while (tok.hasMoreTokens()) {

                        String currentAccessionNumber = tok.nextToken();
                        String database = null;

                        if (currentAccessionNumber.toUpperCase().startsWith("IPI")) {
                            database = "IPI";
//                        } 
//                        else if (currentAccessionNumber.toUpperCase().startsWith("SWISS-PROT")
//                                || currentAccessionNumber.startsWith("UNI-PROT")) {  // @TODO: untested!!
//                            database = "UNI-PROT";
                        } else {  // UniProt assumed
                            database = "UNI-PROT";
                        }

                        // @TODO: add more databases

                        if (database != null) {

                            if (database.equalsIgnoreCase("IPI")) {
                                accessionNumberLinks += "<a href=\"http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-e+%5b"
                                        + database + "-AccNumber:" + currentAccessionNumber
                                        + "%5d\">" + currentAccessionNumber + "</a>, ";
                            } else {
                                accessionNumberLinks += "<a href=\"" + "http://www.uniprot.org/uniprot/" + currentAccessionNumber
                                        + "\"><font color=\"" + notSelectedRowHtmlTagFontColor
                                        + "\">" + currentAccessionNumber + "</font></a>, ";
                            }
                        } else {
                            accessionNumberLinks += currentAccessionNumber + ", ";
                        }
                    }

                    accessionNumberLinks = accessionNumberLinks.substring(0, accessionNumberLinks.length() - 2);
                    accessionNumbersJEditorPane.setText(accessionNumberLinks + "</html>");

                    SummaryStatistics ratioLog2Stats = new SummaryStatistics();
                    SummaryStatistics ratioStats = new SummaryStatistics();
                    SummaryStatistics peptideStats = new SummaryStatistics();
                    SummaryStatistics spectrumStats = new SummaryStatistics();

                    // add bars for the data values in group A
                    for (int i = 0; i < currentProtein.getRatiosGroupA().size(); i++) {
                        if (currentProtein.getRatiosGroupA().get(i) != null) {
                            ratioLog2Dataset.addValue(currentProtein.getRatiosGroupA().get(i), dataSeriesTitle, groupALabel + (i + 1));
                            ratioDataset.addValue(antiLog2(currentProtein.getRatiosGroupA().get(i)), dataSeriesTitle, groupALabel + (i + 1));
                            datasetLog2Errors.add(null, null, dataSeriesTitle, groupALabel + (i + 1));
                            datasetErrors.add(null, null, dataSeriesTitle, groupALabel + (i + 1));
                            ratioLog2Stats.addValue(currentProtein.getRatiosGroupA().get(i));
                            ratioStats.addValue(antiLog2(currentProtein.getRatiosGroupA().get(i)));
                            peptideStats.addValue(currentProtein.getNumPeptidesGroupA().get(i));
                            spectrumStats.addValue(currentProtein.getNumSpectraGroupA().get(i));

                            if (peptideAndSpectraJRadioButtonMenuItem.isSelected()) {
                                labels.add(currentProtein.getNumPeptidesGroupA().get(i) + " / " + currentProtein.getNumSpectraGroupA().get(i));
                            } else {
                                if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                    labels.add("" + Util.roundDouble(currentProtein.getRatiosGroupA().get(i), 2));
                                } else {
                                    labels.add("" + Util.roundDouble(antiLog2(currentProtein.getRatiosGroupA().get(i)), 2));
                                }
                            }
                        } else {
                            ratioLog2Dataset.addValue(0, dataSeriesTitle, groupALabel + (i + 1));
                            datasetLog2Errors.add(null, null, dataSeriesTitle, groupALabel + (i + 1));
                            ratioDataset.addValue(0, dataSeriesTitle, groupALabel + (i + 1));
                            datasetErrors.add(null, null, dataSeriesTitle, groupALabel + (i + 1));
                            labels.add(null);
                        }
                    }

                    if (!showRatioPlotAsLines) {

                        // add a bar for the average value in group A
                        ratioLog2Dataset.addValue(ratioLog2Stats.getMean(), dataSeriesTitle, groupALabel + " Avg");
                        datasetLog2Errors.add(ratioLog2Stats.getMean(), ratioLog2Stats.getStandardDeviation(), dataSeriesTitle, groupALabel + " Avg");
                        ratioDataset.addValue(ratioStats.getMean(), dataSeriesTitle, groupALabel + " Avg");
                        datasetErrors.add(ratioStats.getMean(), ratioStats.getStandardDeviation(), dataSeriesTitle, groupALabel + " Avg");

                        if (peptideAndSpectraJRadioButtonMenuItem.isSelected()) {
                            if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                labels.add(null); // @TODO: add labels??
                            } else {
                                labels.add(null); // @TODO: add labels??
                            }
                        } else {
                            if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                labels.add("" + Util.roundDouble(ratioLog2Stats.getMean(), 2));
                            } else {
                                labels.add("" + Util.roundDouble(antiLog2(ratioLog2Stats.getMean()), 2));
                            }
                        }

                        ratioLog2Stats = new SummaryStatistics();
                        ratioStats = new SummaryStatistics();
                        peptideStats = new SummaryStatistics();
                        spectrumStats = new SummaryStatistics();

                        // add a bar for the average value in group B
                        for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                            if (currentProtein.getRatiosGroupB().get(i) != null) {
                                ratioLog2Stats.addValue(currentProtein.getRatiosGroupB().get(i));
                                ratioStats.addValue(antiLog2(currentProtein.getRatiosGroupB().get(i)));
                                peptideStats.addValue(currentProtein.getNumPeptidesGroupB().get(i));
                                spectrumStats.addValue(currentProtein.getNumSpectraGroupB().get(i));
                            }
                        }

                        ratioLog2Dataset.addValue(ratioLog2Stats.getMean(), dataSeriesTitle, groupBLabel + " Avg");
                        datasetLog2Errors.add(ratioLog2Stats.getMean(), ratioLog2Stats.getStandardDeviation(), dataSeriesTitle, groupBLabel + " Avg");
                        ratioDataset.addValue(ratioStats.getMean(), dataSeriesTitle, groupBLabel + " Avg");
                        datasetErrors.add(ratioStats.getMean(), ratioStats.getStandardDeviation(), dataSeriesTitle, groupBLabel + " Avg");

                        if (peptideAndSpectraJRadioButtonMenuItem.isSelected()) {
                            if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                labels.add(null); // @TODO: add labels??
                            } else {
                                labels.add(null); // @TODO: add labels??
                            }
                        } else {
                            if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                labels.add("" + Util.roundDouble(ratioLog2Stats.getMean(), 2));
                            } else {
                                labels.add("" + Util.roundDouble(antiLog2(ratioLog2Stats.getMean()), 2));
                            }
                        }
                    }

                    // add bars for the data values in group B
                    for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                        if (currentProtein.getRatiosGroupB().get(i) != null) {
                            ratioLog2Dataset.addValue(currentProtein.getRatiosGroupB().get(i), dataSeriesTitle, groupBLabel + (i + 1));
                            datasetLog2Errors.add(null, null, dataSeriesTitle, groupBLabel + (i + 1));
                            ratioDataset.addValue(antiLog2(currentProtein.getRatiosGroupB().get(i)), dataSeriesTitle, groupBLabel + (i + 1));
                            datasetErrors.add(null, null, dataSeriesTitle, groupBLabel + (i + 1));

                            if (peptideAndSpectraJRadioButtonMenuItem.isSelected()) {
                                labels.add(currentProtein.getNumPeptidesGroupB().get(i) + " / " + currentProtein.getNumSpectraGroupB().get(i));
                            } else {
                                if (ratioLogJCheckBoxMenuItem.isSelected()) {
                                    labels.add("" + Util.roundDouble(currentProtein.getRatiosGroupB().get(i), 2));
                                } else {
                                    labels.add("" + Util.roundDouble(antiLog2(currentProtein.getRatiosGroupB().get(i)), 2));
                                }
                            }
                        } else {
                            ratioLog2Dataset.addValue(0, dataSeriesTitle, groupBLabel + (i + 1));
                            datasetLog2Errors.add(null, null, dataSeriesTitle, groupBLabel + (i + 1));
                            ratioDataset.addValue(0, dataSeriesTitle, groupBLabel + (i + 1));
                            datasetErrors.add(null, null, dataSeriesTitle, groupBLabel + (i + 1));
                            labels.add(null);
                        }
                    }

                    // set up the bar colors
                    ArrayList<Color> ratioBarColors = new ArrayList<Color>();

                    // set the colors for the group A bars
                    for (int i = 0; i < currentProtein.getRatiosGroupA().size(); i++) {
                        ratioBarColors.add(groupAColor);
                    }

                    if (!showRatioPlotAsLines) {

                        // set the color for the average group A bar
                        ratioBarColors.add(getAverageValueColor(groupAColor));

                        // set the color for the average group B bar
                        ratioBarColors.add(getAverageValueColor(groupBColor));
                    }

                    // set the colors for the group B bars
                    for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                        ratioBarColors.add(groupBColor);
                    }

                    if (resultsJTable.getSelectedRows().length == 1) {
                        // use normal ratio or log 2 ratios
                        if (useRatioLog2) {
                            ratioChart = createRatioChart(ratioLog2Dataset, datasetLog2Errors, title, useRatioLog2, ratioBarColors, labels, showRatioPlotAsLines);
                        } else {
                            ratioChart = createRatioChart(ratioDataset, datasetErrors, title, useRatioLog2, ratioBarColors, labels, showRatioPlotAsLines);
                        }
                    }
                }

                if (resultsJTable.getSelectedRows().length > 1) {
                    // use normal ratio or log 2 ratios
                    if (useRatioLog2) {
                        ratioChart = createRatioChart(ratioLog2Dataset, new DefaultStatisticalCategoryDataset(), title, useRatioLog2, new ArrayList<Color>(), new ArrayList<String>(), true);
                    } else {
                        ratioChart = createRatioChart(ratioDataset, new DefaultStatisticalCategoryDataset(), title, useRatioLog2, new ArrayList<Color>(), new ArrayList<String>(), true);
                    }
                }

                if (highlightAverageBars && resultsJTable.getSelectedRows().length == 1) {
                    CategoryPlot plot = (CategoryPlot) ratioChart.getPlot();
                    plot.addDomainMarker(new CategoryMarker(groupALabel + " Avg", Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);
                    plot.addDomainMarker(new CategoryMarker(groupBLabel + " Avg", Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);
                }



                if (dataset != null) {

                    Selection selection = new Selection(Selection.TYPE.OF_ROWS, resultsJTable.getSelectedRows());

                    if (SelectionManager.getSelectionManager().getSelectedRows(dataset) != null) {
                        if (!arraysContainsTheSameNumbers(selection.getMembers(), SelectionManager.getSelectionManager().getSelectedRows(dataset).getMembers())) {
                            SelectionManager.getSelectionManager().setSelectedRows(dataset, selection);
                            //System.out.println("update 1: " + evt);
                        }
                    } else {
                        SelectionManager.getSelectionManager().setSelectedRows(dataset, selection);
                        //System.out.println("update 2: " + evt);
                    }
                } else {
                    //System.out.println("not update: " + evt);
                }

                ratioChartPanel = new ChartPanel(ratioChart);
                ratioChartJPanel.removeAll();
                ratioChartJPanel.add(ratioChartPanel);
                ratioChartJPanel.validate();
            }
        }
    }//GEN-LAST:event_resultsJTableMouseReleased

    /**
     * Switch to/from showing the ratio plot as lines (or bars).
     *
     * @param evt
     */
    private void linesJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linesJCheckBoxMenuItemActionPerformed
        showRatioPlotAsLines = linesJCheckBoxMenuItem.isSelected();
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_linesJCheckBoxMenuItemActionPerformed

    /**
     * Update the ratio chart labels.
     *
     * @param evt
     */
    private void ratioLabelJRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratioLabelJRadioButtonMenuItemActionPerformed
        showBarChartLabelsAsRatios = ratioLabelJRadioButtonMenuItem.isSelected();
        showBarChartLabels = true;
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_ratioLabelJRadioButtonMenuItemActionPerformed

    /**
     * Update the ratio chart labels.
     *
     * @param evt
     */
    private void peptideAndSpectraJRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_peptideAndSpectraJRadioButtonMenuItemActionPerformed
        showBarChartLabelsAsRatios = !peptideAndSpectraJRadioButtonMenuItem.isSelected();
        showBarChartLabels = true;
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_peptideAndSpectraJRadioButtonMenuItemActionPerformed

    /**
     * Update the ratio chart labels.
     *
     * @param evt
     */
    private void noLabelJRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noLabelJRadioButtonMenuItemActionPerformed
        showBarChartLabels = !noLabelJRadioButtonMenuItem.isSelected();
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_noLabelJRadioButtonMenuItemActionPerformed

    /**
     * Export all selected protein plots to file.
     *
     * @param evt
     */
    private void exportAllPlotsJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAllPlotsJMenuItemActionPerformed

        int selectedRow = resultsJTable.getSelectedRow();
        int[] selectedRows = resultsJTable.getSelectedRows();

        ArrayList<ChartPanel> chartPanels = new ArrayList<ChartPanel>();

        for (int i = 0; i < resultsJTable.getRowCount(); i++) {

            if (((Boolean) resultsJTable.getValueAt(i, resultsJTable.getColumn("  ").getModelIndex()))) {
                resultsJTable.setRowSelectionInterval(i, i);
                resultsJTableMouseReleased(null);
                chartPanels.add(ratioChartPanel);
            }
        }

        // reset the row selection interval
        resultsJTable.setRowSelectionInterval(selectedRow, selectedRow);

        for (int i = 0; i < selectedRows.length; i++) {
            resultsJTable.addRowSelectionInterval(selectedRows[i], selectedRows[i]);
        }

        resultsJTableMouseReleased(null);

        if (chartPanels.size() > 0) {
            // export the plots
            new ExportPlot(this, true, chartPanels);
        } else {
            JOptionPane.showMessageDialog(this, "No proteins selected!", "Empty Selection", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_exportAllPlotsJMenuItemActionPerformed

    /**
     * Saves the selections and the settings.
     *
     * @param evt
     */
    private void saveJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJMenuItemActionPerformed

        if (currentRatioFile != null) {
            saveSettings(false);
        } else {
            JOptionPane.showMessageDialog(this, "No project to save.", "No Project", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_saveJMenuItemActionPerformed

    /**
     * Opens a new PreferencesDialog.
     *
     * @param evt
     */
    private void preferencesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesJMenuItemActionPerformed
        new PreferencesDialog(this, true, minNumUniquePeptides, minNumUniqueSpectra, minNumberOfExperiments);
    }//GEN-LAST:event_preferencesJMenuItemActionPerformed

    /**
     * Hides or displayes the 1SD interval marker.
     *
     * @param evt
     */
    private void sd1JCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sd1JCheckBoxMenuItemActionPerformed

        Iterator iterator = foldChangeplot.getDomainMarkers(Layer.BACKGROUND).iterator();

        while (iterator.hasNext()) {
            IntervalMarker tempMarker = (IntervalMarker) iterator.next();

            if (tempMarker.getLabel().equalsIgnoreCase("1SD")) {
                if (sd1JCheckBoxMenuItem.isSelected()) {
                    tempMarker.setAlpha(1f);
                } else {
                    tempMarker.setAlpha(0f);
                }
            }
        }
    }//GEN-LAST:event_sd1JCheckBoxMenuItemActionPerformed

    /**
     * Hides or displayes the 2SD interval marker.
     *
     * @param evt
     */
    private void sd2JCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sd2JCheckBoxMenuItemActionPerformed
        Iterator iterator = foldChangeplot.getDomainMarkers(Layer.BACKGROUND).iterator();

        while (iterator.hasNext()) {
            IntervalMarker tempMarker = (IntervalMarker) iterator.next();

            if (tempMarker.getLabel().equalsIgnoreCase("2SD")) {
                if (sd2JCheckBoxMenuItem.isSelected()) {
                    tempMarker.setAlpha(1f);
                } else {
                    tempMarker.setAlpha(0f);
                }
            }
        }
    }//GEN-LAST:event_sd2JCheckBoxMenuItemActionPerformed

    /**
     * Hides or displays the current proteins markers in the fold change plot.
     *
     * @param evt
     */
    private void currentProteinsJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentProteinsJCheckBoxMenuItemActionPerformed
        resultsJTableMouseReleased(null);
    }//GEN-LAST:event_currentProteinsJCheckBoxMenuItemActionPerformed

    /**
     * Displays the list of removed proteins for possible re-adding.
     *
     * @param evt
     */
    private void removedJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removedJMenuItemActionPerformed
        new RemovedProteins(this, true, removedProteins);
    }//GEN-LAST:event_removedJMenuItemActionPerformed

    /**
     * Removes the selected proteins from the list and reloads the data.
     *
     * @param evt
     */
    private void removeJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeJMenuItemActionPerformed

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        int selectedRow = resultsJTable.getSelectedRow();

        int[] selectedRows = resultsJTable.getSelectedRows();

        for (int i = 0; i < selectedRows.length; i++) {

            int index = new Integer("" + resultsJTable.getValueAt(resultsJTable.getSelectedRows()[i], 0)) - 1;
            Protein currentProtein = allValidProteins.get(index);

            removedProteins.add(currentProtein.getProteinName() + "|" + currentProtein.getAccessionNumber());
        }

        reloadItraqData();

        if (selectedRow != -1 && resultsJTable.getRowCount() >= selectedRow) {
            if (resultsJTable.getRowCount() == selectedRow) {
                resultsJTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
            } else {
                resultsJTable.setRowSelectionInterval(selectedRow, selectedRow);
            }

            // update the plots
            resultsJTableMouseReleased(null);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_removeJMenuItemActionPerformed

    /**
     * Changes the cursor into a hand cursor if the table cell contains an html
     * link.
     *
     * @param evt
     */
    private void resultsJTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsJTableMouseMoved

        int row = resultsJTable.rowAtPoint(evt.getPoint());
        int column = resultsJTable.columnAtPoint(evt.getPoint());

        if (column == resultsJTable.getColumn("Accession").getModelIndex()) {
            String tempValue = (String) resultsJTable.getValueAt(row, column);

            if (tempValue.lastIndexOf("<html>") != -1) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        }

    }//GEN-LAST:event_resultsJTableMouseMoved

    /**
     * Opens the Save As menu.
     *
     * @param evt
     */
    private void saveAsJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsJMenuItemActionPerformed
        // @TODO: implement Save As...?
//        final JFileChooser chooser = new JFileChooser(lastSelectedFolder);
//
//        int selection = chooser.showSaveDialog(this);
//
//        if (selection == JFileChooser.APPROVE_OPTION) {
//
//            String path = chooser.getSelectedFile().getAbsoluteFile().getPath();
//
//            if (!path.endsWith(".tsv")) {
//                path = path + ".tsv";
//            }
//
//            boolean save = true;
//
//            if (new File(path).exists()) {
//                int value = JOptionPane.showConfirmDialog(this, "The file already exists. Overwrite?", "Overwrite?",
//                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
//                save = value == JOptionPane.YES_OPTION;
//            }
//
//            if (save) {
//
//            }
//
//        if (currentRatioFile != null) {
//            saveSettings(true);
//        } else {
//            JOptionPane.showMessageDialog(this, "No project to save.", "No Project", JOptionPane.INFORMATION_MESSAGE);
//        }
    }//GEN-LAST:event_saveAsJMenuItemActionPerformed

    /**
     * Changes the cursor back to the default cursor a hand.
     *
     * @param evt
     */
    private void resultsJTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsJTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_resultsJTableMouseExited

    /**
     * Open the filter
     *
     * @param evt
     */
    private void filterJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterJMenuItemActionPerformed
        new ResultsFilter(this, false, currentFilterValues, currrentFilterRadioButtonSelections, foldChangeAbsoluteValue, true);
    }//GEN-LAST:event_filterJMenuItemActionPerformed

    private void hcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hcButtonActionPerformed

        int columnCount = groupANumberOfMembers + groupBNumberOfMembers;

        double[][] values = new double[resultsJTable.getRowCount()][columnCount];
        boolean[][] missing = new boolean[resultsJTable.getRowCount()][columnCount];
        String[] columnHeader = new String[columnCount];
        String[] rowHeaders = new String[resultsJTable.getRowCount()];

        for (int i = 0; i < resultsJTable.getRowCount(); i++) {

            int index = new Integer("" + resultsJTable.getValueAt(i, 0)) - 1;
            Protein currentProtein = allValidProteins.get(index);

            //String dataSeriesTitle = currentProtein.getProteinName() + " (" + currentProtein.getAccessionNumber() + ")";
            String dataSeriesTitle = currentProtein.getAccessionNumber();

            rowHeaders[i] = dataSeriesTitle;

            for (int j = 0; j < currentProtein.getRatiosGroupA().size(); j++) {

                if (currentProtein.getRatiosGroupA().get(j) != null) {
                    values[i][j] = currentProtein.getRatiosGroupA().get(j);
                    missing[i][j] = false;
                } else {
                    missing[i][j] = true;
                }

                columnHeader[j] = groupALabel + (j + 1);
            }

            for (int j = 0; j < currentProtein.getRatiosGroupB().size(); j++) {

                if (currentProtein.getRatiosGroupB().get(j) != null) {
                    values[i][j + (columnCount / 2)] = currentProtein.getRatiosGroupB().get(j);
                    missing[i][j + (columnCount / 2)] = false;
                } else {
                    missing[i][j + (columnCount / 2)] = true;
                }

                columnHeader[j + (columnCount / 2)] = groupBLabel + (j + 1);
            }
        }

        dataset = DataSet.newDataSet(values, missing);
        dataset.setColumnIds(columnHeader);
        dataset.setRowIds(rowHeaders);

        final MiTRAQ finalRef = this;


        new Thread(new Runnable() {
            public void run() {
                progressDialog.setVisible(true);
            }
        }, "ProgressDialog").start();

        new Thread("DisplayThread") {
            @Override
            public void run() {
                try {
                    progressDialog.setIntermidiate(false);
                    progressDialog.setMax(100);
                    progressDialog.setTitle("Computing Clustering. Please Wait...");

                    ClusterParameters parameters1 = new ClusterParameters();
                    parameters1.setClusterSamples(true);
                    SOMClustCompute som1 = new SOMClustCompute(dataset, parameters1);

                    SWorkerThread t = new SWorkerThread(som1);
                    t.execute();

                    while (!t.isDone()) {
                        progressDialog.setValue(som1.getProgress());
                        Thread.sleep(50);
                    }

                    ClusterResults results1 = t.get();

                    progressDialog.setIntermidiate(true);
                    progressDialog.setTitle("Displaying Clustering. Please Wait...");

                    HierarchicalClusteringPanel hierarchicalClusteringPanel =
                            new HierarchicalClusteringPanel(dataset, parameters1, results1);
                    SelectionManager.getSelectionManager().addSelectionChangeListener(dataset, hierarchicalClusteringPanel);

                    SelectionManager.getSelectionManager().addSelectionChangeListener(dataset, finalRef);

                    JDialog dialog = new JDialog(finalRef, "Hierachical Clutering", false);
                    dialog.setSize(600, finalRef.getHeight() - 100);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    dialog.add(hierarchicalClusteringPanel);

                    progressDialog.setVisible(false);
                    progressDialog.dispose();

                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);

                } catch (Exception ex) {
                    Logger.getLogger(TestPanelJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }//GEN-LAST:event_hcButtonActionPerformed

    /**
     * Update the minimium number of peptides setting.
     *
     * @param minNumUniquePeptides
     */
    public void setMinNumberPeptides(int minNumUniquePeptides) {
        this.minNumUniquePeptides = minNumUniquePeptides;
    }

    /**
     * Update the minimum number of spectra setting.
     *
     * @param minNumUniqueSpectra
     */
    public void setMinNumberSpectra(int minNumUniqueSpectra) {
        this.minNumUniqueSpectra = minNumUniqueSpectra;
    }

    /**
     * Update the minimum number of experiemnts
     *
     * @param minNumberOfExperiments
     */
    public void setMinNumberExperiments(int minNumberOfExperiments) {
        this.minNumberOfExperiments = minNumberOfExperiments;
    }

    /**
     * Returns the results table.
     *
     * @return the results table
     */
    public JTable getResultsTable() {
        return resultsJTable;
    }

    /**
     * Creates the ratio chart for the selected protein.
     *
     * @param dataset the ratios for the protein
     * @param datasetErrors the data set of the errors
     * @param title the title of the chart
     * @param yAxisLabel the label to use for the y axis
     * @param barColors the colors to use for the bars
     * @param customLabels the labels to use for the bars
     * @return the chart
     */
    private JFreeChart createRatioChart(CategoryDataset dataset, DefaultStatisticalCategoryDataset datasetErrors,
            String title, boolean logRatios, ArrayList<Color> barColors, ArrayList<String> customLabels,
            boolean showRatioPlotAsLines) {

        String yAxisLabel;

        if (logRatios) {
            yAxisLabel = "Ratio (log 2)";
        } else {
            yAxisLabel = "Ratio";
        }

        JFreeChart chart;

        if (showRatioPlotAsLines) {
            // create the line chart
            chart = ChartFactory.createLineChart(
                    title, // chart title
                    null, // domain axis label
                    yAxisLabel, // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    false, // include legend
                    true, // tooltips
                    false); // urls
        } else {
            // create the bar chart
            chart = ChartFactory.createBarChart(
                    title, // chart title
                    null, // domain axis label
                    yAxisLabel, // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    false, // include legend
                    true, // tooltips
                    false); // urls
        }

        // set the background and gridline colors
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);

        // set the bar renderer
        CategoryItemRenderer renderer;

        if (showRatioPlotAsLines) {
            renderer = new LineAndShapeRenderer(true, false);
            renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

            for (int i = 0; i < dataset.getRowCount(); i++) {
                renderer.setSeriesStroke(i, new BasicStroke(LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // @TODO: show background distribution in gray as well
//                if (i==9) {
//                    renderer.setSeriesPaint(i, Color.RED);
//                } else {
//                    renderer.setSeriesPaint(i, new Color(Color.lightGray.getRed(), Color.lightGray.getGreen(), Color.lightGray.getBlue(), 100));
//                }
            }

        } else {
            renderer = new BarChartColorRenderer(barColors);
        }

        renderer.setBaseItemLabelsVisible(true);

        // add bar chart labels if selected
        if (showBarChartLabels && !showRatioPlotAsLines) {

            if (!showBarChartLabelsAsRatios) {
                CustomLabelGenerator labels = new CustomLabelGenerator(customLabels);
                labels.generateLabel(dataset, 0, 0);
                renderer.setBaseItemLabelGenerator(labels);
            } else {
                CustomLabelGenerator labels = new CustomLabelGenerator(customLabels);
                labels.generateLabel(dataset, 0, 0);
                renderer.setBaseItemLabelGenerator(labels);
            }

            renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
            renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        }

        plot.setRenderer(renderer);

        //plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45); // @TODO: make this a user choice?

        // change the margin at the top and bottom of the range axis
        final ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLowerMargin(0.15);
        rangeAxis.setUpperMargin(0.15);

        double lowerBound = rangeAxis.getLowerBound();
        double upperBound = rangeAxis.getUpperBound();

        // make sure that the iTRAQ ratio bar chart has a symmetrical y-axis
        if (logRatios) {
            if (Math.abs(lowerBound) > Math.abs(upperBound)) {
                rangeAxis.setUpperBound(Math.abs(lowerBound));
            } else {
                rangeAxis.setLowerBound(-Math.abs(upperBound));
            }
        }

        // add a second axis on the right, identical to the left one
        ValueAxis rangeAxis2 = chart.getCategoryPlot().getRangeAxis();
        plot.setRangeAxis(1, rangeAxis2);

        // add error bars to the bar chart if selected
        if (showErrorBars && !showRatioPlotAsLines) {
            plot.setDataset(1, datasetErrors);
            plot.mapDatasetToRangeAxis(1, 0);

            ArrayList<Color> barColorsErrors = new ArrayList<Color>();

            // add "colors" for the first dataset, need to get the correct
            // indices for the average bar colors
            for (int i = 0; i < (dataset.getColumnCount() - 2) / 2; i++) {
                barColorsErrors.add(null);
            }

            // set the color for the average group A bar
            barColorsErrors.add(getAverageValueColor(groupAColor));

            // set the color for the average group B bar
            barColorsErrors.add(getAverageValueColor(groupBColor));

            // set the renderer for the error bar plot
            StatisticalBarChartColorRenderer rendererErrors = new StatisticalBarChartColorRenderer(barColorsErrors);
            plot.setRenderer(1, rendererErrors);

            // make sure that the error bars are drawn last
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        }

        return chart;
    }

    /**
     * The main method used to start MiTRAQ.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        // set the look and feel
        setLookAndFeel();

//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
        MiTRAQ miTRAQ = new MiTRAQ();
        miTRAQ.setVisible(true);

        String dataFile = null;

        // if a file is found in the data folder, suggest this as the file to open
        if (new File(miTRAQ.getJarFilePath() + "/data/").listFiles().length > 0) {
            dataFile = new File(miTRAQ.getJarFilePath() + "/data/").listFiles()[0].getPath();
        }

        new ExperimentalDesign(miTRAQ, true, dataFile);
//            }
//        });
    }

    /**
     * Sets the look and feel. First tries to use Nimbus, if Nimbus is not
     * supported then PlasticXPLookAndFeel is used.
     *
     * @return true if the Nimbus look and feel is used, false otherwise
     */
    public static boolean setLookAndFeel() {

        boolean nimbusLookAndFeelFound = false;

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusLookAndFeelFound = true;
                    break;
                }
            }
        } catch (Exception e) {
            // ignore error, use look and feel below
        }

        if (!nimbusLookAndFeelFound) {
            try {
                PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                // this should not be possible...
            }
        }

        return nimbusLookAndFeelFound;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JScrollPane accessiobNumbersJScrollPane;
    private javax.swing.JEditorPane accessionNumbersJEditorPane;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JPanel chartsJPanel;
    private javax.swing.JSplitPane chartsJSplitPane;
    private javax.swing.JButton clearFilterResultsJButton;
    private javax.swing.JCheckBoxMenuItem currentProteinsJCheckBoxMenuItem;
    private javax.swing.JMenu editJMenu;
    private javax.swing.JCheckBoxMenuItem errorBarsJCheckBoxMenuItem;
    private javax.swing.JMenuItem exitJMenuItem;
    private javax.swing.JMenuItem exportAllPlotsJMenuItem;
    private javax.swing.JMenu exportJMenu;
    private javax.swing.JButton exportPlotJButton;
    private javax.swing.JButton exportProteinListJButton;
    private javax.swing.JMenu fileJMenu;
    private javax.swing.JMenuItem filterJMenuItem;
    private javax.swing.JButton filterResultsJButton;
    private javax.swing.JPanel foldChangeChartJPanel;
    private javax.swing.JMenu foldChangePlotJMenu;
    private javax.swing.JButton hcButton;
    private javax.swing.JMenu helpJMenu;
    private javax.swing.JMenuItem helpJMenuItem;
    private javax.swing.JCheckBoxMenuItem highlightAveragesJCheckBoxMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.ButtonGroup labelButtonGroup;
    private javax.swing.JMenu labelsJMenu;
    private javax.swing.JCheckBoxMenuItem linesJCheckBoxMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem noLabelJRadioButtonMenuItem;
    private javax.swing.JMenuItem openJMenuItem;
    private javax.swing.JRadioButtonMenuItem peptideAndSpectraJRadioButtonMenuItem;
    private javax.swing.JMenuItem preferencesJMenuItem;
    private javax.swing.JPanel ratioChartJPanel;
    private javax.swing.JRadioButtonMenuItem ratioLabelJRadioButtonMenuItem;
    private javax.swing.JCheckBoxMenuItem ratioLogJCheckBoxMenuItem;
    private javax.swing.JMenuItem removeJMenuItem;
    private javax.swing.JPopupMenu removeJPopupMenu;
    private javax.swing.JMenuItem removedJMenuItem;
    private javax.swing.JPanel resultsJPanel;
    private javax.swing.JSplitPane resultsJSplitPane;
    private javax.swing.JTable resultsJTable;
    private javax.swing.JPanel resultsTableJPanel;
    private javax.swing.JScrollPane resultsTableJScrollPane;
    private javax.swing.JMenuItem saveAsJMenuItem;
    private javax.swing.JMenuItem saveJMenuItem;
    private javax.swing.JCheckBoxMenuItem sd1JCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem sd2JCheckBoxMenuItem;
    private javax.swing.JLabel significanceLevelJLabel;
    private javax.swing.JSpinner significanceLevelJSpinner;
    private javax.swing.JCheckBoxMenuItem valuesAndChartJCheckBoxMenuItem;
    private javax.swing.JMenu viewJMenu;
    private javax.swing.JCheckBoxMenuItem viewSparklinesJCheckBoxMenuItem;
    // End of variables declaration//GEN-END:variables

    /**
     * Add the current filter settings at the top of export file.
     *
     * @param w the buffered writer
     * @throws IOException
     */
    private void addFilterSettings(BufferedWriter w) throws IOException {

        w.write("Filter Settings:\n");
        w.write("Protein Name: " + currentFilterValues[0] + "\n");
        w.write("#Peptides: ");

        if (currrentFilterRadioButtonSelections[0] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[0] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[1] + "\n");


        w.write("Protein Coverage: ");

        if (currrentFilterRadioButtonSelections[1] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[1] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[2] + "\n");


        w.write("Experiment Count: ");

        if (currrentFilterRadioButtonSelections[2] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[2] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[3] + "\n");


        w.write("Quantification Count: ");

        if (currrentFilterRadioButtonSelections[3] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[3] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[4] + "\n");


        w.write("Fold Change: ");

        if (currrentFilterRadioButtonSelections[4] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[4] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[5] + "\n");


        w.write("p-value: ");

        if (currrentFilterRadioButtonSelections[5] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[5] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[6] + "\n");


        w.write("q-value: ");

        if (currrentFilterRadioButtonSelections[6] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[6] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[7] + "\n");

        w.write("Significance Level: " + significanceLevelJSpinner.getValue() + "\n");

        w.write("Protein Accession: " + currentFilterValues[8] + "\n");
    }

    /**
     * Clear the previous data.
     */
    public void clearOldData() {

        foldChangeChartJPanel.removeAll();
        foldChangeChartJPanel.repaint();

        resultsJTable.setRowSorter(null);

        // clear old data
        while (resultsJTable.getRowCount() > 0) {
            ((DefaultTableModel) resultsJTable.getModel()).removeRow(0);
        }

        // reset the filters
        currentFilterValues = new String[9]; //{"", "", "", "", "", "", "", "", ""};
        currentFilterValues[0] = "";
        currentFilterValues[1] = "";
        currentFilterValues[2] = "";
        currentFilterValues[3] = "";
        currentFilterValues[4] = "";
        currentFilterValues[5] = "";
        currentFilterValues[6] = "";
        currentFilterValues[7] = "";
        currentFilterValues[8] = "";

        currrentFilterRadioButtonSelections = new Integer[7];
        currrentFilterRadioButtonSelections[0] = 0;
        currrentFilterRadioButtonSelections[1] = 0;
        currrentFilterRadioButtonSelections[2] = 0;
        currrentFilterRadioButtonSelections[3] = 0;
        currrentFilterRadioButtonSelections[4] = 0;
        currrentFilterRadioButtonSelections[5] = 2;
        currrentFilterRadioButtonSelections[6] = 2;

        foldChangeAbsoluteValue = true;

        ratioChartJPanel.removeAll();
        ratioChartJPanel.repaint();
        accessionNumbersJEditorPane.setText(null);

        ((TitledBorder) resultsJPanel.getBorder()).setTitle("Results");
        resultsJPanel.revalidate();
        resultsJPanel.repaint();
    }

    /**
     * Reload the iTRAQ file. Used when the preferences have been changed.
     */
    public void reloadItraqData() {

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        if (currentRatioFile != null) {

            saveSettings(false);

            loadItraqData(groupALabel, groupBLabel, currentITraqType,
                    currentITraqReference, numberOfExperiments,
                    experimentalDesignJTable, currentRatioFile,
                    false);
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }

    /**
     * Reload the iTRAQ file. Used when the removed proteins have been changed.
     *
     * @param removedProteins
     */
    public void reloadItraqData(ArrayList<String> removedProteins) {

        this.removedProteins = removedProteins;
        reloadItraqData();
    }

    /**
     * Saves the experimental design.
     */
    private void saveExperimentalDesign() {

        File experimentalDesignFile = new File(getJarFilePath() + "/resources/conf/" + new File(currentRatioFile).getName() + ".exp");

        try {
            FileWriter w = new FileWriter(experimentalDesignFile);
            BufferedWriter b = new BufferedWriter(w);

            b.write("Number of Experiments: " + numberOfExperiments + "\n");
            b.write("Number of Groups: " + 2 + "\n");
            b.write("Group 1: " + groupALabel + "\n");
            b.write("Group 1 Color: " + groupAColor.getRGB() + "\n");
            b.write("Group 2: " + groupBLabel + "\n");
            b.write("Group 2 Color: " + groupBColor.getRGB() + "\n");
            b.write("iTRAQType: " + currentITraqType + "\n");
            b.write("iTRAQReference: " + currentITraqReference + "\n");

            b.write("ExperimentalDesign:\n");

            for (int i = 0; i < experimentalDesignJTable.getRowCount(); i++) {
                for (int j = 1; j < experimentalDesignJTable.getColumnCount(); j++) {
                    b.write(experimentalDesignJTable.getValueAt(i, j) + "\t");
                }

                b.write("\n");
            }

            b.close();
            w.close();

        } catch (IOException e) {
            System.out.println("Error while saving experimental design:");
            e.printStackTrace();
        }
    }

    /**
     * Saves the current settings for the project.
     */
    private void saveSettings(boolean showMessage) {

        File settingsFile = new File(getJarFilePath() + "/resources/conf/" + new File(currentRatioFile).getName() + ".props");

        try {
            FileWriter w = new FileWriter(settingsFile);
            BufferedWriter b = new BufferedWriter(w);

            // significance level
            b.write("Significance Level: " + ((Double) significanceLevelJSpinner.getValue()) + "\n");

            // preferences
            b.write("Min number of Peptides: " + minNumUniquePeptides + "\n");
            b.write("Min number of Spectra: " + minNumUniqueSpectra + "\n");
            b.write("Min number of Experiments: " + minNumberOfExperiments + "\n");

            // view settings
            b.write("JSparklines: " + viewSparklinesJCheckBoxMenuItem.isSelected() + "\n");
            b.write("JSparklines & Values: " + valuesAndChartJCheckBoxMenuItem.isSelected() + "\n");
            b.write("Error Bars: " + errorBarsJCheckBoxMenuItem.isSelected() + "\n");
            b.write("Highlight Average: " + highlightAveragesJCheckBoxMenuItem.isSelected() + "\n");
            b.write("Ratios as log: " + ratioLogJCheckBoxMenuItem.isSelected() + "\n");
            b.write("Ratio Labels: " + ratioLabelJRadioButtonMenuItem.isSelected() + "\n");
            b.write("Peptide and Spectra Labels: " + peptideAndSpectraJRadioButtonMenuItem.isSelected() + "\n");
            b.write("No Labels: " + noLabelJRadioButtonMenuItem.isSelected() + "\n");
            b.write("Lines plot: " + linesJCheckBoxMenuItem.isSelected() + "\n");

            // filter values
            b.write("Filter Values:\n");
            for (int i = 0; i < currentFilterValues.length; i++) {
                b.write(currentFilterValues[i] + "\n");
            }
            b.write(foldChangeAbsoluteValue + "\n");

            // filter radio button selections
            b.write("Filter Radio Button Selections:\n");
            for (int i = 0; i < currrentFilterRadioButtonSelections.length; i++) {
                b.write(currrentFilterRadioButtonSelections[i] + "\n");
            }

            // selected proteins
            b.write("Selected Proteins:\n");
            for (int i = 0; i < resultsJTable.getRowCount(); i++) {
                if (((Boolean) resultsJTable.getValueAt(i, resultsJTable.getColumn("  ").getModelIndex()))) {

                    String temp = (String) resultsJTable.getValueAt(i, resultsJTable.getColumn("Accession").getModelIndex());

                    if (temp.startsWith("<html>")) {
                        temp = temp.substring("<html></u>".length() - 1, temp.length() - "</u></html>".length());

                        if (temp.lastIndexOf("href=") != -1) {

                            String description = temp.substring(0, temp.indexOf("href="));
                            String accession = temp.substring(temp.indexOf("href="));

                            accession = accession.substring(accession.lastIndexOf("\">") + 2);
                            accession = accession.substring(0, accession.lastIndexOf("<"));

                            temp = description.trim() + " " + accession.trim();
                        }
                    }

                    b.write(((String) resultsJTable.getValueAt(i, resultsJTable.getColumn("Protein").getModelIndex())).trim() + " " + temp.trim() + "\n");
                }
            }

            // removed proteins
            b.write("Removed Proteins:\n");
            for (int i = 0; i < removedProteins.size(); i++) {
                b.write(removedProteins.get(i) + "\n");
            }

            b.close();
            w.close();

            if (showMessage) {
                JOptionPane.showMessageDialog(this, "The project has been saved.", "Project Saved", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IOException e) {
            System.out.println("Error while saving settings:");
            e.printStackTrace();
        }
    }

    /**
     * Tries to read the file containing the settings for a previously used
     * iTRAQ data file.
     *
     * @param settingsFile the settings file
     */
    private void readSettings() {

        if (new File(getJarFilePath() + "/resources/conf/" + new File(currentRatioFile).getName() + ".props").exists()) {

            File settingsFile = new File(getJarFilePath() + "/resources/conf/" + new File(currentRatioFile).getName() + ".props");

            try {
                FileReader f = new FileReader(settingsFile);
                BufferedReader b = new BufferedReader(f);

                String currentLine = b.readLine();

                significanceLevelJSpinner.setValue(Double.parseDouble(currentLine.substring("Significance Level: ".length())));

                minNumUniquePeptides = new Integer(b.readLine().substring("Min number of Peptides: ".length()));
                minNumUniqueSpectra = new Integer(b.readLine().substring("Min number of Spectra: ".length()));
                minNumberOfExperiments = new Integer(b.readLine().substring("Min number of Experiments: ".length()));

                showSparklines = Boolean.parseBoolean(b.readLine().substring("JSparklines: ".length()));
                viewSparklinesJCheckBoxMenuItem.setSelected(showSparklines);
                viewSparklinesJCheckBoxMenuItemActionPerformed(null);
                showValuesAndCharts = Boolean.parseBoolean(b.readLine().substring("JSparklines & Values: ".length()));
                valuesAndChartJCheckBoxMenuItem.setSelected(showValuesAndCharts);
                valuesAndChartJCheckBoxMenuItemActionPerformed(null);
                showErrorBars = Boolean.parseBoolean(b.readLine().substring("Error Bars: ".length()));
                errorBarsJCheckBoxMenuItem.setSelected(showErrorBars);
                errorBarsJCheckBoxMenuItemActionPerformed(null);
                highlightAverageBars = Boolean.parseBoolean(b.readLine().substring("Highlight Average: ".length()));
                highlightAveragesJCheckBoxMenuItem.setSelected(highlightAverageBars);
                highlightAveragesJCheckBoxMenuItemActionPerformed(null);
                useRatioLog2 = Boolean.parseBoolean(b.readLine().substring("Ratios as log: ".length()));
                ratioLogJCheckBoxMenuItem.setSelected(useRatioLog2);
                ratioLogJCheckBoxMenuItemActionPerformed(null);
                showBarChartLabelsAsRatios = Boolean.parseBoolean(b.readLine().substring("Ratio Labels: ".length()));
                ratioLabelJRadioButtonMenuItem.setSelected(showBarChartLabelsAsRatios);
                ratioLabelJRadioButtonMenuItemActionPerformed(null);
                showBarChartLabelsAsRatios = !Boolean.parseBoolean(b.readLine().substring("Peptide and Spectra Labels: ".length()));
                peptideAndSpectraJRadioButtonMenuItem.setSelected(showBarChartLabelsAsRatios);
                peptideAndSpectraJRadioButtonMenuItemActionPerformed(null);
                showBarChartLabels = Boolean.parseBoolean(b.readLine().substring("No Labels: ".length()));
                noLabelJRadioButtonMenuItem.setSelected(showBarChartLabels);
                noLabelJRadioButtonMenuItemActionPerformed(null);
                showRatioPlotAsLines = Boolean.parseBoolean(b.readLine().substring("Lines plot: ".length()));
                linesJCheckBoxMenuItem.setSelected(showRatioPlotAsLines);
                linesJCheckBoxMenuItemActionPerformed(null);

                // skip the filter settings line
                b.readLine();

                int index = 0;
                currentLine = b.readLine();

                while (!currentLine.startsWith("Filter Radio Button Selections:")) {

                    if (currentLine.trim().equalsIgnoreCase("true") || currentLine.trim().equalsIgnoreCase("false")) {
                        foldChangeAbsoluteValue = Boolean.parseBoolean(currentLine);
                    } else {
                        currentFilterValues[index++] = currentLine;
                    }

                    currentLine = b.readLine();
                }

                index = 0;
                currentLine = b.readLine();

                while (!currentLine.startsWith("Selected Proteins:")) {
                    currrentFilterRadioButtonSelections[index++] = Integer.parseInt(currentLine);
                    currentLine = b.readLine();
                }

                // selected proteins
                currentLine = b.readLine();

                selectedProteins = new ArrayList<String>();

                while (currentLine != null && !currentLine.startsWith("Removed Proteins:")) {
                    selectedProteins.add(currentLine);
                    currentLine = b.readLine();
                }

                removedProteins = new ArrayList<String>();

                if (currentLine != null) {

                    // selected proteins
                    currentLine = b.readLine();

                    while (currentLine != null) {
                        removedProteins.add(currentLine);
                        currentLine = b.readLine();
                    }
                }

                b.close();
                f.close();

            } catch (FileNotFoundException e) {
                System.out.println("Error while reading settings:");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error while reading settings:");
                e.printStackTrace();
            }
        } else {
            selectedProteins = new ArrayList<String>();
            removedProteins = new ArrayList<String>();
        }
    }

    /**
     * Loads the iTRAQ data from the ssv input file.
     *
     * @param iGroupALabel the label to use for group A
     * @param iGroupBLabel the label to use for group B
     * @param iCurrentITraqType the current iTRAQ type (4-plex or 8-plex)
     * @param iCurrentITraqReference the current iTRAQ reference
     * @param iNumberOfExperiments the number of iTRAQ experiments
     * @param iExperimentalDesignJTable the experimental design table with the
     * experimental setup
     * @param iRatioFile the ssv file containing the data to load
     * @param iUpdateFilter if true the data is "re-filtered"
     */
    public void loadItraqData(String iGroupALabel, String iGroupBLabel, String iCurrentITraqType,
            String iCurrentITraqReference, Integer iNumberOfExperiments, JTable iExperimentalDesignJTable, String iRatioFile,
            boolean iUpdateFilter) {

        final String groupALabel = iGroupALabel;
        final String groupBLabel = iGroupBLabel;
        final boolean updateFilter = iUpdateFilter;
        final String currentITraqType = iCurrentITraqType;
        final String currentITraqReference = iCurrentITraqReference;
        final Integer numberOfExperiments = iNumberOfExperiments;
        final JTable experimentalDesignJTable = iExperimentalDesignJTable;
        final String ratioFile = iRatioFile;

        // needed for threading issues
        final MiTRAQ tempRef = this;

        this.currentITraqType = currentITraqType;
        this.currentITraqReference = currentITraqReference;
        this.numberOfExperiments = numberOfExperiments;
        this.experimentalDesignJTable = experimentalDesignJTable;
        currentRatioFile = ratioFile;

        // add the title of the project to the dialog header
        this.setTitle("MiTRAQ - Multiple iTRAQ Data Analysis - v" + getVersion() + " beta"
                + " - " + new File(ratioFile).getName());

        this.groupALabel = groupALabel;
        this.groupBLabel = groupBLabel;

        this.groupAColor = new Color(groupAColor.getRGB());
        this.groupBColor = new Color(groupBColor.getRGB());

        clearOldData();

        progressDialog = new ProgressDialog(this, this, true);

        new Thread(new Runnable() {
            public void run() {
                progressDialog.setIntermidiate(true);
                progressDialog.setTitle("Loading Data. Please Wait...");
                progressDialog.setVisible(true);
            }
        }, "ProgressDialog").start();

        new Thread("LoadingThread") {
            @Override
            public void run() {

                saveJMenuItem.setEnabled(true);

                // save the experimental design for later
                saveExperimentalDesign();

                readSettings();


                // set up the experimental design
                final int NUMBER_OF_ITRAQ_TAGS;

                if (currentITraqType.equalsIgnoreCase("4-plex")) {
                    NUMBER_OF_ITRAQ_TAGS = 4;
                } else {
                    NUMBER_OF_ITRAQ_TAGS = 8;
                }

                final int MINIMUM_NUMBER_OF_RATIOS_FOR_T_TEST = 2;

                String[][] experimentLabels = new String[numberOfExperiments][(NUMBER_OF_ITRAQ_TAGS - 1)];
                HashMap<String, ArrayList<Double>> allRatios = new HashMap<String, ArrayList<Double>>();

                groupANumberOfMembers = 0;
                groupBNumberOfMembers = 0;

                int refColumn = 1;

                // find the reference column // @TODO: this should be done in a smarter way!
                for (int i = 1; i < experimentalDesignJTable.getColumnCount(); i++) {
                    if (experimentalDesignJTable.getValueAt(0, i) != null) {
                        String currentValue = experimentalDesignJTable.getValueAt(0, i).toString();

                        if (currentValue.equalsIgnoreCase("Ref")) {
                            refColumn = i;
                        }
                    }
                }

                for (int i = 0; i < experimentalDesignJTable.getRowCount(); i++) {
                    for (int j = 1; j < experimentalDesignJTable.getColumnCount(); j++) {

                        int columnIndex;

                        if (j <= refColumn) {
                            columnIndex = j - 1;
                        } else {
                            columnIndex = j - 2;
                        }

                        if (experimentalDesignJTable.getValueAt(i, j) != null) {

                            String currentValue = experimentalDesignJTable.getValueAt(i, j).toString();

                            if (!currentValue.equalsIgnoreCase("Ref")) {
                                experimentLabels[i][columnIndex] = currentValue;
                            }

                            if (currentValue.equalsIgnoreCase(groupALabel)) {
                                allRatios.put(i + "_" + (columnIndex), new ArrayList<Double>());
                                groupANumberOfMembers++;
                            } else if (currentValue.equalsIgnoreCase(groupBLabel)) {
                                allRatios.put(i + "_" + (columnIndex), new ArrayList<Double>());
                                groupBNumberOfMembers++;
                            }
                        } else {
                            experimentLabels[i][columnIndex] = null;
                        }
                    }
                }

                if (groupANumberOfMembers < 2 || groupBNumberOfMembers < 2) {
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(tempRef, "At least two members in each group is required.");
                    return;
                }


                // start reading the iTRAQ ssv file

                ArrayList<Protein> allProteins = new ArrayList<Protein>();

                try {
                    FileReader f = new FileReader(ratioFile);
                    BufferedReader b = new BufferedReader(f);

                    String headerLine = b.readLine();

                    StringTokenizer tok = new StringTokenizer(headerLine, "\t");

                    HashMap<String, Integer> columnHeaders = new HashMap<String, Integer>();

                    int index = 0;

                    // parse the column header titles
                    while (tok.hasMoreTokens()) {
                        String token = tok.nextToken();

                        if (token.lastIndexOf("/") != -1) {
                            token = token.substring(token.lastIndexOf("/") + 1);
                        }

                        token = token.trim();
                        columnHeaders.put(token, index++);
                    }

                    String currentLine = b.readLine();

                    int rowCounter = 0;

                    // read the data lines
                    while (currentLine != null) {

                        rowCounter++;
                        progressDialog.setTitle("Loading Data. Please Wait... (" + rowCounter + ")");

                        tok = new StringTokenizer(currentLine, "\t");

                        Vector<String> rowValues = new Vector<String>();

                        // store the data for the currrent line
                        while (tok.hasMoreTokens()) {
                            rowValues.add(tok.nextToken());
                        }

                        int numExperimentsDetected = 0;
                        int numQuantificationRatios = 0;

                        for (int i = 0; i < numberOfExperiments; i++) {

                            int numUniquePeptides;

                            if (oldDataFormat) {
                                numUniquePeptides = new Integer(rowValues.get(
                                        columnHeaders.get("Exp. " + (i + 1) + " Unique Peps").intValue())).intValue();
                            } else {

                                if (columnHeaders.get("Exp" + (i + 1) + " unique_peptides") == null) {
                                    JOptionPane.showMessageDialog(null, "Unsupported data format!", "Format Error", JOptionPane.ERROR_MESSAGE);
                                    System.exit(0);
                                }

                                numUniquePeptides = new Integer(rowValues.get(
                                        columnHeaders.get("Exp" + (i + 1) + " unique_peptides").intValue())).intValue();
                            }

                            if (numUniquePeptides >= minNumUniquePeptides) {
                                numExperimentsDetected++;
                            }
                        }

                        ArrayList<Double> ratiosGroupA = new ArrayList<Double>();
                        ArrayList<Double> ratiosGroupB = new ArrayList<Double>();

                        ArrayList<Integer> numSpectraGroupA = new ArrayList<Integer>();
                        ArrayList<Integer> numPeptidesGroupA = new ArrayList<Integer>();

                        ArrayList<Integer> numSpectraGroupB = new ArrayList<Integer>();
                        ArrayList<Integer> numPeptidesGroupB = new ArrayList<Integer>();

                        int numUniquePeptides;
                        int numUniqueSpectra;

                        for (int i = 0; i < numberOfExperiments; i++) {

                            if (oldDataFormat) {
                                numUniquePeptides = new Integer(rowValues.get(
                                        columnHeaders.get("Exp. " + (i + 1) + " Unique Peps").intValue())).intValue();

                                numUniqueSpectra = new Integer(rowValues.get(
                                        columnHeaders.get("Exp. " + (i + 1) + " num Spectra").intValue())).intValue();
                            } else {
                                numUniquePeptides = new Integer(rowValues.get(
                                        columnHeaders.get("Exp" + (i + 1) + " unique_peptides").intValue())).intValue();

                                numUniqueSpectra = new Integer(rowValues.get(
                                        columnHeaders.get("Exp" + (i + 1) + " numSpectra").intValue())).intValue();
                            }

                            for (int j = 0; j < NUMBER_OF_ITRAQ_TAGS - 1; j++) {

                                if (columnHeaders.get("Exp" + (i + 1) + " iTRAQ_ratio_" + (j + 1)) != null // new formatting
                                        || columnHeaders.get("Exp. " + (i + 1) + " iTRAQ_" + (j + 1) + " log2 ratio") != null // old type formatting
                                        || columnHeaders.get("Exp" + (i + 1) + " log2_iTRAQ_" + (j + 1) + "_median") != null) {

                                    String temp;

                                    if (oldDataFormat) {
                                        temp = rowValues.get(
                                                columnHeaders.get("Exp. " + (i + 1) + " iTRAQ_" + (j + 1) + " log2 ratio").intValue());
                                    } else {
//                                        temp = rowValues.get(
//                                                columnHeaders.get("Exp" + (i + 1) + " iTRAQ_ratio_" + (j + 1)).intValue());
                                        temp = rowValues.get(
                                                columnHeaders.get("Exp" + (i + 1) + " log2_iTRAQ_" + (j + 1) + "_median").intValue());
                                    }


                                    temp = temp.replace(",", ".");

                                    double ratio = new Double(temp).doubleValue();

                                    if (experimentLabels[i][j] != null) {

                                        if (ratio != 0) { // not sure if this is the correct test for the old dataformat...
                                            // take log 2 of the ratio, NB: not needed for the old data format...
                                            if (!oldDataFormat) {
                                                //ratio = Math.log(ratio) / Math.log(2);
                                            }

                                            numQuantificationRatios++;
                                        } else {
                                            ratio = Double.MIN_VALUE;
                                        }

                                        if (numUniqueSpectra < minNumUniqueSpectra || numUniquePeptides < minNumUniquePeptides) {
                                            ratio = Double.MIN_VALUE;
                                        }

                                        if (ratio != Double.MIN_VALUE) {
                                            allRatios.get(i + "_" + j).add(ratio);
                                        }

                                        if (experimentLabels[i][j].equalsIgnoreCase(groupALabel)) {
                                            ratiosGroupA.add(ratio);
                                            numSpectraGroupA.add(numUniqueSpectra);
                                            numPeptidesGroupA.add(numUniquePeptides);
                                        } else if (experimentLabels[i][j].equalsIgnoreCase(groupBLabel)) {
                                            ratiosGroupB.add(ratio);
                                            numSpectraGroupB.add(numUniqueSpectra);
                                            numPeptidesGroupB.add(numUniquePeptides);
                                        }
                                    }
                                }
                            }
                        }

                        // add the wanted details to the protein list
                        if (numExperimentsDetected >= minNumberOfExperiments) {

                            String proteinName;

                            if (columnHeaders.get("entry_name").intValue() < rowValues.size()) {
                                proteinName = rowValues.get(columnHeaders.get("entry_name").intValue());
                            } else {
                                proteinName = rowValues.get(columnHeaders.get("accession_number").intValue());
                            }

                            String accessionNumber = rowValues.get(columnHeaders.get("accession_number").intValue());
                            String accessionNumbersAll = rowValues.get(columnHeaders.get("accession_numbers").intValue());

                            Integer numberUniquePeptides = new Integer(rowValues.get(columnHeaders.get("numPepsUnique").intValue()));
                            Double percentCoverage = new Double(rowValues.get(columnHeaders.get("percentCoverage").intValue()));

                            if (!removedProteins.contains(proteinName + "|" + accessionNumber)) {
                                allProteins.add(new Protein(ratiosGroupA, ratiosGroupB, numSpectraGroupA, numPeptidesGroupA,
                                        numSpectraGroupB, numPeptidesGroupB, accessionNumber, accessionNumbersAll,
                                        proteinName, numberUniquePeptides, numExperimentsDetected, numQuantificationRatios, percentCoverage));
                            }
                        }

                        currentLine = b.readLine();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Error while loading data:");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error while loading data:");
                    e.printStackTrace();
                }


                progressDialog.setTitle("Calculating Median Ratios. Please Wait...");

                // calculate the median ratios
                HashMap<String, Double> medianRatios = new HashMap<String, Double>();

                for (int i = 0; i < experimentLabels.length; i++) {
                    for (int j = 0; j < experimentLabels[0].length; j++) {

                        ArrayList<Double> currentRatios = allRatios.get(i + "_" + j);

                        if (currentRatios != null) {

                            double[] tempValues = new double[currentRatios.size()];

                            for (int k = 0; k < currentRatios.size(); k++) {
                                tempValues[k] = currentRatios.get(k);
                            }

                            medianRatios.put(i + "_" + j, StatUtils.percentile(tempValues, 50));
                            //System.out.println(medianRatios.get(i + "_" + j));
                        }
                    }
                }


                progressDialog.setTitle("Re-centering Proteins. Please Wait...");

                // re-center the proteins
                for (int i = 0; i < allProteins.size(); i++) {

                    Protein currentProtein = allProteins.get(i);

                    //System.out.println(currentProtein.getProteinName());

                    int groupACounter = 0;
                    int groupBCounter = 0;

                    ArrayList<Double> groupAValues = currentProtein.getRatiosGroupA();
                    ArrayList<Double> groupBValues = currentProtein.getRatiosGroupB();

                    for (int j = 0; j < experimentLabels.length; j++) {
                        for (int k = 0; k < experimentLabels[0].length; k++) {

                            if (experimentLabels[j][k] != null && experimentLabels[j][k].equalsIgnoreCase(groupALabel)) {
                                if (groupAValues.get(groupACounter) == Double.MIN_VALUE) {
                                    groupAValues.set(groupACounter, null);
                                } else {
                                    groupAValues.set(groupACounter, groupAValues.get(groupACounter) - medianRatios.get(j + "_" + k));
                                }

                                groupACounter++;
                            } else if (experimentLabels[j][k] != null && experimentLabels[j][k].equalsIgnoreCase(groupBLabel)) {

                                if (groupBValues.get(groupBCounter) == Double.MIN_VALUE) {
                                    groupBValues.set(groupBCounter, null);
                                } else {
                                    groupBValues.set(groupBCounter, groupBValues.get(groupBCounter) - medianRatios.get(j + "_" + k));
                                }

                                groupBCounter++;
                            }
                        }
                    }
                }

                progressDialog.setTitle("Comparing Groups. Please Wait...");

                // compare the two groups
                ArrayList<Protein> equallyExpressedProteins = new ArrayList<Protein>();
                ArrayList<Protein> differentiallyExpressedProteins = new ArrayList<Protein>();
                allValidProteins = new ArrayList<Protein>();
                ArrayList<Double> allValidFoldChanges = new ArrayList<Double>();

                for (int i = 0; i < allProteins.size(); i++) {

                    Protein currentProtein = allProteins.get(i);

                    ArrayList<Double> groupAValues = currentProtein.getRatiosGroupA();
                    ArrayList<Double> groupBValues = currentProtein.getRatiosGroupB();

                    int sampleACounter = 0;
                    int sampleBCounter = 0;

                    // find number of non-zero values
                    for (int j = 0; j < groupAValues.size(); j++) {
                        if (groupAValues.get(j) != null) {
                            sampleACounter++;
                        }
                    }

                    for (int j = 0; j < groupBValues.size(); j++) {
                        if (groupBValues.get(j) != null) {
                            sampleBCounter++;
                        }
                    }

                    double[] sampleA = new double[sampleACounter];
                    double[] sampleB = new double[sampleBCounter];

                    double averageSampleA = 0;
                    double averageSampleB = 0;

                    sampleACounter = 0;
                    sampleBCounter = 0;

                    for (int j = 0; j < groupAValues.size(); j++) {
                        if (groupAValues.get(j) != null) {
                            sampleA[sampleACounter++] = groupAValues.get(j);
                            averageSampleA += groupAValues.get(j);
                        }
                    }

                    for (int j = 0; j < groupBValues.size(); j++) {
                        if (groupBValues.get(j) != null) {
                            sampleB[sampleBCounter++] = groupBValues.get(j);
                            averageSampleB += groupBValues.get(j);
                        }
                    }

                    averageSampleA /= sampleACounter;
                    averageSampleB /= sampleBCounter;

                    // get the group difference
                    double groupDiff = (averageSampleA - averageSampleB);
                    double foldChange = getFoldChangeFromLog2(groupDiff);


                    // test if fold change is a number
                    if (!Double.isNaN(foldChange)) {

                        try {
                            currentProtein.setFoldChange(foldChange);

                            // require at least a minimum number of the experiments to have values
                            if (sampleACounter >= MINIMUM_NUMBER_OF_RATIOS_FOR_T_TEST && sampleBCounter >= MINIMUM_NUMBER_OF_RATIOS_FOR_T_TEST) {

                                allValidFoldChanges.add(groupDiff);

                                currentProtein.setPValue(TestUtils.homoscedasticTTest(sampleA, sampleB));

                                if (TestUtils.homoscedasticTTest(sampleA, sampleB, equallyExpressedSignificanceLevel)) {
                                    differentiallyExpressedProteins.add(currentProtein);
                                } else {

                                    if (!TestUtils.homoscedasticTTest(sampleA, sampleB, differentiallyExpressedSignificanceLevel)) {
                                        // store all the non-differentially expressed values
                                        equallyExpressedProteins.add(currentProtein);
                                    }
                                }

                                currentProtein.setGroupAPercent((double) sampleACounter / groupANumberOfMembers);
                                currentProtein.setGroupBPercent((double) sampleBCounter / groupBNumberOfMembers);

                                allValidProteins.add(currentProtein);
                            }

                        } catch (MathException e) {
                            System.out.println(currentProtein);
                            e.printStackTrace();
                        }
                    }
                }

                progressDialog.setTitle("Calculating q-values. Please Wait...");

                // create the random non-differentially expressed protein distribution
                final int NUMBER_OF_SWAPS = 10;
                final int NUMBER_OF_ITERATIONS = 100;

                ArrayList<Double> allNonDiffExpressedPValues = new ArrayList<Double>();

                for (int m = 0; m < NUMBER_OF_ITERATIONS; m++) {

                    for (int i = 0; i < equallyExpressedProteins.size(); i++) {

                        Protein currentProtein = equallyExpressedProteins.get(i);

                        ArrayList<Double> nonNullFromGroupA = currentProtein.getAllNonNullFromGroupA();
                        ArrayList<Double> nonNullFromGroupB = currentProtein.getAllNonNullFromGroupB();

                        int minSize = Math.min(nonNullFromGroupA.size(), nonNullFromGroupB.size());
                        int maxSize = Math.max(nonNullFromGroupA.size(), nonNullFromGroupB.size());

                        Random randomNumberGenerator = new Random();

                        for (int j = 0; j < NUMBER_OF_SWAPS; j++) {

                            if (nonNullFromGroupA.size() < nonNullFromGroupB.size()) {
                                int randomIndexGroup1 = randomNumberGenerator.nextInt(minSize);
                                int randomIndexGroup2 = randomNumberGenerator.nextInt(maxSize);

                                Double temp = nonNullFromGroupA.get(randomIndexGroup1);
                                nonNullFromGroupA.set(randomIndexGroup1, nonNullFromGroupB.get(randomIndexGroup2));
                                nonNullFromGroupB.set(randomIndexGroup2, temp);
                            } else {
                                int randomIndexGroup1 = randomNumberGenerator.nextInt(minSize);
                                int randomIndexGroup2 = randomNumberGenerator.nextInt(maxSize);

                                Double temp = nonNullFromGroupB.get(randomIndexGroup1);
                                nonNullFromGroupB.set(randomIndexGroup1, nonNullFromGroupA.get(randomIndexGroup2));
                                nonNullFromGroupA.set(randomIndexGroup2, temp);
                            }
                        }

                        // calculate new p-value
                        double[] sampleA = new double[nonNullFromGroupA.size()];
                        double[] sampleB = new double[nonNullFromGroupB.size()];

                        for (int j = 0; j < nonNullFromGroupA.size(); j++) {
                            sampleA[j] = nonNullFromGroupA.get(j);
                        }

                        for (int j = 0; j < nonNullFromGroupB.size(); j++) {
                            sampleB[j] = nonNullFromGroupB.get(j);
                        }

                        try {
                            allNonDiffExpressedPValues.add(TestUtils.homoscedasticTTest(sampleA, sampleB));
                        } catch (MathException e) {
                            System.out.println("Math Exception: " + currentProtein);
                            e.printStackTrace();
                        }
                    }
                }


                // sort the non differentially expressed p-values
                java.util.Collections.sort(allNonDiffExpressedPValues);

//        // print the sorted p-values
//        for (int i = 0; i < allNonDiffExpressedPValues.size(); i++) {
//            System.out.println(allNonDiffExpressedPValues.get(i));
//        }


                // sort the valid proteins by p-value
                java.util.Collections.sort(allValidProteins);


                // find the q-values
                for (int i = 0; i < allValidProteins.size(); i++) {

                    Protein currentProtein = allValidProteins.get(i);

                    int qValueIndex = 0;
                    double qValue = 1;

                    if (currentProtein.getPValue() != null) {

                        while (qValueIndex < allNonDiffExpressedPValues.size() && allNonDiffExpressedPValues.get(qValueIndex) < currentProtein.getPValue()) {
                            qValueIndex++;
                        }

                        qValue = ((double) ((qValueIndex + 1) * allValidProteins.size())) / (allNonDiffExpressedPValues.size() * (i + 1));

                        if (qValue > 1) {
                            qValue = 1;
                        }
                    }

                    currentProtein.setqValue(qValue);
                }


                // calculate the "true" q-values
                for (int index = 0; index < allValidProteins.size(); index++) {

                    int newIndex = findIndexOfSmallestValueFromIndex(index, allValidProteins);
                    double smallestValue = allValidProteins.get(newIndex).getQValue();

                    for (int j = index; j <= newIndex; j++) {
                        allValidProteins.get(j).setqValue(smallestValue);
                    }

                    index = newIndex;
                }

                if (allValidFoldChanges.size() > 0) {
                    createFoldChangeHistogram(allValidFoldChanges);
                }

                double maxAbsoluteValueFoldChange = 0.0;
                double maxPeptideCount = 0;

                // add the proteins to the results table
                for (int i = 0; i < allValidProteins.size(); i++) {
                    Protein currentProtein = allValidProteins.get(i);

                    boolean selected = false;

                    if (selectedProteins.contains(currentProtein.getProteinName() + " " + currentProtein.getAccessionNumber())) {
                        selected = true;
                    }

                    String tempAccessionNumber = currentProtein.getAccessionNumber();

                    if (tempAccessionNumber.toUpperCase().startsWith("IPI")) {
                        //tempAccessionNumber = "<html><u>" + tempAccessionNumber + "</u></html>"; // @TODO: support more databases

                        tempAccessionNumber = "<html><a href=\"http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-e+%5bIPI-AccNumber:"
                                + tempAccessionNumber + "%5d"
                                + "\"><font color=\"" + notSelectedRowHtmlTagFontColor + "\">"
                                + tempAccessionNumber + "</font></a></html>";
                    } else { // uniprot assumed
                        tempAccessionNumber = "<html><a href=\"" + "http://www.uniprot.org/uniprot/" + tempAccessionNumber
                                + "\"><font color=\"" + notSelectedRowHtmlTagFontColor
                                + "\">" + tempAccessionNumber + "</font></a></html>";
                    }

                    ((DefaultTableModel) resultsJTable.getModel()).addRow(
                            new Object[]{
                                new Integer(i + 1),
                                currentProtein.getProteinName(),
                                tempAccessionNumber,
                                new XYDataPoint(currentProtein.getFoldChange(), currentProtein.getPValue()),
                                currentProtein.getNumberUniquePeptides(),
                                currentProtein.getPercentCoverage(),
                                currentProtein.getNumExperimentsDetected(),
                                currentProtein.getNumQuantificationRatios(),
                                currentProtein.getPValue(),
                                currentProtein.getQValue(),
                                currentProtein.getPValue() < equallyExpressedSignificanceLevel,
                                currentProtein.getPValue() < equallyExpressedSignificanceLevel / allValidProteins.size(),
                                selected
                            });

                    if (Math.abs(currentProtein.getFoldChange()) > maxAbsoluteValueFoldChange) {
                        maxAbsoluteValueFoldChange = Math.abs(currentProtein.getFoldChange());
                    }

                    if (currentProtein.getNumberUniquePeptides() > maxPeptideCount) {
                        maxPeptideCount = currentProtein.getNumberUniquePeptides();
                    }
                }

                if (resultsJTable.getRowCount() > 0) {
                    ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setMaxValue(Math.ceil(maxAbsoluteValueFoldChange));
                    ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setMinValue(-Math.ceil(maxAbsoluteValueFoldChange));
                    ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Peptides").getCellRenderer()).setMaxValue(maxPeptideCount);
                    ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Exp. Count").getCellRenderer()).setMaxValue(numberOfExperiments);
                    ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Quant. Count").getCellRenderer()).setMaxValue(groupANumberOfMembers + groupBNumberOfMembers);

                    valuesAndChartJCheckBoxMenuItemActionPerformed(null);

                    resultsJTable.setRowSelectionInterval(0, 0);
                    resultsJTableMouseReleased(null);
                }

                ((TitledBorder) resultsJPanel.getBorder()).setTitle("Results (" + resultsJTable.getRowCount() + ")");
                resultsJPanel.revalidate();
                resultsJPanel.repaint();

                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultsJTable.getModel());
                resultsJTable.setRowSorter(sorter);

                ResultsFilter filter = new ResultsFilter(tempRef, false, currentFilterValues, currrentFilterRadioButtonSelections, foldChangeAbsoluteValue, false);
                filter.filter();
                filter.dispose();

                exportPlotJButton.setEnabled(true);

                progressDialog.setVisible(false);
                progressDialog.dispose();
            }
        }.start();
    }

    /**
     * Creates a histograms of the fold changes.
     *
     * @param allValidFoldChanges all used fold changes as a list
     */
    private void createFoldChangeHistogram(ArrayList<Double> allValidFoldChanges) {

        HistogramDataset tempDataset = new HistogramDataset();
        tempDataset.setType(HistogramType.RELATIVE_FREQUENCY);

        SummaryStatistics stats = new SummaryStatistics();

        double[] tempValues = new double[allValidFoldChanges.size()];

        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;

        for (int i = 0; i < allValidFoldChanges.size(); i++) {

            if (!allValidFoldChanges.get(i).isNaN()) {
                stats.addValue(allValidFoldChanges.get(i).doubleValue());
                tempValues[i] = allValidFoldChanges.get(i).doubleValue();

                if (tempValues[i] > maxValue) {
                    maxValue = tempValues[i];
                }

                if (tempValues[i] < minValue) {
                    minValue = tempValues[i];
                }
            }
        }

        double maxAbsValue = Math.max(Math.abs(minValue), Math.abs(maxValue));

        tempDataset.addSeries("FoldChange", tempValues, 100, -maxAbsValue, maxAbsValue); // @TODO: bin size set by the user

        JFreeChart chart = ChartFactory.createHistogram(null, "Fold Change (log 2)", "Frequency",
                tempDataset, PlotOrientation.VERTICAL, false, true, false);

        foldChangeChartPanel = new ChartPanel(chart);

        foldChangeplot = chart.getXYPlot();

        foldChangeplot.setOutlineVisible(false);

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(140, 140, 140));
        foldChangeplot.setRenderer(renderer);

        foldChangeplot.setBackgroundPaint(Color.WHITE);
        foldChangeChartPanel.setBackground(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        foldChangeChartJPanel.removeAll();
        foldChangeChartJPanel.add(foldChangeChartPanel);


        // add markers for 2 SDs
        double lower2SD = StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2;
        double upper2SD = StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2;

        IntervalMarker marker2SD = new IntervalMarker(lower2SD, upper2SD, new Color(0f, 1f, 0f, 0.1f));
        marker2SD.setLabel("2SD");
        marker2SD.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        foldChangeplot.addDomainMarker(marker2SD, Layer.BACKGROUND);

        // add markers for 1 SDs
        double lower1SD = StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation();
        double upper1SD = StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation();

        IntervalMarker marker1SD = new IntervalMarker(lower1SD, upper1SD, new Color(0f, 0f, 1f, 0.1f));
        marker1SD.setLabel("1SD");
        marker1SD.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        foldChangeplot.addDomainMarker(marker1SD, Layer.BACKGROUND);


        // add annotation of the median and standard deviations
        foldChangeplot.addAnnotation(new XYTextAnnotation(
                "Median: " + Util.roundDouble(StatUtils.percentile(tempValues, 50), 2),
                foldChangeplot.getDomainAxis().getUpperBound() * 0.75,
                foldChangeplot.getRangeAxis().getUpperBound() * 0.96));

        foldChangeplot.addAnnotation(new XYTextAnnotation(
                "1SD: [" + Util.roundDouble(StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation(), 2)
                + ", " + Util.roundDouble(StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation(), 2) + "]",
                foldChangeplot.getDomainAxis().getUpperBound() * 0.75,
                foldChangeplot.getRangeAxis().getUpperBound() * 0.90));

        foldChangeplot.addAnnotation(new XYTextAnnotation(
                "2SD: [" + Util.roundDouble(StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2, 2)
                + ", " + Util.roundDouble(StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2, 2) + "]",
                foldChangeplot.getDomainAxis().getUpperBound() * 0.75,
                foldChangeplot.getRangeAxis().getUpperBound() * 0.84));

        sd1JCheckBoxMenuItemActionPerformed(null);
        sd2JCheckBoxMenuItemActionPerformed(null);


//        if ((StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2) > 0) {
//            lowerFoldChangeBoundary = Math.pow(2, (StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2));
//        } else {
//            lowerFoldChangeBoundary = -Math.pow(2, -(StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2));
//        }
//
//        if ((StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2) > 0) {
//            upperFoldChangeBoundary = Math.pow(2, (StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2));
//        } else {
//            upperFoldChangeBoundary = -Math.pow(2, -(StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2));
//        }

        //sdJLabel.setText("2SD: [" + Util.roundDouble(lowerFoldChangeBoundary, 2) + " - " + Util.roundDouble(upperFoldChangeBoundary, 2) + "]");
    }

    /**
     * Find the smallest value in the list starting from a given index.
     *
     * @param startIndex the index to start the search from
     * @param allValidProteins the list of values to find the value in
     * @return the index of the smallest value after the given index
     */
    private static int findIndexOfSmallestValueFromIndex(int startIndex, ArrayList<Protein> allValidProteins) {

        double smallestValue = allValidProteins.get(startIndex).getQValue();
        int indexOfSmallestValue = startIndex;

        startIndex++;

        while (startIndex < allValidProteins.size()) {
            if (allValidProteins.get(startIndex).getQValue() < smallestValue) {
                smallestValue = allValidProteins.get(startIndex).getQValue();
                indexOfSmallestValue = startIndex;
            }

            startIndex++;
        }

        return indexOfSmallestValue;
    }

    /**
     * Returns the current text filter values.
     *
     * @return the current text filter values
     */
    public String[] getCurrentFilterValues() {
        return currentFilterValues;
    }

    /**
     * Set the current text filter values.
     *
     * @param currentFilterValues the filter values to set
     */
    public void setCurrentFilterValues(String[] currentFilterValues) {
        this.currentFilterValues = currentFilterValues;
    }

    /**
     * Returns the current filter radio button settings.
     *
     * @return the current filter radio button settings
     */
    public Integer[] getCurrrentFilterRadioButtonSelections() {
        return currrentFilterRadioButtonSelections;
    }

    /**
     * Set the current filter radio button settings.
     *
     * @param currrentFilterRadioButtonSelections the filter radio buttons to
     * set
     */
    public void setCurrrentFilterRadioButtonSelections(Integer[] currrentFilterRadioButtonSelections) {
        this.currrentFilterRadioButtonSelections = currrentFilterRadioButtonSelections;
    }

    /**
     * Sets if the filter should take the absolute value of the fold change.
     *
     * @param foldChangeAbsoluteValue
     */
    public void setFilterFoldChangeAbsoluteValue(boolean foldChangeAbsoluteValue) {
        this.foldChangeAbsoluteValue = foldChangeAbsoluteValue;
    }

    /**
     * Returns the color for group A.
     *
     * @return the groupAColor
     */
    public Color getGroupAColor() {
        return groupAColor;
    }

    /**
     * Set the color for groups A.
     *
     * @param groupAColor the groupAColor to set
     */
    public void setGroupAColor(Color groupAColor) {
        this.groupAColor = groupAColor;
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setPositiveValuesColor(groupAColor);
    }

    /**
     * Returns the color for group B.
     *
     * @return the groupBColor
     */
    public Color getGroupBColor() {
        return groupBColor;
    }

    /**
     * Set the color for group B.
     *
     * @param groupBColor the groupBColor to set
     */
    public void setGroupBColor(Color groupBColor) {
        this.groupBColor = groupBColor;
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setNegativeValuesColor(groupBColor);
    }

    /**
     * Returns the color to use for the average value bar for the given group
     * color.
     *
     * @param groupColor the original color for the group
     * @return the color to use for the average bar
     */
    private Color getAverageValueColor(Color groupColor) {

        int red = groupColor.getRed();
        int green = groupColor.getGreen();
        int blue = groupColor.getBlue();

        if (green + 150 < 255) {
            green += 150;
        } else {
            green = 255;
        }

        if (red + 150 < 255) {
            red += 150;
        } else {
            red = 255;
        }

        if (blue + 150 < 255) {
            blue += 150;
        } else {
            blue = 255;
        }

        // make sure that the bar is not white
        if (red == 255 && blue == 255 && green == 255) {
            red = 225;
            blue = 225;
            green = 225;
        }

        return new Color(red, green, blue);
    }

    /**
     * Returns the group fold change from of the provided log 2 group
     * difference.
     *
     * @param log2Value the value get the gold change for
     * @return the fold change
     */
    private double getFoldChangeFromLog2(double log2Value) {

        double value;

        if (log2Value > 0) {
            value = Math.pow(2, log2Value);
        } else {
            value = -Math.pow(2, -log2Value);
        }

        return value;
    }

    /**
     * Returns the "antilog" of the provided value.
     *
     * @param log2Value the value to take the "antilog" of
     * @return the "antilogged" value
     */
    private double antiLog2(double log2Value) {
        return Math.pow(2, log2Value);
    }

    /**
     * Removes the fold change markers.
     */
    private void removeFoldChangeMarkers() {
        if (foldChangeplot != null && foldChangeplot.getDomainMarkers(Layer.FOREGROUND) != null) {

            Iterator iterator = foldChangeplot.getDomainMarkers(Layer.FOREGROUND).iterator();

            // store the keys in a list first to escape a ConcurrentModificationException
            ArrayList<IntervalMarker> tempMarkers = new ArrayList<IntervalMarker>();

            while (iterator.hasNext()) {
                tempMarkers.add((IntervalMarker) iterator.next());
            }

            for (int i = 0; i < tempMarkers.size(); i++) {
                foldChangeplot.removeDomainMarker(tempMarkers.get(i));
            }
        }
    }

    /**
     * Remove the data point annotation in the fold change plot.
     */
    private void removeDataPointAnnotations() {
        if (foldChangeplot != null && foldChangeplot.getAnnotations() != null) {

            Iterator iterator = foldChangeplot.getAnnotations().iterator();

            // store the keys in a list first to escape a ConcurrentModificationException
            ArrayList<XYTextAnnotation> tempAnnotations = new ArrayList<XYTextAnnotation>();

            while (iterator.hasNext()) {
                tempAnnotations.add((XYTextAnnotation) iterator.next());
            }

            for (int i = 0; i < tempAnnotations.size(); i++) {
                if (tempAnnotations.get(i).getText().startsWith("Current: ")) {
                    foldChangeplot.removeAnnotation(tempAnnotations.get(i));
                }
            }
        }
    }

    @Override
    public void cancelProgress() {
        // do nothing
    }

    @Override
    public void selectionChanged(Selection.TYPE type) {

        if (type == Selection.TYPE.OF_COLUMNS) {
            // do nothing
        } else {

            int[] selectedRows = SelectionManager.getSelectionManager().getSelectedRows(dataset).getMembers();

            if (selectedRows != null) {

                if (!arraysContainsTheSameNumbers(resultsJTable.getSelectedRows(), SelectionManager.getSelectionManager().getSelectedRows(dataset).getMembers())) {

                    // remove old selection
                    resultsJTable.clearSelection();

                    for (int i = 0; i < selectedRows.length; i++) {
                        resultsJTable.addRowSelectionInterval(selectedRows[i], selectedRows[i]);
                    }

                    resultsJTableMouseReleased(null);
                }
            }
        }
    }

    /**
     * Returns true if the integers contained in the two lists are equal. Note
     * that the order of the numbers are ignored.
     *
     * @param listA
     * @param listB
     * @return
     */
    private boolean arraysContainsTheSameNumbers(int[] listA, int[] listB) {

        if (listA == null && listB == null) {
            return true;
        }

        if (listA == null || listB == null) {
            return false;
        }

        if (listA.length != listB.length) {
            return false;
        }

        ArrayList<Integer> arrayA = new ArrayList<Integer>(listA.length);
        ArrayList<Integer> arrayB = new ArrayList<Integer>(listB.length);

        java.util.Collections.sort(arrayA);
        java.util.Collections.sort(arrayB);

        return Arrays.equals(arrayA.toArray(), arrayB.toArray());
    }
}

class SWorkerThread extends SwingWorker<ClusterResults, Integer> {

    private SOMClustCompute som;

    public SWorkerThread(SOMClustCompute som) {
        this.som = som;
    }

    @Override
    protected ClusterResults doInBackground() throws Exception {
        return som.runClustering();
    }
}