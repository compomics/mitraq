
package no.uib.mitraq.util;

/**
 * Contains helper methods used by the other classes.
 *
 * @author Harald Barsnes
 */
public class Util {

    /**
     * Rounds of a double value to the wanted number of decimalplaces
     *
     * @param d the double to round of
     * @param places number of decimal places wanted
     * @return double - the new double
     */
    public static double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10, (double) places);
    }
}
