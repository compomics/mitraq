package no.uib.mitraq.util;

import java.util.ArrayList;

/**
 * An object containing the details about a protein as contained in the iTRAQ 
 * input file.
 *
 * @author Harald Barsnes
 */
public class Protein implements Comparable<Object> {

    /**
     * The list of the number of spectra in each experiment.
     */
    private ArrayList<Integer> numSpectraGroupA;
    /**
     * The list of the number of peptides in each experiment.
     */
    private ArrayList<Integer> numPeptidesGroupA;
    /**
     * The list of the number of spectra in each experiment.
     */
    private ArrayList<Integer> numSpectraGroupB;
    /**
     * The list of the number of peptides in each experiment.
     */
    private ArrayList<Integer> numPeptidesGroupB;
    /**
     * The list of ratios in the first group.
     */
    private ArrayList<Double> ratiosGroupA;
    /**
     * The list of ratios in the first group.
     */
    private ArrayList<Double> ratiosGroupB;
    /**
     * The leading accession number.
     */
    private String accessionNumber;
    /**
     * The complete list of accession numbers.
     */
    private String accessionNumbersAll;
    /**
     * The protein name/description.
     */
    private String proteinName;
    /**
     * The number of unique peptides.
     */
    private Integer numberUniquePeptides;
    /**
     * The number of experiments in which the given protein has been detected.
     */
    private Integer numExperimentsDetected;
    /**
     * The number of iTRAQ ratios detected for this protein.
     */
    private Integer numQuantificationRatios;
    /**
     * The detected sequence coverage.
     */
    private Double percentCoverage;
    /**
     * The fold change between the two groups.
     */
    private Double foldChange;
    /**
     * The p-value for the t-test comparing the two groups.
     */
    private Double pValue;
    /**
     * The q-value for the ordered list of proteins.
     */
    private Double qValue;
    /**
     * The group A percentage.
     */
    private Double groupAPercent;
    /**
     * The group B percentage.
     */
    private Double groupBPercent;

    /**
     * Constructor.
     *
     * @param ratiosGroupA
     * @param ratiosGroupB
     * @param numSpectraGroupA
     * @param numPeptidesGroupA
     * @param numSpectraGroupB
     * @param numPeptidesGroupB
     * @param accessionNumber
     * @param accessionNumbersAll
     * @param proteinName
     * @param numberUniquePeptides
     * @param numExperimentsDetected
     * @param numQuantificationRatios 
     * @param percentCoverage
     */
    public Protein(ArrayList<Double> ratiosGroupA, ArrayList<Double> ratiosGroupB,
            ArrayList<Integer> numSpectraGroupA, ArrayList<Integer> numPeptidesGroupA,
            ArrayList<Integer> numSpectraGroupB, ArrayList<Integer> numPeptidesGroupB,
            String accessionNumber, String accessionNumbersAll, String proteinName,
            Integer numberUniquePeptides, Integer numExperimentsDetected, Integer numQuantificationRatios, 
            Double percentCoverage) {
        this.ratiosGroupA = ratiosGroupA;
        this.ratiosGroupB = ratiosGroupB;
        this.numSpectraGroupA = numSpectraGroupA;
        this.numPeptidesGroupA = numPeptidesGroupA;
        this.numSpectraGroupB = numSpectraGroupB;
        this.numPeptidesGroupB = numPeptidesGroupB;
        this.accessionNumber = accessionNumber;
        this.accessionNumbersAll = accessionNumbersAll;
        this.proteinName = proteinName;
        this.numberUniquePeptides = numberUniquePeptides;
        this.numExperimentsDetected = numExperimentsDetected;
        this.numQuantificationRatios = numQuantificationRatios;
        this.percentCoverage = percentCoverage;
        this.foldChange = null;
        this.pValue = null;
        this.qValue = null;
        this.groupAPercent = null;
        this.groupBPercent = null;
    }

    /**
     * Returns the protein name.
     *
     * @return the protein name as a String
     */
    public String toString() {
        return proteinName;
    }

    /**
     * Returns a list of all the non null ratios from group A.
     *
     * @return a list of all the non null ratios from group A
     */
    public ArrayList<Double> getAllNonNullFromGroupA(){

        ArrayList<Double> nonNullFragments = new ArrayList<Double>();

        for(int i=0; i < ratiosGroupA.size(); i++) {
            if(ratiosGroupA.get(i) != null){
                nonNullFragments.add(ratiosGroupA.get(i));
            }
        }

        return nonNullFragments;
    }

    /**
     * Returns a list of all the non null ratios from group A.
     *
     * @return a list of all the non null ratios from group A
     */
    public ArrayList<Double> getAllNonNullFromGroupB(){

        ArrayList<Double> nonNullFragments = new ArrayList<Double>();

        for(int i=0; i < ratiosGroupB.size(); i++) {
            if(ratiosGroupB.get(i) != null){
                nonNullFragments.add(ratiosGroupB.get(i));
            }
        }
        
        return nonNullFragments;
    }

    /**
     * @return the ratiosGroupA
     */
    public ArrayList<Double> getRatiosGroupA() {
        return ratiosGroupA;
    }

    /**
     * @param ratiosGroupA the ratiosGroupA to set
     */
    public void setRatiosGroupA(ArrayList<Double> ratiosGroupA) {
        this.ratiosGroupA = ratiosGroupA;
    }

    /**
     * @return the ratiosGroupB
     */
    public ArrayList<Double> getRatiosGroupB() {
        return ratiosGroupB;
    }

    /**
     * @param ratiosGroupB the ratiosGroupB to set
     */
    public void setRatiosGroupB(ArrayList<Double> ratiosGroupB) {
        this.ratiosGroupB = ratiosGroupB;
    }

    /**
     * @return the proteinName
     */
    public String getProteinName() {
        return proteinName;
    }

    /**
     * @param proteinName the proteinName to set
     */
    public void setProteinName(String proteinName) {
        this.proteinName = proteinName;
    }

    /**
     * @return the accessionNumber
     */
    public String getAccessionNumber() {
        return accessionNumber;
    }

    /**
     * @param accessionNumber the accessionNumber to set
     */
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    /**
     * @return the numberUniquePeptides
     */
    public Integer getNumberUniquePeptides() {
        return numberUniquePeptides;
    }

    /**
     * @param numberUniquePeptides the numberUniquePeptides to set
     */
    public void setNumberUniquePeptides(Integer numberUniquePeptides) {
        this.numberUniquePeptides = numberUniquePeptides;
    }

    /**
     * @return the percentCoverage
     */
    public Double getPercentCoverage() {
        return percentCoverage;
    }

    /**
     * @param percentCoverage the percentCoverage to set
     */
    public void setPercentCoverage(Double percentCoverage) {
        this.percentCoverage = percentCoverage;
    }

    /**
     * @return the foldChange
     */
    public Double getFoldChange() {
        return foldChange;
    }

    /**
     * @param foldChange the foldChange to set
     */
    public void setFoldChange(Double foldChange) {
        this.foldChange = foldChange;
    }

    /**
     * @return the pValue
     */
    public Double getPValue() {
        return pValue;
    }

    /**
     * @param pValue the pValue to set
     */
    public void setPValue(Double pValue) {
        this.pValue = pValue;
    }

    /**
     * @return the groupAPercent
     */
    public Double getGroupAPercent() {
        return groupAPercent;
    }

    /**
     * @param groupAPercent the groupAPercent to set
     */
    public void setGroupAPercent(Double groupAPercent) {
        this.groupAPercent = groupAPercent;
    }

    /**
     * @return the groupBPercent
     */
    public Double getGroupBPercent() {
        return groupBPercent;
    }

    /**
     * @param groupBPercent the groupBPercent to set
     */
    public void setGroupBPercent(Double groupBPercent) {
        this.groupBPercent = groupBPercent;
    }

    public int compareTo(Object o) {

        if (o instanceof Protein) {
            Protein tempProtein = (Protein) o;

            if (this.getPValue() == null) {
                return -1;
            } else if(this.getPValue().equals(Double.NaN)) {
                return 1;
            } else if (tempProtein.getPValue() == null) {
                return 1;
            } else if (tempProtein.getPValue().equals(Double.NaN)) {
                return -1;
            }

            if (this.getPValue() > tempProtein.getPValue()) {
                return 1;
            } else if (this.getPValue() < tempProtein.getPValue()) {
                return -1;
            } else {
                return 0;
            }
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the qValue
     */
    public Double getQValue() {
        return qValue;
    }

    /**
     * @param qValue the qValue to set
     */
    public void setqValue(Double qValue) {
        this.qValue = qValue;
    }

    /**
     * @return the numExperimentsDetected
     */
    public Integer getNumExperimentsDetected() {
        return numExperimentsDetected;
    }

    /**
     * @param numExperimentsDetected the numExperimentsDetected to set
     */
    public void setNumExperimentsDetected(Integer numExperimentsDetected) {
        this.numExperimentsDetected = numExperimentsDetected;
    }

    /**
     * @return the accessionNumbersAll
     */
    public String getAccessionNumbersAll() {
        return accessionNumbersAll;
    }

    /**
     * @param accessionNumbersAll the accessionNumbersAll to set
     */
    public void setAccessionNumbersAll(String accessionNumbersAll) {
        this.accessionNumbersAll = accessionNumbersAll;
    }

    /**
     * @return the numSpectra
     */
    public ArrayList<Integer> getNumSpectraGroupA() {
        return numSpectraGroupA;
    }

    /**
     * @param numSpectra the numSpectra to set
     */
    public void setNumSpectraGroupA(ArrayList<Integer> numSpectra) {
        this.numSpectraGroupA = numSpectra;
    }

    /**
     * @return the numSpectra
     */
    public ArrayList<Integer> getNumSpectraGroupB() {
        return numSpectraGroupB;
    }

    /**
     * @param numSpectra the numSpectra to set
     */
    public void setNumSpectraGroupB(ArrayList<Integer> numSpectra) {
        this.numSpectraGroupB = numSpectra;
    }

    /**
     * @return the numPeptides
     */
    public ArrayList<Integer> getNumPeptidesGroupA() {
        return numPeptidesGroupA;
    }

    /**
     * @param numPeptides the numPeptides to set
     */
    public void setNumPeptidesGroupA(ArrayList<Integer> numPeptides) {
        this.numPeptidesGroupA = numPeptides;
    }

    /**
     * @return the numPeptides
     */
    public ArrayList<Integer> getNumPeptidesGroupB() {
        return numPeptidesGroupB;
    }

    /**
     * @param numPeptides the numPeptides to set
     */
    public void setNumPeptidesGroupB(ArrayList<Integer> numPeptides) {
        this.numPeptidesGroupB = numPeptides;
    }

    /**
     * @return the numQuantificationRatios
     */
    public Integer getNumQuantificationRatios() {
        return numQuantificationRatios;
    }

    /**
     * @param numQuantificationRatios the numQuantificationRatios to set
     */
    public void setNumQuantificationRatios(Integer numQuantificationRatios) {
        this.numQuantificationRatios = numQuantificationRatios;
    }
}
