package no.uib.mitraq.gui;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;

/**
 * Creates a new dialog displaying the lis tof removed proteins.
 *
 * @author Harald Barsnes
 */
public class RemovedProteins extends javax.swing.JDialog {

    /**
     * A reference to the MiTRAQ parent.
     */
    private MiTRAQ mitraq;
    /**
     * Arraylist of the currently removed proteins, i.e., not used in the
     * analysis.
     */
    private ArrayList<String> removedProteins;
    /**
     * Used to ontroll the select/deselect all feature.
     */
    private boolean selectAll = false;

    /**
     * Opens a new RemovedProteins dialog.
     *
     * @param mitraq            the MiTRAQ parent
     * @param modal             modal or not modal
     * @param removedProteins   the list of removed proteins
     */
    public RemovedProteins(MiTRAQ mitraq, boolean modal, ArrayList<String> removedProteins) {
        super(mitraq, modal);

        this.mitraq = mitraq;
        this.removedProteins = removedProteins;

        initComponents();

        proteinJTable.getColumn(" ").setMaxWidth(40);
        proteinJTable.getColumn(" ").setMinWidth(40);
        proteinJTable.getColumn("Removed").setMaxWidth(80);
        proteinJTable.getColumn("Removed").setMinWidth(80);

        proteinJTable.getColumn("Removed").setCellRenderer(new NimbusCheckBoxRenderer());

        for (int i=0; i<removedProteins.size(); i++) {
            ((DefaultTableModel) proteinJTable.getModel()).addRow(new Object[] {
            (i+1), removedProteins.get(i), true});
        }

        setLocationRelativeTo(mitraq);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectJPopupMenu = new javax.swing.JPopupMenu();
        selectAllJMenuItem = new javax.swing.JMenuItem();
        jScrollPane = new javax.swing.JScrollPane();
        proteinJTable = new javax.swing.JTable();
        updateJButton = new javax.swing.JButton();
        cancelJButton = new javax.swing.JButton();
        helpLabel = new javax.swing.JLabel();

        selectAllJMenuItem.setText("Deselect All");
        selectAllJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllJMenuItemActionPerformed(evt);
            }
        });
        selectJPopupMenu.add(selectAllJMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Removed Proteins");
        setMinimumSize(new java.awt.Dimension(650, 0));

        proteinJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Protein", "Removed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        proteinJTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                proteinJTableMouseClicked(evt);
            }
        });
        jScrollPane.setViewportView(proteinJTable);

        updateJButton.setText("Update");
        updateJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateJButtonActionPerformed(evt);
            }
        });

        cancelJButton.setText("Cancel");
        cancelJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelJButtonActionPerformed(evt);
            }
        });

        helpLabel.setFont(helpLabel.getFont().deriveFont((helpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        helpLabel.setText("Re-add a protein by removing the check sign in the last column and click on Update.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(helpLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                        .addComponent(updateJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelJButton))
                    .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelJButton, updateJButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelJButton)
                    .addComponent(updateJButton)
                    .addComponent(helpLabel))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelJButton, updateJButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog.
     *
     * @param evt
     */
    private void cancelJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelJButtonActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_cancelJButtonActionPerformed

    /**
     * Updates the results table based on the selection.
     *
     * @param evt
     */
    private void updateJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateJButtonActionPerformed

        removedProteins = new ArrayList<String> ();

        for (int i=0; i<proteinJTable.getRowCount(); i++) {
            if ((Boolean) proteinJTable.getValueAt(i, 2)) {
                removedProteins.add((String) proteinJTable.getValueAt(i, 1));
            }
        }

        this.setVisible(false);
        mitraq.reloadItraqData(removedProteins);
        this.dispose();
    }//GEN-LAST:event_updateJButtonActionPerformed

    /**
     * Selects or deselects all rows.
     *
     * @param evt
     */
    private void selectAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllJMenuItemActionPerformed
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

        if (selectAll) {
            for (int i = 0; i < proteinJTable.getRowCount(); i++) {
                proteinJTable.setValueAt(true, i, proteinJTable.getColumnCount() - 1);
            }
        } else {
            for (int i = 0; i < proteinJTable.getRowCount(); i++) {
                proteinJTable.setValueAt(false, i, proteinJTable.getColumnCount() - 1);
            }
        }

        selectAll = !selectAll;

        if(selectAll){
            selectAllJMenuItem.setText("Select All");
        } else {
            selectAllJMenuItem.setText("Deselect All");
        }

        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
}//GEN-LAST:event_selectAllJMenuItemActionPerformed

    /**
     * Opens the select/deselect all pop up menu.
     * 
     * @param evt
     */
    private void proteinJTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proteinJTableMouseClicked
        selectJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_proteinJTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelJButton;
    private javax.swing.JLabel helpLabel;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTable proteinJTable;
    private javax.swing.JMenuItem selectAllJMenuItem;
    private javax.swing.JPopupMenu selectJPopupMenu;
    private javax.swing.JButton updateJButton;
    // End of variables declaration//GEN-END:variables
}