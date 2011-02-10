package no.uib.mitraq.gui;

import no.uib.mitraq.util.Protein;
import no.uib.mitraq.util.BareBonesBrowserLaunch;
import no.uib.mitraq.util.TsvFileFilter;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import no.uib.jsparklines.data.XYDataPoint;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.extra.TrueFalseIconRenderer;
import no.uib.jsparklines.renderers.util.BarChartColorRenderer;
import no.uib.jsparklines.renderers.util.StatisticalBarChartColorRenderer;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.inference.TestUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 * MiTRAQ main frame.
 *
 * @author Harald Barsnes
 */
public class MiTRAQ extends javax.swing.JFrame {

    /**
     * Set to true if using the old data output format.
     */
    private boolean oldDataFormat = false;
    /**
     * The significance level to use for the t-test.
     */
    private double equallyExpressedSignificanceLevel = 0.05;
    /**
     * t-test scoring higher than this value are considered differentially expressed
     * and just in the q-value calculation.
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
    private String[] currentFilterValues = {"", "", "", "", "", "", ""};
    /**
     * The current settings for the radio buttons for the filters.
     */
    private Integer[] currrentFilterRadioButtonSelections = {0, 0, 0, 0, 2, 2};
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
     * If true error bars are shown for the average value bar in the plot.
     */
    private boolean showErrorBars = true;
    /**
     * If true background hightlighting is added to the average value bars.
     */
    private boolean highlightAverageBars = false;
    /**
     * If true each bar in the bar chart will be labelled with its value.
     */
    private boolean showBarChartLabels = true;
    /**
     * The current chart panel.
     */
    private ChartPanel chartPanel = null;
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

        setLocationRelativeTo(null);
    }

    /**
     * Sets up the results table.
     */
    private void setUpResultsTable() {

        // sparklines cell renderers
        resultsJTable.getColumn("FC").setCellRenderer(new JSparklinesBarChartTableCellRenderer(
                PlotOrientation.HORIZONTAL, -1.0, 1.0, groupBColor, groupAColor, Color.GRAY, new Double(significanceLevelJSpinner.getValue().toString())));
        resultsJTable.getColumn("Peptides").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, true));
        resultsJTable.getColumn("Coverage").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 100.0, true));
        resultsJTable.getColumn("Exp. Count").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, true));
        resultsJTable.getColumn("P-value").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, false));
        resultsJTable.getColumn("Q-value").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, 1.0, false));

        // add the true/false cell renderer
        resultsJTable.getColumn("Significant").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")), new ImageIcon(this.getClass().getResource("/icons/Error_3.png"))));
        resultsJTable.getColumn("Bonferroni").setCellRenderer(new TrueFalseIconRenderer(
                new ImageIcon(this.getClass().getResource("/icons/accept.png")), new ImageIcon(this.getClass().getResource("/icons/Error_3.png"))));

        // turn off column reordering
        resultsJTable.getTableHeader().setReorderingAllowed(false);

        // enable sorting by clicking on the column headers
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultsJTable.getModel());
        resultsJTable.setRowSorter(sorter);

        // set the widths of the columns
        resultsJTable.getColumn(" ").setMaxWidth(40);
        resultsJTable.getColumn("FC").setMaxWidth(70);
        resultsJTable.getColumn("P-value").setMaxWidth(70);
        resultsJTable.getColumn("Q-value").setMaxWidth(70);
        resultsJTable.getColumn("Peptides").setMaxWidth(80);
        resultsJTable.getColumn("Coverage").setMaxWidth(80);
        resultsJTable.getColumn("Exp. Count").setMaxWidth(80);
        resultsJTable.getColumn("Significant").setMaxWidth(80);
        resultsJTable.getColumn("Bonferroni").setMaxWidth(80);

        resultsJTable.getColumn(" ").setMinWidth(40);
        resultsJTable.getColumn("FC").setMinWidth(70);
        resultsJTable.getColumn("P-value").setMinWidth(70);
        resultsJTable.getColumn("Q-value").setMinWidth(70);
        resultsJTable.getColumn("Peptides").setMinWidth(80);
        resultsJTable.getColumn("Coverage").setMinWidth(80);
        resultsJTable.getColumn("Exp. Count").setMinWidth(80);
        resultsJTable.getColumn("Significant").setMinWidth(80);
        resultsJTable.getColumn("Bonferroni").setMinWidth(80);

        // set the column header tooltips
        columnHeaderToolTips = new Vector();
        columnHeaderToolTips.add(null);
        columnHeaderToolTips.add("Protein Description");
        columnHeaderToolTips.add("Fold Change - Group 1 / Group 2");
        columnHeaderToolTips.add("Number of Unique Peptides");
        columnHeaderToolTips.add("Sequence Coverage");
        columnHeaderToolTips.add("Experiment Counter");
        columnHeaderToolTips.add("P-value for t-test");
        columnHeaderToolTips.add("Q-value");
        columnHeaderToolTips.add("Significant/Not Significant t-test");
        columnHeaderToolTips.add("Significant/Not Significant t-test - Bonferroni Corrected");
    }

    /**
     * Set up the log file.
     */
    private void setUpLogFile() {
        if (useLogFile && !getJarFilePath().equalsIgnoreCase(".")) {
            try {
                String path = getJarFilePath() + "/conf/MiTRAQLog.txt";

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
                        null, "An error occured when trying to create the MiTRAQLog.",
                        "Error Creating Log File", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        String path = this.getClass().getResource("MiTRAQ.class").getPath();

        if (path.lastIndexOf("/MiTRAQ-") != -1) {
            path = path.substring(5, path.lastIndexOf("/MiTRAQ-"));
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
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("mitraq.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("mitraq.version");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        proteinCountJLabel = new javax.swing.JLabel();
        significanceLevelJLabel = new javax.swing.JLabel();
        significanceLevelJSpinner = new javax.swing.JSpinner();
        filterResultsJButton = new javax.swing.JButton();
        exportProteinListJButton = new javax.swing.JButton();
        chartJPanel = new javax.swing.JPanel();
        exportPlotJButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileJMenu = new javax.swing.JMenu();
        openJMenuItem = new javax.swing.JMenuItem();
        exitJMenuItem = new javax.swing.JMenuItem();
        viewJMenu = new javax.swing.JMenu();
        viewSparklinesJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        errorBarsJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        highlightAveragesJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        barChartLabelsJCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpJMenu = new javax.swing.JMenu();
        helpJMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MiTRAQ - Multiple iTRAQ Data Analysis");
        setMinimumSize(new java.awt.Dimension(800, 700));

        resultsJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results"));

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
        resultsJSplitPane.setDividerSize(0);
        resultsJSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        resultsJSplitPane.setResizeWeight(1.0);

        resultsJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein", "FC", "Peptides", "Coverage", "Exp. Count", "P-value", "Q-value", "Significant", "Bonferroni"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, XYDataPoint.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultsJTableMouseClicked(evt);
            }
        });
        resultsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resultsJTableKeyReleased(evt);
            }
        });
        resultsTableJScrollPane.setViewportView(resultsJTable);

        proteinCountJLabel.setFont(proteinCountJLabel.getFont().deriveFont((proteinCountJLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        proteinCountJLabel.setText("Protein Count: -");

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

        javax.swing.GroupLayout resultsTableJPanelLayout = new javax.swing.GroupLayout(resultsTableJPanel);
        resultsTableJPanel.setLayout(resultsTableJPanelLayout);
        resultsTableJPanelLayout.setHorizontalGroup(
            resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsTableJPanelLayout.createSequentialGroup()
                .addComponent(proteinCountJLabel)
                .addGap(41, 41, 41)
                .addComponent(significanceLevelJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(significanceLevelJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 725, Short.MAX_VALUE)
                .addComponent(filterResultsJButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportProteinListJButton))
            .addComponent(resultsTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1133, Short.MAX_VALUE)
        );

        resultsTableJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {exportProteinListJButton, filterResultsJButton});

        resultsTableJPanelLayout.setVerticalGroup(
            resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsTableJPanelLayout.createSequentialGroup()
                .addComponent(resultsTableJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsTableJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportProteinListJButton)
                    .addComponent(filterResultsJButton)
                    .addComponent(proteinCountJLabel)
                    .addComponent(significanceLevelJSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(significanceLevelJLabel))
                .addContainerGap())
        );

        resultsJSplitPane.setTopComponent(resultsTableJPanel);

        chartJPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chartJPanel.setMaximumSize(new java.awt.Dimension(4, 200));
        chartJPanel.setLayout(new javax.swing.BoxLayout(chartJPanel, javax.swing.BoxLayout.LINE_AXIS));
        resultsJSplitPane.setRightComponent(chartJPanel);

        exportPlotJButton.setText("<html>\n<p align=center>\nExport<br>Plot\n</p>\n</html>");
        exportPlotJButton.setToolTipText("Export the plot to file");
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
                    .addComponent(resultsJSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1133, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsJPanelLayout.createSequentialGroup()
                        .addComponent(accessiobNumbersJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1062, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPlotJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        resultsJPanelLayout.setVerticalGroup(
            resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(accessiobNumbersJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportPlotJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE))
                .addContainerGap())
        );

        resultsJPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {accessiobNumbersJScrollPane, exportPlotJButton});

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

        exitJMenuItem.setText("Exit");
        exitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitJMenuItemActionPerformed(evt);
            }
        });
        fileJMenu.add(exitJMenuItem);

        menuBar.add(fileJMenu);

        viewJMenu.setMnemonic('V');
        viewJMenu.setText("View");

        viewSparklinesJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
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

        barChartLabelsJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        barChartLabelsJCheckBoxMenuItem.setMnemonic('B');
        barChartLabelsJCheckBoxMenuItem.setSelected(true);
        barChartLabelsJCheckBoxMenuItem.setText("Bar Chart Labels");
        barChartLabelsJCheckBoxMenuItem.setToolTipText("Show the labels for the bar charts");
        barChartLabelsJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barChartLabelsJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        viewJMenu.add(barChartLabelsJCheckBoxMenuItem);

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(resultsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Updates the results table selection.
     */
    public void updateResultTableSelection() {
        if (resultsJTable.getRowCount() > 0) {
            resultsJTableMouseClicked(null);
        } else {
            chartJPanel.removeAll();

            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    chartJPanel.repaint();
                }
            });
        }

        proteinCountJLabel.setText("Protein Count: " + resultsJTable.getRowCount());
    }

    /**
     * Updates the ratio plot according to the currently selected row in the
     * protein table.
     *
     * @param evt
     */
    private void resultsJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsJTableMouseClicked

        if (resultsJTable.getSelectedRow() != -1) {

            int index = new Integer("" + resultsJTable.getValueAt(resultsJTable.getSelectedRow(), 0)) - 1;
            Protein currentProtein = allValidProteins.get(index);
            StringTokenizer tok = new StringTokenizer(currentProtein.getAccessionNumbersAll(), "|");

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            DefaultStatisticalCategoryDataset datasetErrors = new DefaultStatisticalCategoryDataset();

            String accessionNumberLinks = "<html>Accession Numbers: ";

            while (tok.hasMoreTokens()) {

                String currentAccessionNumber = tok.nextToken();
                String database = null;

                if (currentAccessionNumber.toUpperCase().startsWith("IPI")) {
                    database = "IPI";
                } else if (currentAccessionNumber.toUpperCase().startsWith("SWISS-PROT")
                        || currentAccessionNumber.startsWith("UNI-PROT")) {  // @TODO: untested!!
                    database = "UNI-PROT";
                }

                // @TODO: add more databases

                if (database != null) {
                    accessionNumberLinks += "<a href=\"http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-e+%5b"
                            + database + "-AccNumber:" + currentAccessionNumber
                            + "%5d\">" + currentAccessionNumber + "</a>, ";
                } else {
                    accessionNumberLinks += currentAccessionNumber + ", ";
                }
            }

            accessionNumberLinks = accessionNumberLinks.substring(0, accessionNumberLinks.length() - 2);
            accessionNumbersJEditorPane.setText(accessionNumberLinks + "</html>");

            SummaryStatistics stats = new SummaryStatistics();

            // add bars for the data values in group A
            for (int i = 0; i < currentProtein.getRatiosGroupA().size(); i++) {
                if (currentProtein.getRatiosGroupA().get(i) != null) {
                    dataset.addValue(currentProtein.getRatiosGroupA().get(i), "1", groupALabel + (i + 1));
                    datasetErrors.add(null, null, "1", groupALabel + (i + 1));
                    stats.addValue(currentProtein.getRatiosGroupA().get(i));
                } else {
                    dataset.addValue(0, "1", groupALabel + (i + 1));
                    datasetErrors.add(null, null, "1", groupALabel + (i + 1));
                }
            }

            // add a bar for the average value in group A
            dataset.addValue(stats.getMean(), "1", groupALabel + " Avg");
            datasetErrors.add(stats.getMean(), stats.getStandardDeviation(), "1", groupALabel + " Avg");

            stats = new SummaryStatistics();

            // add a bar for the average value in group B
            for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                if (currentProtein.getRatiosGroupB().get(i) != null) {
                    stats.addValue(currentProtein.getRatiosGroupB().get(i));
                }
            }

            dataset.addValue(stats.getMean(), "1", groupBLabel + " Avg");
            datasetErrors.add(stats.getMean(), stats.getStandardDeviation(), "1", groupBLabel + " Avg");

            // add bars for the data values in group B
            for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                if (currentProtein.getRatiosGroupB().get(i) != null) {
                    dataset.addValue(currentProtein.getRatiosGroupB().get(i), "1", groupBLabel + (i + 1));
                    datasetErrors.add(null, null, "1", groupBLabel + (i + 1));
                } else {
                    dataset.addValue(0, "1", groupBLabel + (i + 1));
                    datasetErrors.add(null, null, "1", groupBLabel + (i + 1));
                }
            }

            // set up the bar colors
            ArrayList<Color> barColors = new ArrayList<Color>();

            // set the colors for the group A bars
            for (int i = 0; i < currentProtein.getRatiosGroupA().size(); i++) {
                barColors.add(groupAColor);
            }

            // set the color for the average group A bar
            barColors.add(getAverageValueColor(groupAColor));

            // set the color for the average group B bar
            barColors.add(getAverageValueColor(groupBColor));

            // set the colors for the group B bars
            for (int i = 0; i < currentProtein.getRatiosGroupB().size(); i++) {
                barColors.add(groupBColor);
            }

            String title = currentProtein.getProteinName() + " (" + currentProtein.getAccessionNumber() + ")";

            JFreeChart chart = createRatioChart(dataset, datasetErrors, title, barColors);

            if (highlightAverageBars) {
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                plot.addDomainMarker(new CategoryMarker(groupALabel + " Avg", Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);
                plot.addDomainMarker(new CategoryMarker(groupBLabel + " Avg", Color.LIGHT_GRAY, new BasicStroke(1.0f), Color.LIGHT_GRAY, new BasicStroke(1.0f), 0.2f), Layer.BACKGROUND);
            }

            chartPanel = new ChartPanel(chart);
            chartJPanel.removeAll();
            chartJPanel.add(chartPanel);
            chartJPanel.validate();
        }
    }//GEN-LAST:event_resultsJTableMouseClicked

    /**
     * Updates the ratio plot according to the currently selected row in the
     * protein table.
     *
     * @param evt
     */
    private void resultsJTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultsJTableKeyReleased
        resultsJTableMouseClicked(null);
    }//GEN-LAST:event_resultsJTableKeyReleased

    /**
     * Opens a dialog with the results filter options.
     *
     * @param evt
     */
    private void filterResultsJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterResultsJButtonActionPerformed
        new ResultsFilter(this, false, currentFilterValues, currrentFilterRadioButtonSelections);
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
     * currently selected significance level. And update the color coding
     * in the fold change plot.
     *
     * @param evt
     */
    private void significanceLevelJSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_significanceLevelJSpinnerStateChanged
        equallyExpressedSignificanceLevel = new Double(significanceLevelJSpinner.getValue().toString());
        differentiallyExpressedSignificanceLevel = new Double(significanceLevelJSpinner.getValue().toString());

        for (int i = 0; i < ((DefaultTableModel) resultsJTable.getModel()).getRowCount(); i++) {
            double pValue = (Double) ((DefaultTableModel) resultsJTable.getModel()).getValueAt(i, resultsJTable.getColumn("P-value").getModelIndex());
            ((DefaultTableModel) resultsJTable.getModel()).setValueAt(pValue < equallyExpressedSignificanceLevel, i, resultsJTable.getColumn("Significant").getModelIndex());
            ((DefaultTableModel) resultsJTable.getModel()).setValueAt(pValue < equallyExpressedSignificanceLevel / allValidProteins.size(), i, resultsJTable.getColumn("Bonferroni").getModelIndex());
        }

        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setSignificanceLevel(differentiallyExpressedSignificanceLevel);

        resultsJTable.revalidate();
        resultsJTable.repaint();
    }//GEN-LAST:event_significanceLevelJSpinnerStateChanged

    /**
     * Tries to export the currently shown proteins in the results tabel to a
     * tab separated text file.
     *
     * @param evt
     */
    private void exportProteinListJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportProteinListJButtonActionPerformed

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

                    w.write("\nIndex\tProtein Description\tAccession Number\tAccession Numbers\tUnique Peptides\t"
                            + "Coverage\tExperiment Counter\tFold Change\tP-value\tQ-value\tSignificant\tBonferroni\t");

                    if (resultsJTable.getRowCount() > 0) {

                        int index = new Integer("" + resultsJTable.getValueAt(0, 0)) - 1;
                        Protein firstProtein = allValidProteins.get(index);

                        for (int i = 0; i < firstProtein.getRatiosGroupA().size(); i++) {
                            w.write("Ratio Log2 " + groupALabel + (i + 1) + "\t");
                        }

                        for (int i = 0; i < firstProtein.getRatiosGroupB().size(); i++) {
                            w.write("Ratio Log2 " + groupBLabel + (i + 1) + "\t");
                        }
                    }

                    w.write("\n");

                    this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

                    for (int i = 0; i < resultsJTable.getRowCount(); i++) {

                        int index = new Integer("" + resultsJTable.getValueAt(i, 0)) - 1;
                        Protein currentProtein = allValidProteins.get(index);

                        w.write((index + 1) + "\t" + currentProtein.getProteinName() + "\t" + currentProtein.getAccessionNumber()
                                + "\t" + currentProtein.getAccessionNumbersAll() + "\t" + currentProtein.getNumberUniquePeptides()
                                + "\t" + currentProtein.getPercentCoverage() + "\t" + currentProtein.getNumExperimentsTwoUniquePeptides()
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

                    w.close();
                    f.close();

                    JOptionPane.showMessageDialog(this, "Results successfully exported.", "Results Exported", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "An error occured when exporting the results. See ../conf/MiTRAQLog.txt for details.");
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

        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Peptides").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Exp. Count").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Coverage").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("P-value").getCellRenderer()).showNumbers(!showSparklines);
        ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Q-value").getCellRenderer()).showNumbers(!showSparklines);

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
        resultsJTableMouseClicked(null);
    }//GEN-LAST:event_errorBarsJCheckBoxMenuItemActionPerformed

    /**
     * Turns the display of the highlighting of the average value bars on or off.
     * 
     * @param evt
     */
    private void highlightAveragesJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightAveragesJCheckBoxMenuItemActionPerformed
        highlightAverageBars = highlightAveragesJCheckBoxMenuItem.isSelected();
        resultsJTableMouseClicked(null);
    }//GEN-LAST:event_highlightAveragesJCheckBoxMenuItemActionPerformed

    /**
     * Turns the display of the labels in the bar charts on or off.
     * 
     * @param evt
     */
    private void barChartLabelsJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barChartLabelsJCheckBoxMenuItemActionPerformed
        showBarChartLabels = barChartLabelsJCheckBoxMenuItem.isSelected();
        resultsJTableMouseClicked(null);
    }//GEN-LAST:event_barChartLabelsJCheckBoxMenuItemActionPerformed

    /**
     * 
     * 
     * @param evt
     */
    private void exportPlotJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPlotJButtonActionPerformed
        new ExportPlot(this, true, chartPanel);
    }//GEN-LAST:event_exportPlotJButtonActionPerformed

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
     * @param title the title of the chart
     * @param barColors the colors to use for the bars
     * @return the chart
     */
    private JFreeChart createRatioChart(CategoryDataset dataset, DefaultStatisticalCategoryDataset datasetErrors, String title, ArrayList<Color> barColors) {

        // create the bar chart
        final JFreeChart chart = ChartFactory.createBarChart(
                title, // chart title
                null, // domain axis label
                "Ratio (log 2)", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // the plot orientation
                false, // include legend
                true, // tooltips
                false); // urls

        // set the background and gridline colors
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);

        // set the bar chart renderer
        CategoryItemRenderer renderer = new BarChartColorRenderer(barColors);
        renderer.setBaseItemLabelsVisible(true);

        // add bar chart labels if selected
        if (showBarChartLabels) {
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
            renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        }

        plot.setRenderer(renderer);

        // change the margin at the top and bottom of the range axis
        final ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLowerMargin(0.15);
        rangeAxis.setUpperMargin(0.15);

        // add a second axis on the right, identical to the left one
        ValueAxis rangeAxis2 = chart.getCategoryPlot().getRangeAxis();
        plot.setRangeAxis(1, rangeAxis2);

        // add error bars to the bar chart if selected
        if (showErrorBars) {
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
        try {
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception, i.e. use default look and feel
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                MiTRAQ miTRAQ = new MiTRAQ();
                miTRAQ.setVisible(true);

                String dataFile = null;

                // if a file is found in the data folder, suggest this as the file to open
                if (new File(miTRAQ.getJarFilePath() + "/data/").listFiles().length > 0) {
                    dataFile = new File(miTRAQ.getJarFilePath() + "/data/").listFiles()[0].getPath();
                }

                new ExperimentalDesign(miTRAQ, true, dataFile);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JScrollPane accessiobNumbersJScrollPane;
    private javax.swing.JEditorPane accessionNumbersJEditorPane;
    private javax.swing.JCheckBoxMenuItem barChartLabelsJCheckBoxMenuItem;
    private javax.swing.JPanel chartJPanel;
    private javax.swing.JCheckBoxMenuItem errorBarsJCheckBoxMenuItem;
    private javax.swing.JMenuItem exitJMenuItem;
    private javax.swing.JButton exportPlotJButton;
    private javax.swing.JButton exportProteinListJButton;
    private javax.swing.JMenu fileJMenu;
    private javax.swing.JButton filterResultsJButton;
    private javax.swing.JMenu helpJMenu;
    private javax.swing.JMenuItem helpJMenuItem;
    private javax.swing.JCheckBoxMenuItem highlightAveragesJCheckBoxMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openJMenuItem;
    private javax.swing.JLabel proteinCountJLabel;
    private javax.swing.JPanel resultsJPanel;
    private javax.swing.JSplitPane resultsJSplitPane;
    private javax.swing.JTable resultsJTable;
    private javax.swing.JPanel resultsTableJPanel;
    private javax.swing.JScrollPane resultsTableJScrollPane;
    private javax.swing.JLabel significanceLevelJLabel;
    private javax.swing.JSpinner significanceLevelJSpinner;
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
        w.write("Protein Name or Accession Contains: " + currentFilterValues[0] + "\n");
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


        w.write("Fold Change: ");

        if (currrentFilterRadioButtonSelections[3] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[3] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[4] + "\n");


        w.write("P-value: ");

        if (currrentFilterRadioButtonSelections[4] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[4] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[5] + "\n");


        w.write("Q-value: ");

        if (currrentFilterRadioButtonSelections[5] == 0) {
            w.write("> ");
        } else if (currrentFilterRadioButtonSelections[5] == 1) {
            w.write("= ");
        } else {
            w.write("< ");
        }

        w.write(currentFilterValues[6] + "\n");

        w.write("Significance Level: " + significanceLevelJSpinner.getValue() + "\n");
    }

    /**
     * Loads the iTRAQ data from the ssv input file.
     *
     * @param groupALabel the label to use for group A
     * @param groupBLabel the label to use for group B
     * @param currentITraqType the current iTRAQ type (4-plex or 8-plex)
     * @param currentITraqReference the current iTRAQ reference
     * @param numberOfExperiments the number of iTRAQ experiments
     * @param experimentalDesignJTable the experimental design table with the experimental setup
     * @param ratioFile the ssv file containing the data to load
     */
    public void loadItraqData(String groupALabel, String groupBLabel, String currentITraqType,
            String currentITraqReference, Integer numberOfExperiments, JTable experimentalDesignJTable, String ratioFile) {

        currentRatioFile = ratioFile;

        // save the experimental design for later
        File experimentalDesignFile = new File(getJarFilePath() + "/conf/" + new File(ratioFile).getName() + ".config");
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
            e.printStackTrace();
        }

        this.groupALabel = groupALabel;
        this.groupBLabel = groupBLabel;

        this.groupAColor = new Color(groupAColor.getRGB());
        this.groupBColor = new Color(groupBColor.getRGB());

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));


        // clear old data

        while (resultsJTable.getRowCount() > 0) {
            ((DefaultTableModel) resultsJTable.getModel()).removeRow(0);
        }

        chartJPanel.removeAll();

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                chartJPanel.repaint();
            }
        });

        accessionNumbersJEditorPane.setText(null);


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

        int groupANumberOfMembers = 0;
        int groupBNumberOfMembers = 0;

        for (int i = 0; i < experimentalDesignJTable.getRowCount(); i++) {
            for (int j = 1; j < experimentalDesignJTable.getColumnCount(); j++) {

                if (experimentalDesignJTable.getValueAt(i, j) != null) {

                    String currentValue = experimentalDesignJTable.getValueAt(i, j).toString();

                    if (!currentValue.equalsIgnoreCase("Ref")) {
                        experimentLabels[i][j - 2] = currentValue;
                    }

                    if (currentValue.equalsIgnoreCase(groupALabel)) {
                        allRatios.put(i + "_" + (j - 2), new ArrayList<Double>());
                        groupANumberOfMembers++;
                    } else if (currentValue.equalsIgnoreCase(groupBLabel)) {
                        allRatios.put(i + "_" + (j - 2), new ArrayList<Double>());
                        groupBNumberOfMembers++;
                    }
                } else {
                    experimentLabels[i][j - 2] = null;
                }
            }
        }

        if (groupANumberOfMembers < 2 || groupBNumberOfMembers < 2) {
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            JOptionPane.showMessageDialog(this, "At least two members in each group is required.");
            return;
        }


        // start reading the iTRAQ ssv file

        ArrayList<Protein> allProteins = new ArrayList<Protein>();

        try {
            FileReader f = new FileReader(ratioFile);
            BufferedReader b = new BufferedReader(f);

            String headerLine = b.readLine();

            StringTokenizer tok = new StringTokenizer(headerLine, ";");

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

            // read the data lines
            while (currentLine != null) {

                tok = new StringTokenizer(currentLine, ";");

                Vector<String> rowValues = new Vector<String>();

                // store the data for the currrent line
                while (tok.hasMoreTokens()) {
                    rowValues.add(tok.nextToken());
                }

                int numExperimentsTwoUniquePeptides = 0;

                for (int i = 0; i < numberOfExperiments; i++) {

                    int numUniquePeptides;

                    if (oldDataFormat) {
                        numUniquePeptides = new Integer(rowValues.get(
                                columnHeaders.get("Exp. " + (i + 1) + " Unique Peps").intValue())).intValue();
                    } else {
                        numUniquePeptides = new Integer(rowValues.get(
                                columnHeaders.get("Exp" + (i + 1) + " unique_peptides").intValue())).intValue();
                    }

                    if (numUniquePeptides > 1) {
                        numExperimentsTwoUniquePeptides++;
                    }
                }

                ArrayList<Double> ratiosGroupA = new ArrayList<Double>();
                ArrayList<Double> ratiosGroupB = new ArrayList<Double>();

                for (int i = 0; i < numberOfExperiments; i++) {

                    int numUniquePeptides;
                    int numUniqueSpectra;

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
                                || columnHeaders.get("Exp. " + (i + 1) + " iTRAQ_" + (j + 1) + " log2 ratio") != null) { // old type formatting

                            String temp;

                            if (oldDataFormat) {
                                temp = rowValues.get(
                                        columnHeaders.get("Exp. " + (i + 1) + " iTRAQ_" + (j + 1) + " log2 ratio").intValue());
                            } else {
                                temp = rowValues.get(
                                        columnHeaders.get("Exp" + (i + 1) + " iTRAQ_ratio_" + (j + 1)).intValue());
                            }


                            temp = temp.replace(",", ".");

                            double ratio = new Double(temp).doubleValue();


                            // take log 2 of the ratio, NB: not needed for the old data format...
                            if (!oldDataFormat) {
                                ratio = Math.log(ratio) / Math.log(2);
                            }

                            if (experimentLabels[i][j] != null) {

                                if (numUniquePeptides > 1) {
                                    allRatios.get(i + "_" + j).add(ratio);
                                }

                                if (numUniqueSpectra < 2) {
                                    ratio = 0;
                                }

                                if (experimentLabels[i][j].equalsIgnoreCase(groupALabel)) {
                                    ratiosGroupA.add(ratio);
                                } else if (experimentLabels[i][j].equalsIgnoreCase(groupBLabel)) {
                                    ratiosGroupB.add(ratio);
                                }
                            }
                        }
                    }
                }

                // add the wanted details to the protein list
                if (numExperimentsTwoUniquePeptides > 1) {

                    String proteinName = rowValues.get(columnHeaders.get("entry_name").intValue());
                    String accessionNumber = rowValues.get(columnHeaders.get("accession_number").intValue());
                    String accessionNumbersAll = rowValues.get(columnHeaders.get("accession_numbers").intValue());

                    Integer numberUniquePeptides = new Integer(rowValues.get(columnHeaders.get("numPepsUnique").intValue()));
                    Integer percentCoverage = new Integer(rowValues.get(columnHeaders.get("percentCoverage").intValue()));

                    allProteins.add(new Protein(ratiosGroupA, ratiosGroupB, accessionNumber, accessionNumbersAll,
                            proteinName, numberUniquePeptides, numExperimentsTwoUniquePeptides, percentCoverage));
                }

                currentLine = b.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
                }
            }
        }


        // re-center the proteins
        for (int i = 0; i < allProteins.size(); i++) {

            Protein currentProtein = allProteins.get(i);

            int groupACounter = 0;
            int groupBCounter = 0;

            ArrayList<Double> groupAValues = currentProtein.getRatiosGroupA();
            ArrayList<Double> groupBValues = currentProtein.getRatiosGroupB();

            for (int j = 0; j < experimentLabels.length; j++) {
                for (int k = 0; k < experimentLabels[0].length; k++) {

                    if (experimentLabels[j][k] != null && experimentLabels[j][k].equalsIgnoreCase(groupALabel)) {
                        if (groupAValues.get(groupACounter) == 0) {
                            groupAValues.set(groupACounter, null);
                        } else {
                            groupAValues.set(groupACounter, groupAValues.get(groupACounter) - medianRatios.get(j + "_" + k));
                        }

                        groupACounter++;
                    } else if (experimentLabels[j][k] != null && experimentLabels[j][k].equalsIgnoreCase(groupBLabel)) {

                        if (groupBValues.get(groupBCounter) == 0) {
                            groupBValues.set(groupBCounter, null);
                        } else {
                            groupBValues.set(groupBCounter, groupBValues.get(groupBCounter) - medianRatios.get(j + "_" + k));
                        }

                        groupBCounter++;
                    }
                }
            }
        }


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

            double groupDiff = (averageSampleA - averageSampleB);
            double foldChange = 0.0;

            if (groupDiff > 0) {
                foldChange = Math.pow(2, groupDiff);
            } else {
                foldChange = -Math.pow(2, -groupDiff);
            }


            // test if fold change is a number
            if (!Double.isNaN(foldChange)) {

                try {
                    allValidFoldChanges.add(groupDiff);
                    currentProtein.setFoldChange(foldChange);

                    // require at least a minimum number of the experiments to have values
                    if (sampleACounter >= MINIMUM_NUMBER_OF_RATIOS_FOR_T_TEST && sampleBCounter >= MINIMUM_NUMBER_OF_RATIOS_FOR_T_TEST) {

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
                    System.out.println(currentProtein);
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

                while (allNonDiffExpressedPValues.get(qValueIndex) < currentProtein.getPValue()) {
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


//        // calculate the upper and lower boudaries for the fold change (+- 2SD)
//        SummaryStatistics stats = new SummaryStatistics();
//
//        double[] tempValues = new double[allValidFoldChanges.size()];
//
//        for (int i = 0; i < allValidFoldChanges.size(); i++) {
//
//            if (!allValidFoldChanges.get(i).isNaN()) {
//                stats.addValue(allValidFoldChanges.get(i).doubleValue());
//                tempValues[i] = allValidFoldChanges.get(i).doubleValue();
//            }
//        }
//
//        double lowerFoldChangeBoundary = Double.MIN_VALUE;
//        double upperFoldChangeBoundary = Double.MAX_VALUE;

//        lowerFoldChangeBoundary = StatUtils.percentile(tempValues, 50) - stats.getStandardDeviation() * 2;
//        upperFoldChangeBoundary = StatUtils.percentile(tempValues, 50) + stats.getStandardDeviation() * 2;
//
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
//
//        sdJLabel.setText("2SD: [" + Util.roundDouble(lowerFoldChangeBoundary, 2) + " - " + Util.roundDouble(upperFoldChangeBoundary, 2) + "]");

        double maxAbsoluteValueFoldChange = 0.0;
        double maxPeptideCount = 0;

        // add the proteins to the results table
        for (int i = 0; i < allValidProteins.size(); i++) {
            Protein currentProtein = allValidProteins.get(i);

            ((DefaultTableModel) resultsJTable.getModel()).addRow(
                    new Object[]{
                        new Integer(i + 1),
                        currentProtein.getProteinName() + " (" + currentProtein.getAccessionNumber() + ")",
                        new XYDataPoint(currentProtein.getFoldChange(), currentProtein.getPValue()),
                        currentProtein.getNumberUniquePeptides(),
                        currentProtein.getPercentCoverage(),
                        currentProtein.getNumExperimentsTwoUniquePeptides(),
                        currentProtein.getPValue(),
                        currentProtein.getQValue(),
                        currentProtein.getPValue() < equallyExpressedSignificanceLevel,
                        currentProtein.getPValue() < equallyExpressedSignificanceLevel / allValidProteins.size()
                    });

            if (Math.abs(currentProtein.getFoldChange()) > maxAbsoluteValueFoldChange) {
                maxAbsoluteValueFoldChange = Math.abs(currentProtein.getFoldChange());
            }

            if (currentProtein.getNumberUniquePeptides() > maxPeptideCount) {
                maxPeptideCount = currentProtein.getNumberUniquePeptides();
            }

            proteinCountJLabel.setText("Protein Count: " + resultsJTable.getRowCount());
        }

        if (resultsJTable.getRowCount() > 0) {
            ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setMaxValue(Math.ceil(maxAbsoluteValueFoldChange));
            ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("FC").getCellRenderer()).setMinValue(-Math.ceil(maxAbsoluteValueFoldChange));
            ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Peptides").getCellRenderer()).setMaxValue(maxPeptideCount);
            ((JSparklinesBarChartTableCellRenderer) resultsJTable.getColumn("Exp. Count").getCellRenderer()).setMaxValue(numberOfExperiments);


            resultsJTable.setRowSelectionInterval(0, 0);
            resultsJTableMouseClicked(null);
        }

        exportPlotJButton.setEnabled(true);
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
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
     * @param currrentFilterRadioButtonSelections the filter radio buttons to set
     */
    public void setCurrrentFilterRadioButtonSelections(Integer[] currrentFilterRadioButtonSelections) {
        this.currrentFilterRadioButtonSelections = currrentFilterRadioButtonSelections;
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
     * Returns the color to use for the average value bar for the given 
     * group color.
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
}
