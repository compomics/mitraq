package no.uib.mitraq.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.table.TableRowSorter;
import no.uib.jsparklines.data.XYDataPoint;

/**
 * A dialog displaying various filters that can be applied to the protein results
 * table.
 *
 * @author Harald Barsnes
 */
public class ResultsFilter extends javax.swing.JDialog {

    /**
     * The protein results table.
     */
    private JTable resultsTable;
    /**
     * The MiTRAQ parent frame.
     */
    private MiTRAQ miTraq;

    /**
     * Creates a new ResultsFilter dialog.
     *
     * @param miTraq the MiTRAQ parent frame
     * @param modal if the dialog is modal or not
     * @param currentFilterValues the current filter text values
     * @param currrentFilterRadioButtonSelections the current filter radio button settings
     * @param foldChangeAbsoluteValue
     * @param visible if true the dialog is made visible
     */
    public ResultsFilter(MiTRAQ miTraq, boolean modal, String[] currentFilterValues, Integer[] currrentFilterRadioButtonSelections, 
            boolean foldChangeAbsoluteValue, boolean visible) {
        super(miTraq, modal);

        this.miTraq = miTraq;

        resultsTable = miTraq.getResultsTable();

        initComponents();
        
        // set the focus traveral policy
        HashMap<Component, Component> focusMap = new HashMap<Component, Component>();
        focusMap.put(proteinJTextField, accessionJTextField);
        focusMap.put(accessionJTextField, peptidesJTextField);
        focusMap.put(peptidesJTextField, coverageJTextField);
        focusMap.put(coverageJTextField, expCountJTextField);
        focusMap.put(expCountJTextField, quantCountJTextField);
        focusMap.put(quantCountJTextField, foldChangeJTextField);
        focusMap.put(foldChangeJTextField, pValueJTextField);
        focusMap.put(pValueJTextField, qValueJTextField);
        focusMap.put(qValueJTextField, okJButton);
        focusMap.put(okJButton, clearJButton);
        focusMap.put(clearJButton, proteinJTextField);
        
        HashMap<Component, Component> focusReverseMap = new HashMap<Component, Component>();
        focusReverseMap.put(proteinJTextField, clearJButton);
        focusReverseMap.put(accessionJTextField, proteinJTextField);
        focusReverseMap.put(peptidesJTextField, accessionJTextField);
        focusReverseMap.put(coverageJTextField, peptidesJTextField);
        focusReverseMap.put(expCountJTextField, coverageJTextField);
        focusReverseMap.put(quantCountJTextField, expCountJTextField);
        focusReverseMap.put(foldChangeJTextField, quantCountJTextField);
        focusReverseMap.put(pValueJTextField, foldChangeJTextField);
        focusReverseMap.put(qValueJTextField, pValueJTextField);
        focusReverseMap.put(okJButton, qValueJTextField);
        focusReverseMap.put(clearJButton, okJButton);
        
        MyFocusPolicy focusPolicy = new MyFocusPolicy(focusMap, focusReverseMap, proteinJTextField, clearJButton);
        this.setFocusTraversalPolicy(focusPolicy);

        // update the filter properties
        proteinJTextField.setText(currentFilterValues[0]);
        peptidesJTextField.setText(currentFilterValues[1]);
        coverageJTextField.setText(currentFilterValues[2]);
        expCountJTextField.setText(currentFilterValues[3]);
        quantCountJTextField.setText(currentFilterValues[4]);
        foldChangeJTextField.setText(currentFilterValues[5]);
        pValueJTextField.setText(currentFilterValues[6]);
        qValueJTextField.setText(currentFilterValues[7]);

        if (currentFilterValues.length > 8) {
            accessionJTextField.setText(currentFilterValues[8]);
        }

        peptidesGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[0].intValue() == 0);
        peptidesEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[0].intValue() == 1);
        peptidesLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[0].intValue() == 2);

        coverageGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[1].intValue() == 0);
        coverageEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[1].intValue() == 1);
        coverageLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[1].intValue() == 2);

        expCountGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[2].intValue() == 0);
        expCountEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[2].intValue() == 1);
        expCountLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[2].intValue() == 2);
        
        quantCountGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[3].intValue() == 0);
        quantCountEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[3].intValue() == 1);
        quantCountLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[3].intValue() == 2);

        foldChangeGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[4].intValue() == 0);
        foldChangeEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[4].intValue() == 1);
        foldChangeLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[4].intValue() == 2);

        pValueGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[5].intValue() == 0);
        pValueEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[5].intValue() == 1);
        pValueLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[5].intValue() == 2);

        qValueGreaterThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[6].intValue() == 0);
        qValueEqualJRadioButton.setSelected(currrentFilterRadioButtonSelections[6].intValue() == 1);
        qValueLessThanJRadioButton.setSelected(currrentFilterRadioButtonSelections[6].intValue() == 2);
        
        absoluteValueCheckBox.setSelected(foldChangeAbsoluteValue);

        setLocationRelativeTo(miTraq);
        setVisible(visible);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        peptidesButtonGroup = new javax.swing.ButtonGroup();
        coverageButtonGroup = new javax.swing.ButtonGroup();
        expCountButtonGroup = new javax.swing.ButtonGroup();
        quantCountButtonGroup = new javax.swing.ButtonGroup();
        foldChangeButtonGroup = new javax.swing.ButtonGroup();
        pValueButtonGroup = new javax.swing.ButtonGroup();
        qValueButtonGroup = new javax.swing.ButtonGroup();
        filterPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        proteinJTextField = new javax.swing.JTextField();
        accessionJTextField = new javax.swing.JTextField();
        peptidesJTextField = new javax.swing.JTextField();
        coverageJTextField = new javax.swing.JTextField();
        foldChangeJTextField = new javax.swing.JTextField();
        expCountJTextField = new javax.swing.JTextField();
        quantCountJTextField = new javax.swing.JTextField();
        pValueJTextField = new javax.swing.JTextField();
        qValueJTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        peptidesGreaterThanJRadioButton = new javax.swing.JRadioButton();
        peptidesEqualJRadioButton = new javax.swing.JRadioButton();
        peptidesLessThanJRadioButton = new javax.swing.JRadioButton();
        coverageGreaterThanJRadioButton = new javax.swing.JRadioButton();
        coverageEqualJRadioButton = new javax.swing.JRadioButton();
        coverageLessThanJRadioButton = new javax.swing.JRadioButton();
        expCountGreaterThanJRadioButton = new javax.swing.JRadioButton();
        expCountEqualJRadioButton = new javax.swing.JRadioButton();
        expCountLessThanJRadioButton = new javax.swing.JRadioButton();
        foldChangeGreaterThanJRadioButton = new javax.swing.JRadioButton();
        foldChangeEqualJRadioButton = new javax.swing.JRadioButton();
        foldChangeLessThanJRadioButton = new javax.swing.JRadioButton();
        pValueGreaterThanJRadioButton = new javax.swing.JRadioButton();
        pValueEqualJRadioButton = new javax.swing.JRadioButton();
        pValueLessThanJRadioButton = new javax.swing.JRadioButton();
        qValueGreaterThanJRadioButton = new javax.swing.JRadioButton();
        qValueEqualJRadioButton = new javax.swing.JRadioButton();
        qValueLessThanJRadioButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        quantCountGreaterThanJRadioButton = new javax.swing.JRadioButton();
        quantCountEqualJRadioButton = new javax.swing.JRadioButton();
        quantCountLessThanJRadioButton = new javax.swing.JRadioButton();
        absoluteValueCheckBox = new javax.swing.JCheckBox();
        clearJButton = new javax.swing.JButton();
        okJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Results Filter");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results Fillter"));

        jLabel9.setText("Protein:");

        proteinJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        proteinJTextField.setToolTipText("<html>\nFind all proteins containing a given string.<br>\nRegular expressions are supported.\n</html>");
        proteinJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                proteinJTextFieldKeyReleased(evt);
            }
        });

        accessionJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        accessionJTextField.setToolTipText("<html>\nFind all proteins containing a given accession number.<br>\nRegular expressions are supported.\n</html>");
        accessionJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                accessionJTextFieldKeyReleased(evt);
            }
        });

        peptidesJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        peptidesJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                peptidesJTextFieldKeyReleased(evt);
            }
        });

        coverageJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        coverageJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                coverageJTextFieldKeyReleased(evt);
            }
        });

        foldChangeJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        foldChangeJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                foldChangeJTextFieldKeyReleased(evt);
            }
        });

        expCountJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        expCountJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                expCountJTextFieldKeyReleased(evt);
            }
        });

        quantCountJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        quantCountJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                quantCountJTextFieldKeyReleased(evt);
            }
        });

        pValueJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pValueJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pValueJTextFieldKeyReleased(evt);
            }
        });

        qValueJTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        qValueJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                qValueJTextFieldKeyReleased(evt);
            }
        });

        jLabel10.setText("Peptides:");

        jLabel11.setText("Coverage:");

        jLabel12.setText("Exp. Count:");

        jLabel13.setText("Fold Change:");

        jLabel14.setText("p-value:");

        jLabel15.setText("q-value:");

        peptidesButtonGroup.add(peptidesGreaterThanJRadioButton);
        peptidesGreaterThanJRadioButton.setSelected(true);
        peptidesGreaterThanJRadioButton.setText(">");
        peptidesGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        peptidesButtonGroup.add(peptidesEqualJRadioButton);
        peptidesEqualJRadioButton.setText("=");
        peptidesEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        peptidesButtonGroup.add(peptidesLessThanJRadioButton);
        peptidesLessThanJRadioButton.setText("<");
        peptidesLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        coverageButtonGroup.add(coverageGreaterThanJRadioButton);
        coverageGreaterThanJRadioButton.setSelected(true);
        coverageGreaterThanJRadioButton.setText(">");
        coverageGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        coverageButtonGroup.add(coverageEqualJRadioButton);
        coverageEqualJRadioButton.setText("=");
        coverageEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        coverageButtonGroup.add(coverageLessThanJRadioButton);
        coverageLessThanJRadioButton.setText("<");
        coverageLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        expCountButtonGroup.add(expCountGreaterThanJRadioButton);
        expCountGreaterThanJRadioButton.setText(">");
        expCountGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        expCountButtonGroup.add(expCountEqualJRadioButton);
        expCountEqualJRadioButton.setText("=");
        expCountEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        expCountButtonGroup.add(expCountLessThanJRadioButton);
        expCountLessThanJRadioButton.setText("<");
        expCountLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        foldChangeButtonGroup.add(foldChangeGreaterThanJRadioButton);
        foldChangeGreaterThanJRadioButton.setSelected(true);
        foldChangeGreaterThanJRadioButton.setText(">");
        foldChangeGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        foldChangeButtonGroup.add(foldChangeEqualJRadioButton);
        foldChangeEqualJRadioButton.setText("=");
        foldChangeEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        foldChangeButtonGroup.add(foldChangeLessThanJRadioButton);
        foldChangeLessThanJRadioButton.setText("<");
        foldChangeLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        pValueButtonGroup.add(pValueGreaterThanJRadioButton);
        pValueGreaterThanJRadioButton.setText(">");
        pValueGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        pValueButtonGroup.add(pValueEqualJRadioButton);
        pValueEqualJRadioButton.setText("=");
        pValueEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        pValueButtonGroup.add(pValueLessThanJRadioButton);
        pValueLessThanJRadioButton.setSelected(true);
        pValueLessThanJRadioButton.setText("<");
        pValueLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        qValueButtonGroup.add(qValueGreaterThanJRadioButton);
        qValueGreaterThanJRadioButton.setText(">");
        qValueGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        qValueButtonGroup.add(qValueEqualJRadioButton);
        qValueEqualJRadioButton.setText("=");
        qValueEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        qValueButtonGroup.add(qValueLessThanJRadioButton);
        qValueLessThanJRadioButton.setSelected(true);
        qValueLessThanJRadioButton.setText("<");
        qValueLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("(contains, RegExp)");
        jLabel1.setToolTipText("<html>\nFind all proteins containing a given string.<br>\nRegular expressions are supported.\n</html>");

        jLabel16.setText("Accession:");

        jLabel2.setText("(contains, RegExp)");
        jLabel2.setToolTipText("<html>\nFind all proteins containing a given accession number.<br>\nRegular expressions are supported.\n</html>");

        jLabel17.setText("Quant. Count:");

        quantCountButtonGroup.add(quantCountGreaterThanJRadioButton);
        quantCountGreaterThanJRadioButton.setSelected(true);
        quantCountGreaterThanJRadioButton.setText(">");
        quantCountGreaterThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantCountGreaterThanJRadioButtonradioButtonActionPerformed(evt);
            }
        });

        quantCountButtonGroup.add(quantCountEqualJRadioButton);
        quantCountEqualJRadioButton.setText("=");
        quantCountEqualJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantCountEqualJRadioButtonradioButtonActionPerformed(evt);
            }
        });

        quantCountButtonGroup.add(quantCountLessThanJRadioButton);
        quantCountLessThanJRadioButton.setText("<");
        quantCountLessThanJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantCountLessThanJRadioButtonradioButtonActionPerformed(evt);
            }
        });

        absoluteValueCheckBox.setText("Abs");
        absoluteValueCheckBox.setToolTipText("Absolute Value");
        absoluteValueCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                absoluteValueCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, filterPanelLayout.createSequentialGroup()
                        .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16)
                            .addComponent(jLabel10)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                        .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(pValueJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(foldChangeJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(quantCountJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(expCountJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(coverageJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(peptidesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(accessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(proteinJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(qValueJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(peptidesGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(peptidesEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(peptidesLessThanJRadioButton))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(coverageGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(coverageEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(coverageLessThanJRadioButton))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(expCountGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(expCountEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(expCountLessThanJRadioButton))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(quantCountGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(quantCountEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(quantCountLessThanJRadioButton))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(foldChangeGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(foldChangeEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(foldChangeLessThanJRadioButton)
                        .addGap(6, 6, 6)
                        .addComponent(absoluteValueCheckBox))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(qValueGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(qValueEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(qValueLessThanJRadioButton))
                    .addGroup(filterPanelLayout.createSequentialGroup()
                        .addComponent(pValueGreaterThanJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pValueEqualJRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pValueLessThanJRadioButton))))
        );

        filterPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {accessionJTextField, coverageJTextField, expCountJTextField, foldChangeJTextField, pValueJTextField, peptidesJTextField, proteinJTextField, qValueJTextField, quantCountJTextField});

        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accessionJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(peptidesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(peptidesGreaterThanJRadioButton)
                    .addComponent(peptidesEqualJRadioButton)
                    .addComponent(peptidesLessThanJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(coverageGreaterThanJRadioButton)
                    .addComponent(coverageJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coverageEqualJRadioButton)
                    .addComponent(coverageLessThanJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expCountJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(expCountGreaterThanJRadioButton)
                    .addComponent(expCountEqualJRadioButton)
                    .addComponent(expCountLessThanJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quantCountJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quantCountGreaterThanJRadioButton)
                    .addComponent(jLabel17)
                    .addComponent(quantCountEqualJRadioButton)
                    .addComponent(quantCountLessThanJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(foldChangeJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(foldChangeGreaterThanJRadioButton)
                    .addComponent(foldChangeEqualJRadioButton)
                    .addComponent(foldChangeLessThanJRadioButton)
                    .addComponent(absoluteValueCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(pValueGreaterThanJRadioButton)
                    .addComponent(pValueJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pValueEqualJRadioButton)
                    .addComponent(pValueLessThanJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(qValueGreaterThanJRadioButton)
                    .addComponent(qValueJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qValueEqualJRadioButton)
                    .addComponent(qValueLessThanJRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        clearJButton.setText("Clear");
        clearJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearJButtonActionPerformed(evt);
            }
        });

        okJButton.setText("OK");
        okJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(351, Short.MAX_VALUE)
                .addComponent(okJButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearJButton)
                .addGap(22, 22, 22))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearJButton, okJButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearJButton)
                    .addComponent(okJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Clears all filters.
     *
     * @param evt
     */
    private void clearJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearJButtonActionPerformed
        proteinJTextField.setText("");
        peptidesJTextField.setText("");
        coverageJTextField.setText("");
        expCountJTextField.setText("");
        quantCountJTextField.setText("");
        foldChangeJTextField.setText("");
        pValueJTextField.setText("");
        qValueJTextField.setText("");
        absoluteValueCheckBox.setSelected(false);
        filter();
    }//GEN-LAST:event_clearJButtonActionPerformed

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void proteinJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_proteinJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_proteinJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void peptidesJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_peptidesJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_peptidesJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void coverageJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_coverageJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_coverageJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void expCountJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expCountJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_expCountJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void foldChangeJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_foldChangeJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_foldChangeJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void pValueJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pValueJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_pValueJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void qValueJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_qValueJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_qValueJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void radioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonActionPerformed
        filter();
    }//GEN-LAST:event_radioButtonActionPerformed

    /**
     * Saves the filter settings and closes the dialog.
     *
     * @param evt
     */
    private void okJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okJButtonActionPerformed

        // store the current filter values
        String[] currentFilterValues = {
            proteinJTextField.getText(),
            peptidesJTextField.getText(),
            coverageJTextField.getText(),
            expCountJTextField.getText(),
            quantCountJTextField.getText(),
            foldChangeJTextField.getText(),
            pValueJTextField.getText(),
            qValueJTextField.getText(),
            accessionJTextField.getText()};

        int peptideRadioButtonIndex = 0;

        if (peptidesEqualJRadioButton.isSelected()) {
            peptideRadioButtonIndex = 1;
        } else if (peptidesLessThanJRadioButton.isSelected()) {
            peptideRadioButtonIndex = 2;
        }

        int coverageRadioButtonIndex = 0;

        if (coverageEqualJRadioButton.isSelected()) {
            coverageRadioButtonIndex = 1;
        } else if (coverageLessThanJRadioButton.isSelected()) {
            coverageRadioButtonIndex = 2;
        }

        int expCountRadioButtonIndex = 0;

        if (expCountEqualJRadioButton.isSelected()) {
            expCountRadioButtonIndex = 1;
        } else if (expCountLessThanJRadioButton.isSelected()) {
            expCountRadioButtonIndex = 2;
        }
        
        int quantCountRadioButtonIndex = 0;

        if (quantCountEqualJRadioButton.isSelected()) {
            quantCountRadioButtonIndex = 1;
        } else if (quantCountLessThanJRadioButton.isSelected()) {
            quantCountRadioButtonIndex = 2;
        }

        int foldChangeRadioButtonIndex = 0;

        if (foldChangeEqualJRadioButton.isSelected()) {
            foldChangeRadioButtonIndex = 1;
        } else if (foldChangeLessThanJRadioButton.isSelected()) {
            foldChangeRadioButtonIndex = 2;
        }

        int pValueRadioButtonIndex = 0;

        if (pValueEqualJRadioButton.isSelected()) {
            pValueRadioButtonIndex = 1;
        } else if (pValueLessThanJRadioButton.isSelected()) {
            pValueRadioButtonIndex = 2;
        }

        int qValueRadioButtonIndex = 0;

        if (qValueEqualJRadioButton.isSelected()) {
            qValueRadioButtonIndex = 1;
        } else if (qValueLessThanJRadioButton.isSelected()) {
            qValueRadioButtonIndex = 2;
        }

        Integer[] currrentFilterRadioButtonSelections = {
            peptideRadioButtonIndex,
            coverageRadioButtonIndex,
            expCountRadioButtonIndex,
            quantCountRadioButtonIndex,
            foldChangeRadioButtonIndex,
            pValueRadioButtonIndex,
            qValueRadioButtonIndex};
        
        miTraq.setFilterFoldChangeAbsoluteValue(absoluteValueCheckBox.isSelected());

        miTraq.setCurrentFilterValues(currentFilterValues);
        miTraq.setCurrrentFilterRadioButtonSelections(currrentFilterRadioButtonSelections);
        miTraq.setFilterFoldChangeAbsoluteValue(absoluteValueCheckBox.isSelected());

        // close the dialog
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_okJButtonActionPerformed

    /**
     * Closes the dialog.
     * 
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        okJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void accessionJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_accessionJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_accessionJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void quantCountJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantCountJTextFieldKeyReleased
        filter();
    }//GEN-LAST:event_quantCountJTextFieldKeyReleased

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void quantCountGreaterThanJRadioButtonradioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantCountGreaterThanJRadioButtonradioButtonActionPerformed
        filter();
    }//GEN-LAST:event_quantCountGreaterThanJRadioButtonradioButtonActionPerformed

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void quantCountEqualJRadioButtonradioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantCountEqualJRadioButtonradioButtonActionPerformed
        filter();
    }//GEN-LAST:event_quantCountEqualJRadioButtonradioButtonActionPerformed

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void quantCountLessThanJRadioButtonradioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantCountLessThanJRadioButtonradioButtonActionPerformed
        filter();
    }//GEN-LAST:event_quantCountLessThanJRadioButtonradioButtonActionPerformed

    /**
     * Filters the results table according to the current filter settings.
     *
     * @param evt
     */
    private void absoluteValueCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_absoluteValueCheckBoxActionPerformed
        filter();
    }//GEN-LAST:event_absoluteValueCheckBoxActionPerformed

    /**
     * Filters the results table according to the current filter settings.
     */
    public void filter() {

        List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();

        // protein name filter
        String text = proteinJTextField.getText();

        if (text == null || text.length() == 0) {
            filters.add(RowFilter.regexFilter(".*"));
        } else {
            try {
                filters.add(RowFilter.regexFilter(text, resultsTable.getColumn("Protein").getModelIndex()));
            } catch (PatternSyntaxException pse) {
                //JOptionPane.showMessageDialog(this, "Bad regex pattern for protein!", "Filter Error", JOptionPane.ERROR_MESSAGE);
                //pse.printStackTrace();
            }
        }

        // protein accession filter
        text = accessionJTextField.getText();

        if (text == null || text.length() == 0) {
            filters.add(RowFilter.regexFilter(".*"));
        } else {
            try {
                filters.add(RowFilter.regexFilter(text, resultsTable.getColumn("Accession").getModelIndex()));
            } catch (PatternSyntaxException pse) {
                //JOptionPane.showMessageDialog(this, "Bad regex pattern for protein!", "Filter Error", JOptionPane.ERROR_MESSAGE);
                //pse.printStackTrace();
            }
        }

        // peptide number filter
        if (peptidesJTextField.getText().length() > 0) {

            try {
                Integer value = new Integer(peptidesJTextField.getText());

                if (peptidesGreaterThanJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("Peptides").getModelIndex()));
                } else if (peptidesEqualJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("Peptides").getModelIndex()));
                } else {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("Peptides").getModelIndex()));
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "Peptide count has to be an integer!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // coverage filter
        if (coverageJTextField.getText().length() > 0) {

            try {
                Integer value = new Integer(coverageJTextField.getText());

                if (coverageGreaterThanJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("Coverage").getModelIndex()));
                } else if (coverageEqualJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("Coverage").getModelIndex()));
                } else {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("Coverage").getModelIndex()));
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "Coverage has to be an integer!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // exp count filter
        if (expCountJTextField.getText().length() > 0) {

            try {
                Integer value = new Integer(expCountJTextField.getText());

                if (expCountGreaterThanJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("Exp. Count").getModelIndex()));
                } else if (expCountEqualJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("Exp. Count").getModelIndex()));
                } else {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("Exp. Count").getModelIndex()));
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "Exp. count has to be an integer!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // quant count filter
        if (quantCountJTextField.getText().length() > 0) {

            try {
                Integer value = new Integer(quantCountJTextField.getText());

                if (quantCountGreaterThanJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("Quant. Count").getModelIndex()));
                } else if (quantCountEqualJRadioButton.isSelected()) {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("Quant. Count").getModelIndex()));
                } else {
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("Quant. Count").getModelIndex()));
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "Exp. count has to be an integer!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // fold change filter
        if (foldChangeJTextField.getText().length() > 0) {

            try {
                final Double value = new Double(foldChangeJTextField.getText());
                final boolean absoluteValue = absoluteValueCheckBox.isSelected();

                RowFilter<Object, Object> foldChangeFilter = new RowFilter<Object, Object>() {

                    public boolean include(Entry<? extends Object, ? extends Object> entry) {

                        double temp = ((XYDataPoint) entry.getValue(resultsTable.getColumn("FC").getModelIndex())).getX();

                        if (absoluteValue) {
                            temp = Math.abs(temp);
                        }
                        
                        if (foldChangeGreaterThanJRadioButton.isSelected() && value < temp) {
                            return true;
                        } else if (foldChangeEqualJRadioButton.isSelected() && value == temp) {
                            return true;
                        } else if (foldChangeLessThanJRadioButton.isSelected() && value > temp) {
                            return true;
                        }

                        return false;
                    }
                };


                filters.add(foldChangeFilter);

            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "Fold change has to be a number!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // p-value filter
        if (pValueJTextField.getText().length() > 0) {

            try {
                Double value = new Double(pValueJTextField.getText());

                if (value != 0) {

                    if (pValueGreaterThanJRadioButton.isSelected()) {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("p-value").getModelIndex()));
                    } else if (pValueEqualJRadioButton.isSelected()) {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("p-value").getModelIndex()));
                    } else {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("p-value").getModelIndex()));
                    }
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "p-value has to be a number!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // q-value filter
        if (qValueJTextField.getText().length() > 0) {

            try {
                Double value = new Double(qValueJTextField.getText());

                if (value != 0) {

                    if (qValueGreaterThanJRadioButton.isSelected()) {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, value, resultsTable.getColumn("q-value").getModelIndex()));
                    } else if (qValueEqualJRadioButton.isSelected()) {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, value, resultsTable.getColumn("q-value").getModelIndex()));
                    } else {
                        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, value, resultsTable.getColumn("q-value").getModelIndex()));
                    }
                }
            } catch (NumberFormatException e) {
                //JOptionPane.showMessageDialog(this, "q-value has to be a number!", "Filter Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        RowFilter<Object, Object> allFilters = RowFilter.andFilter(filters);
        
        if (resultsTable.getRowSorter() != null) {
            ((TableRowSorter) resultsTable.getRowSorter()).setRowFilter(allFilters);
            
            if (resultsTable.getRowCount() > 0) {
                resultsTable.setRowSelectionInterval(0, 0);
            }

            miTraq.updateResultTableSelection();
        } 
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox absoluteValueCheckBox;
    private javax.swing.JTextField accessionJTextField;
    private javax.swing.JButton clearJButton;
    private javax.swing.ButtonGroup coverageButtonGroup;
    private javax.swing.JRadioButton coverageEqualJRadioButton;
    private javax.swing.JRadioButton coverageGreaterThanJRadioButton;
    private javax.swing.JTextField coverageJTextField;
    private javax.swing.JRadioButton coverageLessThanJRadioButton;
    private javax.swing.ButtonGroup expCountButtonGroup;
    private javax.swing.JRadioButton expCountEqualJRadioButton;
    private javax.swing.JRadioButton expCountGreaterThanJRadioButton;
    private javax.swing.JTextField expCountJTextField;
    private javax.swing.JRadioButton expCountLessThanJRadioButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.ButtonGroup foldChangeButtonGroup;
    private javax.swing.JRadioButton foldChangeEqualJRadioButton;
    private javax.swing.JRadioButton foldChangeGreaterThanJRadioButton;
    private javax.swing.JTextField foldChangeJTextField;
    private javax.swing.JRadioButton foldChangeLessThanJRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton okJButton;
    private javax.swing.ButtonGroup pValueButtonGroup;
    private javax.swing.JRadioButton pValueEqualJRadioButton;
    private javax.swing.JRadioButton pValueGreaterThanJRadioButton;
    private javax.swing.JTextField pValueJTextField;
    private javax.swing.JRadioButton pValueLessThanJRadioButton;
    private javax.swing.ButtonGroup peptidesButtonGroup;
    private javax.swing.JRadioButton peptidesEqualJRadioButton;
    private javax.swing.JRadioButton peptidesGreaterThanJRadioButton;
    private javax.swing.JTextField peptidesJTextField;
    private javax.swing.JRadioButton peptidesLessThanJRadioButton;
    private javax.swing.JTextField proteinJTextField;
    private javax.swing.ButtonGroup qValueButtonGroup;
    private javax.swing.JRadioButton qValueEqualJRadioButton;
    private javax.swing.JRadioButton qValueGreaterThanJRadioButton;
    private javax.swing.JTextField qValueJTextField;
    private javax.swing.JRadioButton qValueLessThanJRadioButton;
    private javax.swing.ButtonGroup quantCountButtonGroup;
    private javax.swing.JRadioButton quantCountEqualJRadioButton;
    private javax.swing.JRadioButton quantCountGreaterThanJRadioButton;
    private javax.swing.JTextField quantCountJTextField;
    private javax.swing.JRadioButton quantCountLessThanJRadioButton;
    // End of variables declaration//GEN-END:variables

    /**
     * The focus traversal policy map.
     */
    class MyFocusPolicy extends FocusTraversalPolicy {

        private HashMap<Component, Component> focusMap;
        private HashMap<Component, Component> focusReverseMap;
        private Component first;
        private Component last;
        
        public MyFocusPolicy (HashMap<Component, Component> focusMap, HashMap<Component, Component> focusReverseMap, Component first, Component last) {
            this.focusMap = focusMap;
            this.focusReverseMap = focusReverseMap;
            this.first = first;
            this.last = last;
        }
        
        @Override
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            return focusMap.get(aComponent);  
        } 

        @Override
        public Component getComponentBefore(Container aContainer, Component aComponent) {
            return focusReverseMap.get(aComponent);  
        }

        @Override
        public Component getFirstComponent(Container aContainer) {
            return first;
        }

        @Override
        public Component getLastComponent(Container aContainer) {
            return last;
        }

        @Override
        public Component getDefaultComponent(Container aContainer) {
            return first;
        }
    }
}
