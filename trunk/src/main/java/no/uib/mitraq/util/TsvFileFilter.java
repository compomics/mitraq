package no.uib.mitraq.util;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * File filter for *.tsv files.
 *
 * @author  Harald Barsnes
 */
public class TsvFileFilter extends FileFilter {
    
    /**
     * Accept all directories, *.tsv files.
     *
     * @param f
     * @return boolean
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        String extension = FileFilterUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(FileFilterUtils.tsv)
                    || extension.equals(FileFilterUtils.TSV)){
                return true;
            } 
            else {
                return false;
            }
        }        
        return false;
    }
    
    /**
     * The description of this filter
     *
     * @return String
     */
    public java.lang.String getDescription() {
        return "*.tsv";
    }
}